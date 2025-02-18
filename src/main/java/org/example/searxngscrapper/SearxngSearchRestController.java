package org.example.searxngscrapper;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v0")
public class SearxngSearchRestController {

    private final SearxngSearchService searxngSearchService;

    public SearxngSearchRestController(SearxngSearchService searxngSearchService) {
        this.searxngSearchService = searxngSearchService;
    }

    @GetMapping("/search")
    public Mono<List<SearxngSearchService.SearchResult>> search(
            @RequestParam("keyword") String keyword,
            @RequestParam(name = "narrowing", required = false) String narrowing) {
        return searxngSearchService.fetchSearchResults(keyword, narrowing);
    }

}
