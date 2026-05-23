-- Drop flower fields that are no longer part of the public/admin API contract.
-- Idempotent guards let fresh databases that already use the trimmed V1 schema
-- pass through this migration without failing.

SET @db := DATABASE();

SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'flowers' AND COLUMN_NAME = 'scientific_name');
SET @ddl := IF(@has_col = 1, 'ALTER TABLE flowers DROP COLUMN scientific_name', 'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'flowers' AND COLUMN_NAME = 'origin');
SET @ddl := IF(@has_col = 1, 'ALTER TABLE flowers DROP COLUMN origin', 'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'flowers' AND COLUMN_NAME = 'blooming_season');
SET @ddl := IF(@has_col = 1, 'ALTER TABLE flowers DROP COLUMN blooming_season', 'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'flowers' AND COLUMN_NAME = 'scent');
SET @ddl := IF(@has_col = 1, 'ALTER TABLE flowers DROP COLUMN scent', 'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'flowers' AND COLUMN_NAME = 'management_info');
SET @ddl := IF(@has_col = 1, 'ALTER TABLE flowers DROP COLUMN management_info', 'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
