-- Curation wizard reference data.
-- This migration is intentionally image-free: flowers/content images remain CMS/S3-managed.

SET @db := DATABASE();

SET @has_code_col := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'tags'
      AND COLUMN_NAME = 'code'
);
SET @ddl := IF(
    @has_code_col = 0,
    'ALTER TABLE tags ADD COLUMN code VARCHAR(80) NULL',
    'SELECT ''tags.code already exists'' AS info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE tags SET code = 'BIRTHDAY' WHERE category = 'EVENT' AND name = '생일' AND deleted_at IS NULL AND (code IS NULL OR code = '');
UPDATE tags SET code = 'GRADUATION' WHERE category = 'EVENT' AND name = '졸업' AND deleted_at IS NULL AND (code IS NULL OR code = '');
UPDATE tags SET code = 'HOUSEWARMING' WHERE category = 'EVENT' AND name = '집들이' AND deleted_at IS NULL AND (code IS NULL OR code = '');
UPDATE tags SET code = 'PROMOTION' WHERE category = 'EVENT' AND name = '승진' AND deleted_at IS NULL AND (code IS NULL OR code = '');
UPDATE tags SET code = 'LOVER' WHERE category = 'RELATION' AND name = '연인' AND deleted_at IS NULL AND (code IS NULL OR code = '');
UPDATE tags SET code = 'FRIEND' WHERE category = 'RELATION' AND name = '친구' AND deleted_at IS NULL AND (code IS NULL OR code = '');
UPDATE tags SET code = 'PARENT' WHERE category = 'RELATION' AND name = '부모님' AND deleted_at IS NULL AND (code IS NULL OR code = '');
UPDATE tags SET code = 'COLLEAGUE' WHERE category = 'RELATION' AND name = '동료' AND deleted_at IS NULL AND (code IS NULL OR code = '');
UPDATE tags SET code = 'LOVE' WHERE category = 'EMOTION' AND name = '사랑' AND deleted_at IS NULL AND (code IS NULL OR code = '');
UPDATE tags SET code = 'GRATITUDE' WHERE category = 'EMOTION' AND name = '감사' AND deleted_at IS NULL AND (code IS NULL OR code = '');
UPDATE tags SET code = 'COMFORT' WHERE category = 'EMOTION' AND name = '위로' AND deleted_at IS NULL AND (code IS NULL OR code = '');
UPDATE tags SET code = 'SUPPORT' WHERE category = 'EMOTION' AND name = '응원' AND deleted_at IS NULL AND (code IS NULL OR code = '');
UPDATE tags SET code = 'BALCONY_OUTDOOR' WHERE category = 'ENVIRONMENT' AND name = '실외' AND deleted_at IS NULL AND (code IS NULL OR code = '');
UPDATE tags SET code = 'DESK_SMALL' WHERE category = 'ENVIRONMENT' AND name = '책상' AND deleted_at IS NULL AND (code IS NULL OR code = '');
UPDATE tags SET code = 'LIVING_ROOM' WHERE category = 'ENVIRONMENT' AND name = '거실' AND deleted_at IS NULL AND (code IS NULL OR code = '');

SET @has_code_uk := (
    SELECT COUNT(*)
    FROM (
        SELECT INDEX_NAME
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = @db
          AND TABLE_NAME = 'tags'
          AND NON_UNIQUE = 0
        GROUP BY INDEX_NAME
        HAVING COUNT(*) = 1
           AND SUM(CASE WHEN COLUMN_NAME = 'code' THEN 1 ELSE 0 END) = 1
    ) code_unique_indexes
);
SET @ddl := IF(
    @has_code_uk = 0,
    'CREATE UNIQUE INDEX uk_tags_code ON tags (code)',
    'SELECT ''uk_tags_code already exists'' AS info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TEMPORARY TABLE curation_wizard_required_tags (
    category VARCHAR(50) NOT NULL,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(80) NOT NULL PRIMARY KEY
);

INSERT INTO curation_wizard_required_tags (category, name, code)
VALUES
    ('EVENT', '생일', 'BIRTHDAY'),
    ('EVENT', '졸업', 'GRADUATION'),
    ('EVENT', '집들이', 'HOUSEWARMING'),
    ('EVENT', '승진', 'PROMOTION'),
    ('EVENT', '결혼', 'WEDDING'),
    ('EVENT', '병문안·회복', 'RECOVERY'),
    ('RELATION', '연인', 'LOVER'),
    ('RELATION', '친구', 'FRIEND'),
    ('RELATION', '부모님', 'PARENT'),
    ('RELATION', '동료', 'COLLEAGUE'),
    ('RELATION', '가족', 'FAMILY'),
    ('RELATION', '선후배·스승', 'SENIOR_JUNIOR_MENTOR'),
    ('RELATION', '동료·후배', 'COLLEAGUE_JUNIOR'),
    ('RELATION', '상사·선배', 'BOSS_SENIOR'),
    ('RELATION', '친구·지인', 'FRIEND_ACQUAINTANCE'),
    ('RELATION', '은사·귀빈', 'VIP_MENTOR'),
    ('EMOTION', '사랑', 'LOVE'),
    ('EMOTION', '감사', 'GRATITUDE'),
    ('EMOTION', '위로', 'COMFORT'),
    ('EMOTION', '응원', 'SUPPORT'),
    ('EMOTION', '축하', 'CELEBRATION'),
    ('EMOTION', '격려', 'ENCOURAGEMENT'),
    ('EMOTION', '도약', 'LEAP'),
    ('EMOTION', '존경', 'RESPECT'),
    ('EMOTION', '자부심', 'PRIDE'),
    ('EMOTION', '축복', 'BLESSING'),
    ('EMOTION', '진심', 'SINCERITY'),
    ('EMOTION', '영원', 'ETERNITY'),
    ('EMOTION', '평온', 'PEACE'),
    ('EMOTION', '번창', 'PROSPERITY'),
    ('EMOTION', '쾌유', 'GET_WELL'),
    ('ENVIRONMENT', '실외', 'BALCONY_OUTDOOR'),
    ('ENVIRONMENT', '책상', 'DESK_SMALL'),
    ('ENVIRONMENT', '거실', 'LIVING_ROOM'),
    ('ENVIRONMENT', '창가·밝은 실내', 'WINDOW_BRIGHT'),
    ('MEANING', '변함없는 마음', 'LOVE_1'),
    ('MEANING', '첫사랑의 설렘', 'LOVE_2'),
    ('MEANING', '소중한 당신', 'LOVE_3'),
    ('MEANING', '진실한 사랑', 'LOVE_4'),
    ('MEANING', '언제나 응원해', 'SUPPORT_1'),
    ('MEANING', '변치 않는 우정', 'SUPPORT_2'),
    ('MEANING', '찬란한 미소', 'SUPPORT_3'),
    ('MEANING', '매일의 행복', 'SUPPORT_4'),
    ('MEANING', '용기와 자신감', 'ENCOURAGEMENT_1'),
    ('MEANING', '새로운 도전', 'ENCOURAGEMENT_2'),
    ('MEANING', '당당한 발걸음', 'ENCOURAGEMENT_3'),
    ('MEANING', '무한한 가능성', 'ENCOURAGEMENT_4'),
    ('MEANING', '화사한 축하', 'CELEBRATION_1'),
    ('MEANING', '빛나는 성취', 'CELEBRATION_2'),
    ('MEANING', '새로운 시작', 'CELEBRATION_3'),
    ('MEANING', '함께한 기쁨', 'CELEBRATION_4'),
    ('MEANING', '진심 어린 고마움', 'GRATITUDE_1'),
    ('MEANING', '함께해서 행복', 'GRATITUDE_2'),
    ('MEANING', '오래된 인연', 'GRATITUDE_3'),
    ('MEANING', '따뜻한 기억', 'GRATITUDE_4'),
    ('MEANING', '행복한 시작', 'BLESSING_1'),
    ('MEANING', '아름다운 인연', 'BLESSING_2'),
    ('MEANING', '아낌없는 축복', 'BLESSING_3'),
    ('MEANING', '조화와 화합', 'BLESSING_4'),
    ('MEANING', '진심을 담아', 'SINCERITY_1'),
    ('MEANING', '소중한 인연', 'SINCERITY_2'),
    ('MEANING', '영원한 약속', 'SINCERITY_3'),
    ('MEANING', '고귀한 사랑', 'SINCERITY_4'),
    ('MEANING', '영원히 하나됨', 'ETERNITY_1'),
    ('MEANING', '영원한 사랑', 'ETERNITY_2'),
    ('MEANING', '아름다운 시작', 'ETERNITY_3'),
    ('MEANING', '고귀한 인연', 'ETERNITY_4'),
    ('MEANING', '깊은 존경', 'RESPECT_1'),
    ('MEANING', '명예와 인정', 'RESPECT_2'),
    ('MEANING', '굳건한 신뢰', 'RESPECT_3'),
    ('MEANING', '탄탄대로', 'RESPECT_4'),
    ('MEANING', '값진 노력', 'PRIDE_1'),
    ('MEANING', '빛나는 성공', 'PRIDE_2'),
    ('MEANING', '끊임없는 성장', 'PRIDE_3'),
    ('MEANING', '위풍당당', 'PRIDE_4'),
    ('MEANING', '희망과 도약', 'LEAP_1'),
    ('MEANING', '밝은 앞날', 'LEAP_2'),
    ('MEANING', '무한한 가능성', 'LEAP_3'),
    ('MEANING', '꿈을 향해', 'LEAP_4'),
    ('MEANING', '평온한 일상', 'PEACE_1'),
    ('MEANING', '편안한 공간', 'PEACE_2'),
    ('MEANING', '마음의 안정', 'PEACE_3'),
    ('MEANING', '따뜻한 온기', 'PEACE_4'),
    ('MEANING', '풍요와 번영', 'PROSPERITY_1'),
    ('MEANING', '피어나는 기쁨', 'PROSPERITY_2'),
    ('MEANING', '번창하는 일상', 'PROSPERITY_3'),
    ('MEANING', '가정의 행복', 'PROSPERITY_4'),
    ('MEANING', '따뜻한 위안', 'COMFORT_1'),
    ('MEANING', '깊은 배려', 'COMFORT_2'),
    ('MEANING', '평온한 휴식', 'COMFORT_3'),
    ('MEANING', '마음의 안계', 'COMFORT_4'),
    ('MEANING', '빠른 회복', 'GET_WELL_1'),
    ('MEANING', '다시 찾은 활력', 'GET_WELL_2'),
    ('MEANING', '건강한 내일', 'GET_WELL_3'),
    ('MEANING', '희망의 빛', 'GET_WELL_4');
INSERT INTO tags (category, name, code)
SELECT category, name, code
FROM curation_wizard_required_tags
ON DUPLICATE KEY UPDATE
    category = VALUES(category),
    name = VALUES(name),
    deleted_at = NULL;

SET @missing_required_code_count := (
    SELECT COUNT(*)
    FROM curation_wizard_required_tags req
    LEFT JOIN tags t
        ON t.code = req.code
        AND t.deleted_at IS NULL
    WHERE t.id IS NULL
);
CREATE TEMPORARY TABLE curation_wizard_required_code_validation (
    must_be_zero INT NOT NULL,
    CONSTRAINT chk_curation_wizard_required_code_validation CHECK (must_be_zero = 0)
);
INSERT INTO curation_wizard_required_code_validation (must_be_zero)
SELECT @missing_required_code_count
WHERE @missing_required_code_count > 0;
DROP TEMPORARY TABLE curation_wizard_required_code_validation;

CREATE TEMPORARY TABLE curation_wizard_tag_mapping_sources (
    new_code VARCHAR(80) NOT NULL PRIMARY KEY,
    source_code VARCHAR(80) NULL,
    source_category VARCHAR(50) NULL,
    source_name VARCHAR(50) NULL
);

INSERT INTO curation_wizard_tag_mapping_sources (new_code, source_code, source_category, source_name)
VALUES
    ('WEDDING', 'BIRTHDAY', NULL, NULL),
    ('RECOVERY', 'COMFORT', NULL, NULL),
    ('FAMILY', 'PARENT', NULL, NULL),
    ('SENIOR_JUNIOR_MENTOR', 'COLLEAGUE', NULL, NULL),
    ('COLLEAGUE_JUNIOR', 'COLLEAGUE', NULL, NULL),
    ('BOSS_SENIOR', 'COLLEAGUE', NULL, NULL),
    ('FRIEND_ACQUAINTANCE', 'FRIEND', NULL, NULL),
    ('VIP_MENTOR', 'PARENT', NULL, NULL),
    ('CELEBRATION', 'SUPPORT', NULL, NULL),
    ('ENCOURAGEMENT', 'SUPPORT', NULL, NULL),
    ('LEAP', 'SUPPORT', NULL, NULL),
    ('RESPECT', 'GRATITUDE', NULL, NULL),
    ('PRIDE', 'SUPPORT', NULL, NULL),
    ('BLESSING', 'LOVE', NULL, NULL),
    ('SINCERITY', 'LOVE', NULL, NULL),
    ('ETERNITY', 'LOVE', NULL, NULL),
    ('PEACE', 'LOVE', NULL, NULL),
    ('PROSPERITY', 'SUPPORT', NULL, NULL),
    ('GET_WELL', 'COMFORT', NULL, NULL),
    ('WINDOW_BRIGHT', NULL, 'ENVIRONMENT', '실내'),
    ('LOVE_1', 'LOVE', NULL, NULL),
    ('LOVE_2', 'LOVE', NULL, NULL),
    ('LOVE_3', 'LOVE', NULL, NULL),
    ('LOVE_4', 'LOVE', NULL, NULL),
    ('SUPPORT_1', 'SUPPORT', NULL, NULL),
    ('SUPPORT_2', 'SUPPORT', NULL, NULL),
    ('SUPPORT_3', 'SUPPORT', NULL, NULL),
    ('SUPPORT_4', 'SUPPORT', NULL, NULL),
    ('ENCOURAGEMENT_1', 'SUPPORT', NULL, NULL),
    ('ENCOURAGEMENT_2', 'SUPPORT', NULL, NULL),
    ('ENCOURAGEMENT_3', 'SUPPORT', NULL, NULL),
    ('ENCOURAGEMENT_4', 'SUPPORT', NULL, NULL),
    ('CELEBRATION_1', 'SUPPORT', NULL, NULL),
    ('CELEBRATION_2', 'SUPPORT', NULL, NULL),
    ('CELEBRATION_3', 'SUPPORT', NULL, NULL),
    ('CELEBRATION_4', 'SUPPORT', NULL, NULL),
    ('GRATITUDE_1', 'GRATITUDE', NULL, NULL),
    ('GRATITUDE_2', 'GRATITUDE', NULL, NULL),
    ('GRATITUDE_3', 'GRATITUDE', NULL, NULL),
    ('GRATITUDE_4', 'GRATITUDE', NULL, NULL),
    ('BLESSING_1', 'LOVE', NULL, NULL),
    ('BLESSING_2', 'LOVE', NULL, NULL),
    ('BLESSING_3', 'LOVE', NULL, NULL),
    ('BLESSING_4', 'LOVE', NULL, NULL),
    ('SINCERITY_1', 'LOVE', NULL, NULL),
    ('SINCERITY_2', 'LOVE', NULL, NULL),
    ('SINCERITY_3', 'LOVE', NULL, NULL),
    ('SINCERITY_4', 'LOVE', NULL, NULL),
    ('ETERNITY_1', 'LOVE', NULL, NULL),
    ('ETERNITY_2', 'LOVE', NULL, NULL),
    ('ETERNITY_3', 'LOVE', NULL, NULL),
    ('ETERNITY_4', 'LOVE', NULL, NULL),
    ('RESPECT_1', 'GRATITUDE', NULL, NULL),
    ('RESPECT_2', 'GRATITUDE', NULL, NULL),
    ('RESPECT_3', 'GRATITUDE', NULL, NULL),
    ('RESPECT_4', 'GRATITUDE', NULL, NULL),
    ('PRIDE_1', 'SUPPORT', NULL, NULL),
    ('PRIDE_2', 'SUPPORT', NULL, NULL),
    ('PRIDE_3', 'SUPPORT', NULL, NULL),
    ('PRIDE_4', 'SUPPORT', NULL, NULL),
    ('LEAP_1', 'SUPPORT', NULL, NULL),
    ('LEAP_2', 'SUPPORT', NULL, NULL),
    ('LEAP_3', 'SUPPORT', NULL, NULL),
    ('LEAP_4', 'SUPPORT', NULL, NULL),
    ('PEACE_1', 'LOVE', NULL, NULL),
    ('PEACE_2', 'LOVE', NULL, NULL),
    ('PEACE_3', 'LOVE', NULL, NULL),
    ('PEACE_4', 'LOVE', NULL, NULL),
    ('PROSPERITY_1', 'SUPPORT', NULL, NULL),
    ('PROSPERITY_2', 'SUPPORT', NULL, NULL),
    ('PROSPERITY_3', 'SUPPORT', NULL, NULL),
    ('PROSPERITY_4', 'SUPPORT', NULL, NULL),
    ('COMFORT_1', 'COMFORT', NULL, NULL),
    ('COMFORT_2', 'COMFORT', NULL, NULL),
    ('COMFORT_3', 'COMFORT', NULL, NULL),
    ('COMFORT_4', 'COMFORT', NULL, NULL),
    ('GET_WELL_1', 'COMFORT', NULL, NULL),
    ('GET_WELL_2', 'COMFORT', NULL, NULL),
    ('GET_WELL_3', 'COMFORT', NULL, NULL),
    ('GET_WELL_4', 'COMFORT', NULL, NULL);

SET @existing_flower_mapping_count := (SELECT COUNT(*) FROM flower_tag_mappings);

CREATE TEMPORARY TABLE curation_wizard_mapping_source_status AS
SELECT
    s.new_code,
    COUNT(DISTINCT m_src.id) AS source_mapping_count,
    COUNT(DISTINCT m_new.id) AS target_mapping_count
FROM curation_wizard_tag_mapping_sources s
INNER JOIN tags t_new
    ON t_new.code = s.new_code
    AND t_new.deleted_at IS NULL
LEFT JOIN tags t_src
    ON t_src.deleted_at IS NULL
    AND (
        (s.source_code IS NOT NULL AND t_src.code = s.source_code)
        OR (s.source_code IS NULL AND t_src.category = s.source_category AND t_src.name = s.source_name)
    )
LEFT JOIN flower_tag_mappings m_src
    ON m_src.tag_id = t_src.id
LEFT JOIN flower_tag_mappings m_new
    ON m_new.tag_id = t_new.id
GROUP BY s.new_code;

SET @missing_mapping_source_count := (
    SELECT COUNT(*)
    FROM curation_wizard_mapping_source_status
    WHERE @existing_flower_mapping_count > 0
      AND source_mapping_count = 0
      AND target_mapping_count = 0
);
CREATE TEMPORARY TABLE curation_wizard_mapping_source_validation (
    must_be_zero INT NOT NULL,
    CONSTRAINT chk_curation_wizard_mapping_source_validation CHECK (must_be_zero = 0)
);
INSERT INTO curation_wizard_mapping_source_validation (must_be_zero)
SELECT @missing_mapping_source_count
WHERE @missing_mapping_source_count > 0;
DROP TEMPORARY TABLE curation_wizard_mapping_source_validation;

CREATE TEMPORARY TABLE curation_wizard_flower_tag_mappings AS
SELECT m.flower_id, t_new.id AS tag_id, m.weight
FROM curation_wizard_tag_mapping_sources s
INNER JOIN tags t_new
    ON t_new.code = s.new_code
    AND t_new.deleted_at IS NULL
INNER JOIN tags t_src
    ON t_src.deleted_at IS NULL
    AND (
        (s.source_code IS NOT NULL AND t_src.code = s.source_code)
        OR (s.source_code IS NULL AND t_src.category = s.source_category AND t_src.name = s.source_name)
    )
INNER JOIN flower_tag_mappings m
    ON m.tag_id = t_src.id
LEFT JOIN flower_tag_mappings existing
    ON existing.flower_id = m.flower_id
    AND existing.tag_id = t_new.id
WHERE existing.id IS NULL;

INSERT INTO flower_tag_mappings (flower_id, tag_id, weight)
SELECT flower_id, tag_id, weight
FROM curation_wizard_flower_tag_mappings;

CREATE TEMPORARY TABLE curation_wizard_mapping_target_status AS
SELECT
    s.new_code,
    COUNT(DISTINCT m_new.id) AS target_mapping_count
FROM curation_wizard_tag_mapping_sources s
INNER JOIN tags t_new
    ON t_new.code = s.new_code
    AND t_new.deleted_at IS NULL
LEFT JOIN flower_tag_mappings m_new
    ON m_new.tag_id = t_new.id
GROUP BY s.new_code;

SET @missing_mapping_target_count := (
    SELECT COUNT(*)
    FROM curation_wizard_mapping_target_status
    WHERE @existing_flower_mapping_count > 0
      AND target_mapping_count = 0
);
CREATE TEMPORARY TABLE curation_wizard_mapping_target_validation (
    must_be_zero INT NOT NULL,
    CONSTRAINT chk_curation_wizard_mapping_target_validation CHECK (must_be_zero = 0)
);
INSERT INTO curation_wizard_mapping_target_validation (must_be_zero)
SELECT @missing_mapping_target_count
WHERE @missing_mapping_target_count > 0;
DROP TEMPORARY TABLE curation_wizard_mapping_target_validation;

DROP TEMPORARY TABLE curation_wizard_mapping_target_status;
DROP TEMPORARY TABLE curation_wizard_flower_tag_mappings;
DROP TEMPORARY TABLE curation_wizard_mapping_source_status;
DROP TEMPORARY TABLE curation_wizard_tag_mapping_sources;
DROP TEMPORARY TABLE curation_wizard_required_tags;
