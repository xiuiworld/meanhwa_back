-- =============================================================================
-- Meanhwa 운영 DB: 큐레이션 위저드 v2 (2026-05)
-- MySQL 8.x / RDS. 재실행 가능(멱등).
-- 적용 전 백업 필수. 실행 가이드: docs/migration/README.md
-- =============================================================================

SET NAMES utf8mb4;
SET @db := DATABASE();

-- -----------------------------------------------------------------------------
-- 1) tags.code 컬럼 (Hibernate ddl-auto=update 로 이미 있을 수 있음)
-- -----------------------------------------------------------------------------
SET @has_code_col := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'tags'
      AND COLUMN_NAME = 'code'
);
SET @ddl := IF(
    @has_code_col = 0,
    'ALTER TABLE tags ADD COLUMN code VARCHAR(80) NULL COMMENT ''위저드/API 식별 코드''',
    'SELECT ''tags.code already exists'' AS info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- code 유니크 인덱스 (없을 때만; NULL 은 MySQL 에서 여러 행 허용)
SET @has_code_uk := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'tags'
      AND INDEX_NAME = 'uk_tags_code'
);
SET @ddl := IF(
    @has_code_uk = 0,
    'CREATE UNIQUE INDEX uk_tags_code ON tags (code)',
    'SELECT ''uk_tags_code already exists'' AS info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -----------------------------------------------------------------------------
-- 2) 기존 태그 code 백필 (이름·카테고리 기준, deleted 제외)
-- -----------------------------------------------------------------------------
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

-- -----------------------------------------------------------------------------
-- 3) 위저드 신규 태그 INSERT (code 기준 멱등)
-- -----------------------------------------------------------------------------
INSERT INTO tags (category, name, code)
SELECT 'EVENT', '결혼', 'WEDDING' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'WEDDING' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code)
SELECT 'EVENT', '병문안·회복', 'RECOVERY' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'RECOVERY' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code)
SELECT 'RELATION', '가족', 'FAMILY' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'FAMILY' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code)
SELECT 'RELATION', '선후배·스승', 'SENIOR_JUNIOR_MENTOR' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'SENIOR_JUNIOR_MENTOR' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code)
SELECT 'RELATION', '동료·후배', 'COLLEAGUE_JUNIOR' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'COLLEAGUE_JUNIOR' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code)
SELECT 'RELATION', '상사·선배', 'BOSS_SENIOR' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'BOSS_SENIOR' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code)
SELECT 'RELATION', '친구·지인', 'FRIEND_ACQUAINTANCE' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'FRIEND_ACQUAINTANCE' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code)
SELECT 'RELATION', '은사·귀빈', 'VIP_MENTOR' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'VIP_MENTOR' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code)
SELECT 'EMOTION', '축하', 'CELEBRATION' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'CELEBRATION' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code)
SELECT 'EMOTION', '격려', 'ENCOURAGEMENT' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'ENCOURAGEMENT' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code)
SELECT 'EMOTION', '도약', 'LEAP' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'LEAP' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code)
SELECT 'EMOTION', '존경', 'RESPECT' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'RESPECT' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code)
SELECT 'EMOTION', '자부심', 'PRIDE' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'PRIDE' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code)
SELECT 'EMOTION', '축복', 'BLESSING' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'BLESSING' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code)
SELECT 'EMOTION', '진심', 'SINCERITY' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'SINCERITY' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code)
SELECT 'EMOTION', '영원', 'ETERNITY' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'ETERNITY' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code)
SELECT 'EMOTION', '평온', 'PEACE' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'PEACE' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code)
SELECT 'EMOTION', '번창', 'PROSPERITY' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'PROSPERITY' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code)
SELECT 'EMOTION', '쾌유', 'GET_WELL' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'GET_WELL' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code)
SELECT 'ENVIRONMENT', '창가·밝은 실내', 'WINDOW_BRIGHT' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'WINDOW_BRIGHT' AND deleted_at IS NULL);

-- MEANING (꽃말 결) — 표시명은 flow-2026-05-v1.yml / data.sql 과 동일 (PDF 2026-05)
INSERT INTO tags (category, name, code) SELECT 'MEANING', '변함없는 마음', 'LOVE_1' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'LOVE_1' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '첫사랑의 설렘', 'LOVE_2' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'LOVE_2' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '소중한 당신', 'LOVE_3' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'LOVE_3' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '진실한 사랑', 'LOVE_4' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'LOVE_4' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '언제나 응원해', 'SUPPORT_1' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'SUPPORT_1' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '변치 않는 우정', 'SUPPORT_2' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'SUPPORT_2' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '찬란한 미소', 'SUPPORT_3' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'SUPPORT_3' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '매일의 행복', 'SUPPORT_4' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'SUPPORT_4' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '용기와 자신감', 'ENCOURAGEMENT_1' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'ENCOURAGEMENT_1' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '새로운 도전', 'ENCOURAGEMENT_2' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'ENCOURAGEMENT_2' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '당당한 발걸음', 'ENCOURAGEMENT_3' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'ENCOURAGEMENT_3' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '무한한 가능성', 'ENCOURAGEMENT_4' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'ENCOURAGEMENT_4' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '화사한 축하', 'CELEBRATION_1' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'CELEBRATION_1' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '빛나는 성취', 'CELEBRATION_2' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'CELEBRATION_2' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '새로운 시작', 'CELEBRATION_3' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'CELEBRATION_3' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '함께한 기쁨', 'CELEBRATION_4' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'CELEBRATION_4' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '진심 어린 고마움', 'GRATITUDE_1' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'GRATITUDE_1' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '함께해서 행복', 'GRATITUDE_2' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'GRATITUDE_2' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '오래된 인연', 'GRATITUDE_3' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'GRATITUDE_3' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '따뜻한 기억', 'GRATITUDE_4' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'GRATITUDE_4' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '행복한 시작', 'BLESSING_1' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'BLESSING_1' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '아름다운 인연', 'BLESSING_2' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'BLESSING_2' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '아낌없는 축복', 'BLESSING_3' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'BLESSING_3' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '조화와 화합', 'BLESSING_4' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'BLESSING_4' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '진심을 담아', 'SINCERITY_1' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'SINCERITY_1' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '소중한 인연', 'SINCERITY_2' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'SINCERITY_2' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '영원한 약속', 'SINCERITY_3' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'SINCERITY_3' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '고귀한 사랑', 'SINCERITY_4' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'SINCERITY_4' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '영원히 하나됨', 'ETERNITY_1' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'ETERNITY_1' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '영원한 사랑', 'ETERNITY_2' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'ETERNITY_2' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '아름다운 시작', 'ETERNITY_3' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'ETERNITY_3' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '고귀한 인연', 'ETERNITY_4' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'ETERNITY_4' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '깊은 존경', 'RESPECT_1' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'RESPECT_1' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '명예와 인정', 'RESPECT_2' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'RESPECT_2' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '굳건한 신뢰', 'RESPECT_3' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'RESPECT_3' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '탄탄대로', 'RESPECT_4' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'RESPECT_4' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '값진 노력', 'PRIDE_1' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'PRIDE_1' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '빛나는 성공', 'PRIDE_2' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'PRIDE_2' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '끊임없는 성장', 'PRIDE_3' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'PRIDE_3' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '위풍당당', 'PRIDE_4' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'PRIDE_4' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '희망과 도약', 'LEAP_1' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'LEAP_1' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '밝은 앞날', 'LEAP_2' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'LEAP_2' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '무한한 가능성', 'LEAP_3' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'LEAP_3' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '꿈을 향해', 'LEAP_4' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'LEAP_4' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '평온한 일상', 'PEACE_1' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'PEACE_1' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '편안한 공간', 'PEACE_2' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'PEACE_2' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '마음의 안정', 'PEACE_3' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'PEACE_3' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '따뜻한 온기', 'PEACE_4' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'PEACE_4' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '풍요와 번영', 'PROSPERITY_1' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'PROSPERITY_1' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '피어나는 기쁨', 'PROSPERITY_2' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'PROSPERITY_2' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '번창하는 일상', 'PROSPERITY_3' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'PROSPERITY_3' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '가정의 행복', 'PROSPERITY_4' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'PROSPERITY_4' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '따뜻한 위안', 'COMFORT_1' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'COMFORT_1' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '깊은 배려', 'COMFORT_2' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'COMFORT_2' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '평온한 휴식', 'COMFORT_3' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'COMFORT_3' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '마음의 안계', 'COMFORT_4' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'COMFORT_4' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '빠른 회복', 'GET_WELL_1' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'GET_WELL_1' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '다시 찾은 활력', 'GET_WELL_2' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'GET_WELL_2' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '건강한 내일', 'GET_WELL_3' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'GET_WELL_3' AND deleted_at IS NULL);
INSERT INTO tags (category, name, code) SELECT 'MEANING', '희망의 빛', 'GET_WELL_4' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tags WHERE code = 'GET_WELL_4' AND deleted_at IS NULL);

-- 이미 마이그레이션을 실행한 DB: MEANING 표시명만 PDF 문구로 갱신 (code 유지)
UPDATE tags SET name = '변함없는 마음' WHERE code = 'LOVE_1' AND deleted_at IS NULL;
UPDATE tags SET name = '진실한 사랑' WHERE code = 'LOVE_4' AND deleted_at IS NULL;
UPDATE tags SET name = '변치 않는 우정' WHERE code = 'SUPPORT_2' AND deleted_at IS NULL;
UPDATE tags SET name = '찬란한 미소' WHERE code = 'SUPPORT_3' AND deleted_at IS NULL;
UPDATE tags SET name = '매일의 행복' WHERE code = 'SUPPORT_4' AND deleted_at IS NULL;
UPDATE tags SET name = '당당한 발걸음' WHERE code = 'ENCOURAGEMENT_3' AND deleted_at IS NULL;
UPDATE tags SET name = '화사한 축하' WHERE code = 'CELEBRATION_1' AND deleted_at IS NULL;
UPDATE tags SET name = '함께한 기쁨' WHERE code = 'CELEBRATION_4' AND deleted_at IS NULL;
UPDATE tags SET name = '진심 어린 고마움' WHERE code = 'GRATITUDE_1' AND deleted_at IS NULL;
UPDATE tags SET name = '함께해서 행복' WHERE code = 'GRATITUDE_2' AND deleted_at IS NULL;
UPDATE tags SET name = '아낌없는 축복' WHERE code = 'BLESSING_3' AND deleted_at IS NULL;
UPDATE tags SET name = '영원히 하나됨' WHERE code = 'ETERNITY_1' AND deleted_at IS NULL;
UPDATE tags SET name = '명예와 인정' WHERE code = 'RESPECT_2' AND deleted_at IS NULL;
UPDATE tags SET name = '탄탄대로' WHERE code = 'RESPECT_4' AND deleted_at IS NULL;
UPDATE tags SET name = '빛나는 성공' WHERE code = 'PRIDE_2' AND deleted_at IS NULL;
UPDATE tags SET name = '밝은 앞날' WHERE code = 'LEAP_2' AND deleted_at IS NULL;
UPDATE tags SET name = '풍요와 번영' WHERE code = 'PROSPERITY_1' AND deleted_at IS NULL;
UPDATE tags SET name = '피어나는 기쁨' WHERE code = 'PROSPERITY_2' AND deleted_at IS NULL;
UPDATE tags SET name = '따뜻한 위안' WHERE code = 'COMFORT_1' AND deleted_at IS NULL;
UPDATE tags SET name = '평온한 휴식' WHERE code = 'COMFORT_3' AND deleted_at IS NULL;
UPDATE tags SET name = '마음의 안계' WHERE code = 'COMFORT_4' AND deleted_at IS NULL;
UPDATE tags SET name = '다시 찾은 활력' WHERE code = 'GET_WELL_2' AND deleted_at IS NULL;

-- -----------------------------------------------------------------------------
-- 4) flower_tag_mappings — 신규 tag code ← 기존 tag code weight 복제 (data.sql 과 동일 전략)
--    PROCEDURE: clone_mapping(new_code, source_code)
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS clone_mapping_by_code;

DELIMITER //
CREATE PROCEDURE clone_mapping_by_code(IN p_new_code VARCHAR(80), IN p_source_code VARCHAR(80))
BEGIN
    INSERT INTO flower_tag_mappings (flower_id, tag_id, weight)
    SELECT m.flower_id, t_new.id, m.weight
    FROM flower_tag_mappings m
    INNER JOIN tags t_src ON t_src.id = m.tag_id AND t_src.code = p_source_code AND t_src.deleted_at IS NULL
    INNER JOIN tags t_new ON t_new.code = p_new_code AND t_new.deleted_at IS NULL
    WHERE NOT EXISTS (
        SELECT 1 FROM flower_tag_mappings x
        WHERE x.flower_id = m.flower_id AND x.tag_id = t_new.id
    );
END //
DELIMITER ;

CALL clone_mapping_by_code('WEDDING', 'BIRTHDAY');
CALL clone_mapping_by_code('RECOVERY', 'COMFORT');
CALL clone_mapping_by_code('FAMILY', 'PARENT');
CALL clone_mapping_by_code('SENIOR_JUNIOR_MENTOR', 'COLLEAGUE');
CALL clone_mapping_by_code('COLLEAGUE_JUNIOR', 'COLLEAGUE');
CALL clone_mapping_by_code('BOSS_SENIOR', 'COLLEAGUE');
CALL clone_mapping_by_code('FRIEND_ACQUAINTANCE', 'FRIEND');
CALL clone_mapping_by_code('VIP_MENTOR', 'PARENT');
CALL clone_mapping_by_code('CELEBRATION', 'SUPPORT');
CALL clone_mapping_by_code('ENCOURAGEMENT', 'SUPPORT');
CALL clone_mapping_by_code('LEAP', 'SUPPORT');
CALL clone_mapping_by_code('RESPECT', 'GRATITUDE');
CALL clone_mapping_by_code('PRIDE', 'SUPPORT');
CALL clone_mapping_by_code('BLESSING', 'LOVE');
CALL clone_mapping_by_code('SINCERITY', 'LOVE');
CALL clone_mapping_by_code('ETERNITY', 'LOVE');
CALL clone_mapping_by_code('PEACE', 'LOVE');
CALL clone_mapping_by_code('PROSPERITY', 'SUPPORT');
CALL clone_mapping_by_code('GET_WELL', 'COMFORT');

CALL clone_mapping_by_code('LOVE_1', 'LOVE');
CALL clone_mapping_by_code('LOVE_2', 'LOVE');
CALL clone_mapping_by_code('LOVE_3', 'LOVE');
CALL clone_mapping_by_code('LOVE_4', 'LOVE');
CALL clone_mapping_by_code('SUPPORT_1', 'SUPPORT');
CALL clone_mapping_by_code('SUPPORT_2', 'SUPPORT');
CALL clone_mapping_by_code('SUPPORT_3', 'SUPPORT');
CALL clone_mapping_by_code('SUPPORT_4', 'SUPPORT');
CALL clone_mapping_by_code('ENCOURAGEMENT_1', 'SUPPORT');
CALL clone_mapping_by_code('ENCOURAGEMENT_2', 'SUPPORT');
CALL clone_mapping_by_code('ENCOURAGEMENT_3', 'SUPPORT');
CALL clone_mapping_by_code('ENCOURAGEMENT_4', 'SUPPORT');
CALL clone_mapping_by_code('CELEBRATION_1', 'SUPPORT');
CALL clone_mapping_by_code('CELEBRATION_2', 'SUPPORT');
CALL clone_mapping_by_code('CELEBRATION_3', 'SUPPORT');
CALL clone_mapping_by_code('CELEBRATION_4', 'SUPPORT');
CALL clone_mapping_by_code('GRATITUDE_1', 'GRATITUDE');
CALL clone_mapping_by_code('GRATITUDE_2', 'GRATITUDE');
CALL clone_mapping_by_code('GRATITUDE_3', 'GRATITUDE');
CALL clone_mapping_by_code('GRATITUDE_4', 'GRATITUDE');
CALL clone_mapping_by_code('BLESSING_1', 'LOVE');
CALL clone_mapping_by_code('BLESSING_2', 'LOVE');
CALL clone_mapping_by_code('BLESSING_3', 'LOVE');
CALL clone_mapping_by_code('BLESSING_4', 'LOVE');
CALL clone_mapping_by_code('SINCERITY_1', 'LOVE');
CALL clone_mapping_by_code('SINCERITY_2', 'LOVE');
CALL clone_mapping_by_code('SINCERITY_3', 'LOVE');
CALL clone_mapping_by_code('SINCERITY_4', 'LOVE');
CALL clone_mapping_by_code('ETERNITY_1', 'LOVE');
CALL clone_mapping_by_code('ETERNITY_2', 'LOVE');
CALL clone_mapping_by_code('ETERNITY_3', 'LOVE');
CALL clone_mapping_by_code('ETERNITY_4', 'LOVE');
CALL clone_mapping_by_code('RESPECT_1', 'GRATITUDE');
CALL clone_mapping_by_code('RESPECT_2', 'GRATITUDE');
CALL clone_mapping_by_code('RESPECT_3', 'GRATITUDE');
CALL clone_mapping_by_code('RESPECT_4', 'GRATITUDE');
CALL clone_mapping_by_code('PRIDE_1', 'SUPPORT');
CALL clone_mapping_by_code('PRIDE_2', 'SUPPORT');
CALL clone_mapping_by_code('PRIDE_3', 'SUPPORT');
CALL clone_mapping_by_code('PRIDE_4', 'SUPPORT');
CALL clone_mapping_by_code('LEAP_1', 'SUPPORT');
CALL clone_mapping_by_code('LEAP_2', 'SUPPORT');
CALL clone_mapping_by_code('LEAP_3', 'SUPPORT');
CALL clone_mapping_by_code('LEAP_4', 'SUPPORT');
CALL clone_mapping_by_code('PEACE_1', 'LOVE');
CALL clone_mapping_by_code('PEACE_2', 'LOVE');
CALL clone_mapping_by_code('PEACE_3', 'LOVE');
CALL clone_mapping_by_code('PEACE_4', 'LOVE');
CALL clone_mapping_by_code('PROSPERITY_1', 'SUPPORT');
CALL clone_mapping_by_code('PROSPERITY_2', 'SUPPORT');
CALL clone_mapping_by_code('PROSPERITY_3', 'SUPPORT');
CALL clone_mapping_by_code('PROSPERITY_4', 'SUPPORT');
CALL clone_mapping_by_code('COMFORT_1', 'COMFORT');
CALL clone_mapping_by_code('COMFORT_2', 'COMFORT');
CALL clone_mapping_by_code('COMFORT_3', 'COMFORT');
CALL clone_mapping_by_code('COMFORT_4', 'COMFORT');
CALL clone_mapping_by_code('GET_WELL_1', 'COMFORT');
CALL clone_mapping_by_code('GET_WELL_2', 'COMFORT');
CALL clone_mapping_by_code('GET_WELL_3', 'COMFORT');
CALL clone_mapping_by_code('GET_WELL_4', 'COMFORT');

-- WINDOW_BRIGHT ← 실내(ENVIRONMENT) 매핑 (source code 없음 → 이름 매칭)
INSERT INTO flower_tag_mappings (flower_id, tag_id, weight)
SELECT m.flower_id, t_new.id, m.weight
FROM flower_tag_mappings m
INNER JOIN tags t_src ON t_src.id = m.tag_id
    AND t_src.category = 'ENVIRONMENT' AND t_src.name = '실내' AND t_src.deleted_at IS NULL
INNER JOIN tags t_new ON t_new.code = 'WINDOW_BRIGHT' AND t_new.deleted_at IS NULL
WHERE NOT EXISTS (
    SELECT 1 FROM flower_tag_mappings x
    WHERE x.flower_id = m.flower_id AND x.tag_id = t_new.id
);

DROP PROCEDURE IF EXISTS clone_mapping_by_code;

-- -----------------------------------------------------------------------------
-- 5) 검증 — missing_wizard_codes 가 0건이어야 함
-- -----------------------------------------------------------------------------
SELECT req.code AS missing_wizard_code
FROM (
    SELECT 'BIRTHDAY' AS code UNION ALL SELECT 'GRADUATION' UNION ALL SELECT 'HOUSEWARMING' UNION ALL SELECT 'PROMOTION'
    UNION ALL SELECT 'LOVER' UNION ALL SELECT 'FRIEND' UNION ALL SELECT 'PARENT' UNION ALL SELECT 'COLLEAGUE'
    UNION ALL SELECT 'LOVE' UNION ALL SELECT 'GRATITUDE' UNION ALL SELECT 'COMFORT' UNION ALL SELECT 'SUPPORT'
    UNION ALL SELECT 'BALCONY_OUTDOOR' UNION ALL SELECT 'DESK_SMALL' UNION ALL SELECT 'LIVING_ROOM'
    UNION ALL SELECT 'WEDDING' UNION ALL SELECT 'RECOVERY' UNION ALL SELECT 'FAMILY'
    UNION ALL SELECT 'SENIOR_JUNIOR_MENTOR' UNION ALL SELECT 'COLLEAGUE_JUNIOR' UNION ALL SELECT 'BOSS_SENIOR'
    UNION ALL SELECT 'FRIEND_ACQUAINTANCE' UNION ALL SELECT 'VIP_MENTOR'
    UNION ALL SELECT 'CELEBRATION' UNION ALL SELECT 'ENCOURAGEMENT' UNION ALL SELECT 'LEAP'
    UNION ALL SELECT 'RESPECT' UNION ALL SELECT 'PRIDE' UNION ALL SELECT 'BLESSING'
    UNION ALL SELECT 'SINCERITY' UNION ALL SELECT 'ETERNITY' UNION ALL SELECT 'PEACE'
    UNION ALL SELECT 'PROSPERITY' UNION ALL SELECT 'GET_WELL' UNION ALL SELECT 'WINDOW_BRIGHT'
    UNION ALL SELECT 'LOVE_1' UNION ALL SELECT 'LOVE_2' UNION ALL SELECT 'LOVE_3' UNION ALL SELECT 'LOVE_4'
    UNION ALL SELECT 'SUPPORT_1' UNION ALL SELECT 'SUPPORT_2' UNION ALL SELECT 'SUPPORT_3' UNION ALL SELECT 'SUPPORT_4'
    UNION ALL SELECT 'ENCOURAGEMENT_1' UNION ALL SELECT 'ENCOURAGEMENT_2' UNION ALL SELECT 'ENCOURAGEMENT_3' UNION ALL SELECT 'ENCOURAGEMENT_4'
    UNION ALL SELECT 'CELEBRATION_1' UNION ALL SELECT 'CELEBRATION_2' UNION ALL SELECT 'CELEBRATION_3' UNION ALL SELECT 'CELEBRATION_4'
    UNION ALL SELECT 'GRATITUDE_1' UNION ALL SELECT 'GRATITUDE_2' UNION ALL SELECT 'GRATITUDE_3' UNION ALL SELECT 'GRATITUDE_4'
    UNION ALL SELECT 'BLESSING_1' UNION ALL SELECT 'BLESSING_2' UNION ALL SELECT 'BLESSING_3' UNION ALL SELECT 'BLESSING_4'
    UNION ALL SELECT 'SINCERITY_1' UNION ALL SELECT 'SINCERITY_2' UNION ALL SELECT 'SINCERITY_3' UNION ALL SELECT 'SINCERITY_4'
    UNION ALL SELECT 'ETERNITY_1' UNION ALL SELECT 'ETERNITY_2' UNION ALL SELECT 'ETERNITY_3' UNION ALL SELECT 'ETERNITY_4'
    UNION ALL SELECT 'RESPECT_1' UNION ALL SELECT 'RESPECT_2' UNION ALL SELECT 'RESPECT_3' UNION ALL SELECT 'RESPECT_4'
    UNION ALL SELECT 'PRIDE_1' UNION ALL SELECT 'PRIDE_2' UNION ALL SELECT 'PRIDE_3' UNION ALL SELECT 'PRIDE_4'
    UNION ALL SELECT 'LEAP_1' UNION ALL SELECT 'LEAP_2' UNION ALL SELECT 'LEAP_3' UNION ALL SELECT 'LEAP_4'
    UNION ALL SELECT 'PEACE_1' UNION ALL SELECT 'PEACE_2' UNION ALL SELECT 'PEACE_3' UNION ALL SELECT 'PEACE_4'
    UNION ALL SELECT 'PROSPERITY_1' UNION ALL SELECT 'PROSPERITY_2' UNION ALL SELECT 'PROSPERITY_3' UNION ALL SELECT 'PROSPERITY_4'
    UNION ALL SELECT 'COMFORT_1' UNION ALL SELECT 'COMFORT_2' UNION ALL SELECT 'COMFORT_3' UNION ALL SELECT 'COMFORT_4'
    UNION ALL SELECT 'GET_WELL_1' UNION ALL SELECT 'GET_WELL_2' UNION ALL SELECT 'GET_WELL_3' UNION ALL SELECT 'GET_WELL_4'
) req
LEFT JOIN tags t ON t.code = req.code AND t.deleted_at IS NULL
WHERE t.id IS NULL;

SELECT COUNT(*) AS wizard_tag_count FROM tags WHERE code IS NOT NULL AND deleted_at IS NULL;
