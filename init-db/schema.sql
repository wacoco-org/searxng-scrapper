CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE search_group (
    group_id uuid PRIMARY KEY NOT NULL,
    search_query varchar(255) NOT NULL,
    response_time double precision,
    total_links INTEGER,
    created_at timestamptz NOT NULL,
    total_search_engine varchar(255)[] NOT NULL
);

CREATE TABLE engine_response_stats (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    group_id uuid NOT NULL,
    brave varchar(255),
    duckduckgo varchar(255),
    qwant varchar(255),
    yahoo varchar(255),
    google varchar(255),
    startpage varchar(255),
    wikidata varchar(255),
    wikipedia varchar(255),
    CONSTRAINT fk_engine_group FOREIGN KEY (group_id) REFERENCES search_group(group_id)
);

CREATE TABLE search_result (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    group_id uuid NOT NULL,
    title varchar(255) NOT NULL,
    description varchar(1024),
    search_engine varchar(255)[],
    url_link varchar(255),
    page_number integer NOT NULL,
    CONSTRAINT fk_search_result_group FOREIGN KEY (group_id) REFERENCES search_group(group_id)
);
