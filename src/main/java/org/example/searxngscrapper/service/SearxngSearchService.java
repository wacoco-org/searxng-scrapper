package org.example.searxngscrapper.service;

import org.example.searxngscrapper.error.ErrorType;
import org.example.searxngscrapper.error.ScraperException;
import org.example.searxngscrapper.modal.EngineResponseStats;
import org.example.searxngscrapper.modal.SearchGroup;
import org.example.searxngscrapper.modal.SearchResult;
import org.example.searxngscrapper.modal.dto.SearchResultDTO;
import org.example.searxngscrapper.repsitory.EngineResponseStatsRepository;
import org.example.searxngscrapper.repsitory.SearchGroupRepository;
import org.example.searxngscrapper.repsitory.SearchResultRepository;
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

import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SearxngSearchService {

    private static final String BASE_URL = "http://13.61.152.91:8080";
    private static final String BASE_SEARCH_URL =
            BASE_URL + "/search?q=%s&categories=general&language=en&time_range=&safesearch=0&theme=simple&pageno=%d";

    private final Logger logger = LoggerFactory.getLogger(SearxngSearchService.class);
    private final WebClient webClient;
    private final SearchGroupRepository searchGroupRepository;
    private final SearchResultRepository searchResultRepository;
    private final EngineResponseStatsRepository engineResponseStatsRepository;

    public SearxngSearchService(WebClient.Builder webClientBuilder,
                                SearchGroupRepository searchGroupRepository,
                                SearchResultRepository searchResultRepository,
                                EngineResponseStatsRepository engineResponseStatsRepository) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
        this.searchGroupRepository = searchGroupRepository;
        this.searchResultRepository = searchResultRepository;
        this.engineResponseStatsRepository = engineResponseStatsRepository;
    }

    public Mono<List<SearchResultDTO>> searchAndSave(String keyword, String narrowing) {
        boolean useNarrowing = narrowing != null && !narrowing.isBlank();
        String query = useNarrowing ? String.format("site:%s %s", narrowing, keyword) : keyword;

        SearchGroup group = new SearchGroup();
        group.setGroupId(UUID.randomUUID());
        group.setSearchQuery(query);
        group.setCreatedAt(OffsetDateTime.now());
        group.setTotalSearchEngine(new ArrayList<>());

        return searchGroupRepository.save(group)
                .flatMap(savedGroup -> {
                    savedGroup.markNotNew();
                    return Flux.range(1, 7)
                            .flatMap(page -> fetchPageResults(query, page))
                            .collectList()
                            .flatMap(pageResults -> {
                                // Collect all search results from all pages
                                List<SearchResultDTO> allResults = pageResults.stream()
                                        .flatMap(pr -> pr.searchResults().stream())
                                        .collect(Collectors.toList());

                                allResults.forEach(dto -> dto.setGroupId(savedGroup.getGroupId()));

                                // Aggregate distinct engine names
                                Set<String> distinctEngines = new HashSet<>();
                                for (PageResult pr : pageResults) {
                                    for (SearchResultDTO dto : pr.searchResults()) {
                                        distinctEngines.addAll(dto.getEngines());
                                    }
                                }

                                List<Double> responseTimes = pageResults.stream()
                                        .map(pr -> {
                                            if (pr.totalResponseTime() != null) {
                                                try {
                                                    return Double.parseDouble(pr.totalResponseTime());
                                                } catch (NumberFormatException nfe) {
                                                    logger.warn("Failed to parse response time '{}' on a page", pr.totalResponseTime());
                                                }
                                            }
                                            return null;
                                        })
                                        .filter(Objects::nonNull)
                                        .toList();
                                double avgResponseTime = 0.0;
                                if (!responseTimes.isEmpty()) {
                                    avgResponseTime = responseTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                                }

                                savedGroup.setResponseTime(avgResponseTime);
                                savedGroup.setTotal_links(allResults.size());
                                savedGroup.setTotalSearchEngine(new ArrayList<>(distinctEngines));
                                Mono<SearchGroup> updateGroupMono = searchGroupRepository.save(savedGroup);

                                List<SearchResult> searchResults = allResults.stream().map(dto -> {
                                    SearchResult sr = new SearchResult();
                                    sr.setGroupId(savedGroup.getGroupId());
                                    sr.setTitle(dto.getTitle());
                                    sr.setUrlLink(dto.getUrl());
                                    sr.setDescription(dto.getDescription());
                                    sr.setSearchEngine(dto.getEngines());
                                    sr.setPageNumber(dto.getPage());
                                    return sr;
                                }).collect(Collectors.toList());
                                Mono<Void> saveResultsMono = searchResultRepository.saveAll(searchResults).then();

                                Map<String, String> aggregatedStats = new HashMap<>();
                                for (PageResult pr : pageResults) {
                                    aggregatedStats.putAll(pr.engineStats());
                                }
                                EngineResponseStats stats = new EngineResponseStats();
                                stats.setGroupId(savedGroup.getGroupId());
                                stats.setBrave(aggregatedStats.get("brave"));
                                stats.setDuckduckgo(aggregatedStats.get("duckduckgo"));
                                stats.setQwant(aggregatedStats.get("qwant"));
                                stats.setYahoo(aggregatedStats.get("yahoo"));
                                stats.setGoogle(aggregatedStats.get("google"));
                                stats.setStartpage(aggregatedStats.get("startpage"));
                                stats.setWikidata(aggregatedStats.get("wikidata"));
                                stats.setWikipedia(aggregatedStats.get("wikipedia"));
                                Mono<EngineResponseStats> saveStatsMono = engineResponseStatsRepository.save(stats);

                                return Mono.when(updateGroupMono, saveResultsMono, saveStatsMono)
                                        .thenReturn(allResults);
                            });
                })
                .doOnError(e -> logger.error("Error in searchAndSave: {}", e.getMessage(), e));
    }

    private Mono<PageResult> fetchPageResults(String query, int page) {
        String requestUrl = String.format(BASE_SEARCH_URL, query, page);
        logger.info("Fetching search results from URL (Page {}): {}", page, requestUrl);

        return webClient.get()
                .uri(requestUrl)
                .retrieve()
                .bodyToMono(String.class)
                .map(html -> parseHtmlResults(html, page))
                .doOnError(error -> logger.error("Error fetching page {}: {}", page, error.getMessage(), error))
                .onErrorMap(e -> new ScraperException(ErrorType.FETCH_PAGE_ERROR, "Error fetching page " + page, e));
    }

    private PageResult parseHtmlResults(String html, int page) {
        try {
            Document doc = Jsoup.parse(html);

            // Collect the search results
            Elements articles = doc.select("article.result");
            List<SearchResultDTO> results = articles.stream().map(article -> {
                String title = article.select("h3 a").text();
                String url = article.select("a.url_header").attr("href");
                String description = article.select("p.content").text();
                List<String> engines = article.select(".engines span").eachText();
                SearchResultDTO dto = new SearchResultDTO();
                dto.setTitle(title);
                dto.setUrl(url);
                dto.setDescription(description);
                dto.setEngines(engines);
                dto.setPage(page);
                return dto;
            }).collect(Collectors.toList());

            Map<String, String> engineStats = new HashMap<>();
            Elements rows = doc.select("#engines_msg-table tr");
            for (Element row : rows) {
                String engineName = row.select(".engine-name").text().trim().toLowerCase();
                String statusOrTime = Objects.requireNonNull(row.select("td").last()).text().trim();
                engineStats.put(engineName, statusOrTime);
            }

            String totalResponseTime = null;
            String summaryText = doc.select("#engines_msg-title").text();
            if (!summaryText.isEmpty()) {
                Pattern pattern = Pattern.compile("Response time:\\s+([0-9.]+)\\s+seconds");
                Matcher matcher = pattern.matcher(summaryText);
                if (matcher.find()) {
                    totalResponseTime = matcher.group(1);
                }
            }

            return new PageResult(results, engineStats, totalResponseTime);
        } catch (Exception e) {
            logger.error("Error parsing HTML on page {}: {}", page, e.getMessage(), e);
            throw new ScraperException(ErrorType.PARSE_ERROR, "Error parsing HTML on page " + page, e);
        }
    }

    private record PageResult(List<SearchResultDTO> searchResults, Map<String, String> engineStats,
                              String totalResponseTime) {
    }
}