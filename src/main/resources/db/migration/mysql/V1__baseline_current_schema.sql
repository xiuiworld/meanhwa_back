-- Baseline schema for Flyway-managed MySQL production databases.
-- This migration contains schema only. Seed data stays outside Flyway so
-- production does not receive local/test dummy image URLs.

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    provider VARCHAR(20) NOT NULL,
    oauth_id VARCHAR(100) NOT NULL,
    email VARCHAR(100) NULL,
    nickname VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_provider_oauth_id UNIQUE (provider, oauth_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE flowers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    image_url VARCHAR(255) NULL,
    core_meaning VARCHAR(100) NULL,
    description TEXT NULL,
    scientific_name VARCHAR(150) NULL,
    origin VARCHAR(100) NULL,
    blooming_season VARCHAR(100) NULL,
    scent VARCHAR(100) NULL,
    management_level VARCHAR(20) NOT NULL,
    management_info TEXT NULL,
    is_toxic_to_pets BOOLEAN NOT NULL,
    price_range VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE tags (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category VARCHAR(50) NOT NULL,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(80) NULL,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_tags_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE flower_tag_mappings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    flower_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    weight INT NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_flower_tag (flower_id, tag_id),
    INDEX idx_tag_flower (tag_id, flower_id),
    CONSTRAINT fk_flower_tag_mappings_flower FOREIGN KEY (flower_id) REFERENCES flowers (id),
    CONSTRAINT fk_flower_tag_mappings_tag FOREIGN KEY (tag_id) REFERENCES tags (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(100) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX idx_refresh_token_hash (token_hash),
    INDEX idx_refresh_token_user (user_id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_likes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    flower_id BIGINT NOT NULL,
    created_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_likes_user_flower UNIQUE (user_id, flower_id),
    INDEX idx_user_likes_user_created (user_id, created_at),
    INDEX idx_user_likes_flower (flower_id),
    CONSTRAINT fk_user_likes_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_likes_flower FOREIGN KEY (flower_id) REFERENCES flowers (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_histories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    flower_id BIGINT NOT NULL,
    viewed_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_histories_user_flower UNIQUE (user_id, flower_id),
    INDEX idx_user_histories_user_viewed (user_id, viewed_at),
    INDEX idx_user_histories_flower (flower_id),
    CONSTRAINT fk_user_histories_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_histories_flower FOREIGN KEY (flower_id) REFERENCES flowers (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_curation_results (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    flow_version VARCHAR(50) NOT NULL,
    selections TEXT NOT NULL,
    recommendations TEXT NOT NULL,
    result_count INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_user_curation_results_user_created (user_id, created_at),
    CONSTRAINT fk_user_curation_results_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_messages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    flower_id BIGINT NOT NULL,
    curation_result_id BIGINT NULL,
    flower_name VARCHAR(100) NOT NULL,
    flower_image_url VARCHAR(255) NULL,
    core_meaning VARCHAR(100) NULL,
    selected_tags TEXT NOT NULL,
    sender_name VARCHAR(50) NOT NULL,
    receiver_name VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_user_messages_user_created (user_id, created_at),
    INDEX idx_user_messages_curation_result (curation_result_id),
    INDEX idx_user_messages_flower (flower_id),
    CONSTRAINT fk_user_messages_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_messages_flower FOREIGN KEY (flower_id) REFERENCES flowers (id),
    CONSTRAINT fk_user_messages_curation_result FOREIGN KEY (curation_result_id) REFERENCES user_curation_results (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE action_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NULL,
    action_type VARCHAR(50) NOT NULL,
    action_data TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_action_logs_type_created (action_type, created_at),
    INDEX idx_action_logs_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
