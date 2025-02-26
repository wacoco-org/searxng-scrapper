package org.example.searxngscrapper.repsitory;

import org.example.searxngscrapper.modal.SearchGroup;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import java.util.UUID;

public interface SearchGroupRepository extends ReactiveCrudRepository<SearchGroup, UUID> {}
