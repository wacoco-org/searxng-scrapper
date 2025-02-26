package org.example.searxngscrapper.modal;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("engine_response_stats")
@SuppressWarnings("unused")
public class EngineResponseStats {

    @Id
    private UUID id;


    @Column("group_id")
    private UUID groupId;

    @Column("brave")
    private String brave;

    @Column("duckduckgo")
    private String duckduckgo;

    @Column("qwant")
    private String qwant;

    @Column("yahoo")
    private String yahoo;

    @Column("google")
    private String google;

    @Column("startpage")
    private String startpage;

    @Column("wikidata")
    private String wikidata;

    @Column("wikipedia")
    private String wikipedia;

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public void setDuckduckgo(String duckduckgo) {
        this.duckduckgo = duckduckgo;
    }

    public void setBrave(String brave) {
        this.brave = brave;
    }

    public void setQwant(String qwant) {
        this.qwant = qwant;
    }

    public void setYahoo(String yahoo) {
        this.yahoo = yahoo;
    }

    public void setStartpage(String startpage) {
        this.startpage = startpage;
    }

    public void setGoogle(String google) {
        this.google = google;
    }

    public void setWikipedia(String wikipedia) {
        this.wikipedia = wikipedia;
    }

    public void setWikidata(String wikidata) {
        this.wikidata = wikidata;
    }
}