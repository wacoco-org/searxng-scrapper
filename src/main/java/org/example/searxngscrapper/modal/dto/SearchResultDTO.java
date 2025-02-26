package org.example.searxngscrapper.modal.dto;

import java.util.List;
import java.util.UUID;

public class SearchResultDTO {

    private UUID groupId;
    private String title;
    private String url;
    private String description;
    private List<String> engines;
    private int page;

    public SearchResultDTO(String title, String url, String description, List<String> engines, int page) {
        this.title = title;
        this.url = url;
        this.description = description;
        this.engines = engines;
        this.page = page;
    }

    public SearchResultDTO() {
    }

    public String getTitle() {
        return title;
    }


    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getEngines() {
        return engines;
    }

    public void setEngines(List<String> engines) {
        this.engines = engines;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
