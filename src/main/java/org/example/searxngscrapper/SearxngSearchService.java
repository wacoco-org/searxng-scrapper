package org.example.searxngscrapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearxngSearchService {

    private final Logger logger = LoggerFactory.getLogger(SearxngSearchService.class);
    private final WebClient webClient;

    public SearxngSearchService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    public Mono<List<SearchResult>> fetchSearchResults(String keyword, String narrowing) {
        String query = (narrowing != null && !narrowing.isBlank())
                ? String.format("site:%s %s", narrowing, keyword)
                : keyword;

        String BASE_SEARCH_URL = "http://localhost:8080/search?q=%s&categories=general&language=auto&time_range=&safesearch=0&theme=simple";
        String requestUrl = String.format(BASE_SEARCH_URL, query);
        logger.info("Fetching search results from URL: {}", requestUrl);

        return webClient.get()
                .uri(requestUrl)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(html -> logger.debug("Received HTML response: {}...",
                        html.substring(0, Math.min(500, html.length()))))
                .map(this::parseHtmlResults)
                .doOnSuccess(results -> {
                    results.forEach(result -> {
                        System.out.println("Link: " + result.url());
                        System.out.println("Title: " + result.title());
                        System.out.println("Description: " + result.description());
                        System.out.println("Search Engine(s): " + String.join(", ", result.engines()));
                        System.out.println("------------------------------------------------");
                        logger.info("Successfully extracted {} search results.", results.size());
                    });
                })
                .doOnError(error -> logger.error("Error fetching search results: {}",
                        error.getMessage(), error));
    }

    private List<SearchResult> parseHtmlResults(String html) {
        try {
            logger.info("Parsing HTML for search results...");
            Document doc = Jsoup.parse(html);
            Elements articles = doc.select("article.result");

            if (articles.isEmpty()) {
                logger.warn("No search results found in HTML!");
            }

            return articles.stream().map(article -> {
                String title = article.select("h3 a").text();
                String url = article.select("a.url_header").attr("href");
                String description = article.select("p.content").text();
                List<String> searchEngines = article.select(".engines span").eachText();

                logger.debug("Extracted result - Title: {}, URL: {}, Engines: {}", title, url, searchEngines);

                return new SearchResult(title, url, description, searchEngines);
            }).limit(40).collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error parsing HTML: {}", e.getMessage(), e);
            throw new RuntimeException("Error parsing search results", e);
        }
    }

    public record SearchResult(String title, String url, String description, List<String> engines) {}

}
