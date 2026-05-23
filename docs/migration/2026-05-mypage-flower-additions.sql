-- =============================================================================
-- Meanhwa 운영 DB: 마이페이지 이력/꽃 정보 추가 (2026-05)
-- 과거 수동 적용 이력입니다. 현재 운영/신규 DB에는 실행하지 마세요.
-- 꽃 상세 추가 컬럼(scientific_name, origin, blooming_season, scent)은
-- Flyway V4__drop_unused_flower_detail_columns.sql에서 제거됩니다.
-- 현재 schema 변경은 src/main/resources/db/migration/mysql/V*.sql로 관리합니다.
-- =============================================================================

SET NAMES utf8mb4;
SET @db := DATABASE();

-- -----------------------------------------------------------------------------
-- 1) flowers: 관리 정보와 별도로 꽃 자체 정보 컬럼 추가
-- -----------------------------------------------------------------------------
SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'flowers' AND COLUMN_NAME = 'description');
SET @ddl := IF(@has_col = 0, 'ALTER TABLE flowers ADD COLUMN description TEXT NULL COMMENT ''꽃/식물 자체 소개 및 특징'' AFTER core_meaning', 'SELECT ''flowers.description already exists'' AS info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'flowers' AND COLUMN_NAME = 'scientific_name');
SET @ddl := IF(@has_col = 0, 'ALTER TABLE flowers ADD COLUMN scientific_name VARCHAR(150) NULL COMMENT ''학명'' AFTER description', 'SELECT ''flowers.scientific_name already exists'' AS info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'flowers' AND COLUMN_NAME = 'origin');
SET @ddl := IF(@has_col = 0, 'ALTER TABLE flowers ADD COLUMN origin VARCHAR(100) NULL COMMENT ''원산지 또는 주요 분포'' AFTER scientific_name', 'SELECT ''flowers.origin already exists'' AS info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'flowers' AND COLUMN_NAME = 'blooming_season');
SET @ddl := IF(@has_col = 0, 'ALTER TABLE flowers ADD COLUMN blooming_season VARCHAR(100) NULL COMMENT ''개화 시기'' AFTER origin', 'SELECT ''flowers.blooming_season already exists'' AS info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'flowers' AND COLUMN_NAME = 'scent');
SET @ddl := IF(@has_col = 0, 'ALTER TABLE flowers ADD COLUMN scent VARCHAR(100) NULL COMMENT ''향 정보'' AFTER blooming_season', 'SELECT ''flowers.scent already exists'' AS info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- -----------------------------------------------------------------------------
-- 2) user_curation_results: 큐레이션 완료 결과 snapshot
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_curation_results (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    flow_version VARCHAR(50) NOT NULL,
    selections TEXT NOT NULL,
    recommendations TEXT NOT NULL,
    result_count INT NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_user_curation_results_user_created (user_id, created_at),
    CONSTRAINT fk_user_curation_results_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 3) user_messages: 생성 메시지 이력 snapshot
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_messages (
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
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_user_messages_user_created (user_id, created_at),
    INDEX idx_user_messages_curation_result (curation_result_id),
    CONSTRAINT fk_user_messages_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_messages_flower FOREIGN KEY (flower_id) REFERENCES flowers (id),
    CONSTRAINT fk_user_messages_curation_result FOREIGN KEY (curation_result_id) REFERENCES user_curation_results (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
