package org.example.searxngscrapper.service;

import org.example.searxngscrapper.error.ErrorType;
import org.example.searxngscrapper.error.ScraperException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SearxngSearchService {

    private static final String BASE_URL = "http://13.61.152.91:8080";
    private static final String BASE_SEARCH_URL =
            BASE_URL + "/search?q=%s&categories=general&language=en&time_range=&safesearch=0&theme=simple&pageno=%d";

    private final Logger logger = LoggerFactory.getLogger(SearxngSearchService.class);
    private final WebClient webClient;

    public SearxngSearchService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
    }

    public Mono<List<SearchResult>> fetchSearchResults(String keyword, String narrowing) {
        final boolean useNarrowing = narrowing != null && !narrowing.isBlank();
        String query = useNarrowing
                ? String.format("site:%s %s", narrowing, keyword)
                : keyword;

        return Flux.range(1, 7) // Pages 1 to 7
                .flatMap(page -> fetchPageResults(query, page))
                .collectList()
                .map(lists -> lists.stream().flatMap(List::stream).distinct().collect(Collectors.toList()))
                .map(results -> results.stream()
                        .map(result -> useNarrowing ? applyNarrowing(result, narrowing) : result)
                        .collect(Collectors.toList()))
                .doOnSuccess(results -> logger.info("Total results fetched: {}", results.size()))
                .doOnError(error -> logger.error("Error fetching search results: {}", error.getMessage(), error));
    }

    private SearchResult applyNarrowing(SearchResult result, String narrowing) {
        try {
            String url = result.url();
            if (!url.toLowerCase().contains(narrowing.toLowerCase())) {
                URI uri = URI.create(url);
                URI newUri = new URI(
                        uri.getScheme(),
                        uri.getUserInfo(),
                        narrowing,
                        uri.getPort(),
                        uri.getPath(),
                        uri.getQuery(),
                        uri.getFragment());
                return new SearchResult(result.title(), newUri.toString(), result.description(), result.engines(), result.page());
            }
        } catch (Exception e) {
            logger.error("Error adding narrowing on URL {}: {}", result.url(), e.getMessage());
            throw new ScraperException(ErrorType.NARROWING_ERROR, "Error applying narrowing", e);
        }
        return result;
    }

    private Mono<List<SearchResult>> fetchPageResults(String query, int page) {
        String requestUrl = String.format(BASE_SEARCH_URL, query, page);
        logger.info("Fetching search results from URL (Page {}): {}", page, requestUrl);

        return webClient.get()
                .uri(requestUrl)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(html -> logger.debug("Received HTML response (Page {}): {}...", page,
                        html.substring(0, Math.min(500, html.length()))))
                .map(html -> parseHtmlResults(html, page))
                .doOnSuccess(results -> {
                    logger.info("Page {} results count: {}", page, results.size());
                    results.forEach(result -> {
                        logger.info("Link: {}", result.url());
                        logger.info("Title: {}", result.title());
                        logger.info("Description: {}", result.description());
                        logger.info("Search Engine(s): {}", String.join(", ", result.engines()));
                        logger.info("Found on Page: {}", result.page());
                        logger.info("------------------------------------------------");
                    });
                })
                .doOnError(error -> {
                    logger.error("Error fetching page {}: {}", page, error.getMessage(), error);
                })
                .onErrorMap(e -> new ScraperException(ErrorType.FETCH_PAGE_ERROR, "Error fetching page " + page, e));
    }

    private List<SearchResult> parseHtmlResults(String html, int page) {
        try {
            logger.info("Parsing HTML for search results on page {}...", page);
            Document doc = Jsoup.parse(html);
            Elements articles = doc.select("article.result");

            Elements rows = doc.select("#engines_msg-table tr");
            for (Element row : rows) {
                String engineName = row.select(".engine-name").text();
                String statusOrTime = Objects.requireNonNull(row.select("td").last()).text();
                logger.info("Response Time {} : {}", engineName, statusOrTime);
            }

            if (articles.isEmpty()) {
                logger.warn("No search results found on page {}!", page);
            }

            return articles.stream()
                    .map(article -> {
                        String title = article.select("h3 a").text();
                        String url = article.select("a.url_header").attr("href");
                        String description = article.select("p.content").text();
                        List<String> searchEngines = article.select(".engines span").eachText();

                        logger.debug("Extracted result - Page: {}, Title: {}, URL: {}, Engines: {}",
                                page, title, url, searchEngines);
                        return new SearchResult(title, url, description, searchEngines, page);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error parsing HTML on page {}: {}", page, e.getMessage(), e);
            throw new ScraperException(ErrorType.PARSE_ERROR, "Error parsing HTML on page " + page, e);
        }
    }

    public record SearchResult(String title, String url, String description, List<String> engines, int page) {}

}

