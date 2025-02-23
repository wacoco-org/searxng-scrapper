package org.example.searxngscrapper.api;

import org.example.searxngscrapper.service.SearxngSearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v0")
public class SearxngSearchRestController {

    private final SearxngSearchService searxngSearchService;

    public SearxngSearchRestController(SearxngSearchService searxngSearchService) {
        this.searxngSearchService = searxngSearchService;
    }

    @GetMapping("/search")
    public Mono<ResponseEntity<Object>> search(
            @RequestParam("keyword") String keyword,
            @RequestParam(name = "narrowing", required = false) String narrowing) {
        return searxngSearchService.fetchSearchResults(keyword, narrowing)
                .map(results -> ResponseEntity.ok((Object) results))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("internal error")));
    }


}
