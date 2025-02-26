package org.example.searxngscrapper.modal;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Table("search_group")
@SuppressWarnings("unused")
public class SearchGroup  implements Persistable<UUID> {

    @Id
    @Column("group_id")
    private UUID groupId;

    @Column("search_query")
    private String searchQuery;

    @Column("response_time")
    private Double responseTime;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("total_search_engine")
    private List<String> totalSearchEngine;

    @Column("total_links")
    private int total_links;

    @Transient
    private EngineResponseStats engineResponseStats;

    @Transient
    private boolean newEntity = true;

    @Override
    public UUID getId() {
        return groupId;
    }

    @Override
    public boolean isNew() {
        return newEntity;
    }

    public void markNotNew() {
        this.newEntity = false;
    }
    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public void setTotal_links(int total_links) {
        this.total_links = total_links;
    }

    public void setResponseTime(Double responseTime) {
        this.responseTime = responseTime;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }


    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setTotalSearchEngine(List<String> totalSearchEngine) {
        this.totalSearchEngine = totalSearchEngine;
    }

}