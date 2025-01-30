package org.example.searxngscrapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;
@Service
public class SearxngSearchService {

    private final Logger logger = LoggerFactory.getLogger(SearxngSearchService.class);
    private final WebClient webClient;

    public SearxngSearchService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://13.61.152.91:8080").build();
    }

    public Mono<List<SearchResult>> fetchSearchResults(String keyword, String narrowing) {
        String query = (narrowing != null && !narrowing.isBlank())
                ? String.format("site:%s %s", narrowing, keyword)
                : keyword;

        return Flux.range(1, 3) // Get first 3 pages
                .flatMap(page -> fetchPageResults(query, page)) // Fetch results per page
                .collectList()
                .map(results -> results.stream()
                        .flatMap(List::stream)
                        .distinct()
                        .collect(Collectors.toList())) // Combine results
                .doOnSuccess(results -> logger.info("Total results fetched: {}", results.size()))
                .doOnError(error -> logger.error("Error fetching search results: {}", error.getMessage(), error));
    }

    private Mono<List<SearchResult>> fetchPageResults(String query, int page) {
        String BASE_SEARCH_URL = "http://13.61.152.91:8080/search?q=%s&categories=general&language=auto&time_range=&safesearch=0&theme=simple&pageno=%d";
        String requestUrl = String.format(BASE_SEARCH_URL, query, page);
        logger.info("Fetching search results from URL (Page {}): {}", page, requestUrl);

        return webClient.get()
                .uri(requestUrl)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(html -> logger.debug("Received HTML response (Page {}): {}...", page, html.substring(0, Math.min(500, html.length()))))
                .map(html -> parseHtmlResults(html, page))
                .doOnSuccess(results -> {
                    logger.info("Page {} results count: {}", page, results.size());
                    results.forEach(result -> {
                        System.out.println("Link: " + result.url());
                        System.out.println("Title: " + result.title());
                        System.out.println("Description: " + result.description());
                        System.out.println("Search Engine(s): " + String.join(", ", result.engines()));
                        System.out.println("Found on Page: " + result.page());
                        System.out.println("------------------------------------------------");
                    });
                })
                .doOnError(error -> logger.error("Error fetching page {}: {}", page, error.getMessage(), error));
    }

    private List<SearchResult> parseHtmlResults(String html, int page) {
        try {
            logger.info("Parsing HTML for search results on page {}...", page);
            Document doc = Jsoup.parse(html);
            Elements articles = doc.select("article.result");

            if (articles.isEmpty()) {
                logger.warn("No search results found on page {}!", page);
            }

            return articles.stream().map(article -> {
                String title = article.select("h3 a").text();
                String url = article.select("a.url_header").attr("href");
                String description = article.select("p.content").text();
                List<String> searchEngines = article.select(".engines span").eachText();

                logger.debug("Extracted result - Page: {}, Title: {}, URL: {}, Engines: {}", page, title, url, searchEngines);
                return new SearchResult(title, url, description, searchEngines, page);
            }).collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error parsing HTML on page {}: {}", page, e.getMessage(), e);
            throw new RuntimeException("Error parsing search results", e);
        }
    }

    public record SearchResult(String title, String url, String description, List<String> engines, int page) {}

}

