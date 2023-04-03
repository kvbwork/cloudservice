CREATE TABLE files_info (
    id BIGSERIAL,
    owner_id BIGINT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    filesize BIGINT NOT NULL,
    hash VARCHAR(255) NOT NULL,
    content_uid VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    PRIMARY KEY (id)
);
