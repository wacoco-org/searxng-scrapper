package org.example.searxngscrapper.repsitory;

import org.example.searxngscrapper.modal.EngineResponseStats;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import java.util.UUID;

public interface EngineResponseStatsRepository extends ReactiveCrudRepository<EngineResponseStats, UUID> {}
