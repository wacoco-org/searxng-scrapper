package org.example.searxngscrapper.repsitory;

import org.example.searxngscrapper.modal.SearchResult;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import java.util.UUID;

public interface SearchResultRepository extends ReactiveCrudRepository<SearchResult, UUID> {}
