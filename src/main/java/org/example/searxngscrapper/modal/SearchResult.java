package org.example.searxngscrapper.modal;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")

@Table("search_result")
public class SearchResult {

    @Id
    private UUID id;

    @Column("group_id")
    private UUID groupId;

    @Column("title")
    private String title;

    @Column("description")
    private String description;

    @Column("search_engine")
    private List<String> searchEngine;

    @Column("url_link")
    private String urlLink;

    @Column("page_number")
    private int pageNumber;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getSearchEngine() {
        return searchEngine;
    }

    public void setSearchEngine(List<String> searchEngine) {
        this.searchEngine = searchEngine;
    }

    public String getUrlLink() {
        return urlLink;
    }

    public void setUrlLink(String urlLink) {
        this.urlLink = urlLink;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }
}