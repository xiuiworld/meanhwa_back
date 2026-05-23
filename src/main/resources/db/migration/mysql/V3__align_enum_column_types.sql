-- Align Flyway-managed MySQL schema with Hibernate 6 native enum validation.
-- Existing production databases that were baselined at V1 will receive this as
-- a normal migration; fresh databases receive it after V1/V2.

ALTER TABLE users
    MODIFY provider ENUM('DEV','KAKAO','NAVER') NOT NULL,
    MODIFY role ENUM('ROLE_ADMIN','ROLE_USER') NOT NULL;

ALTER TABLE flowers
    MODIFY management_level ENUM('EASY','HARD','NORMAL') NOT NULL,
    MODIFY price_range ENUM('HIGH','LOW','MEDIUM','PREMIUM') NOT NULL;

ALTER TABLE tags
    MODIFY category ENUM('CARE','EMOTION','ENVIRONMENT','EVENT','MEANING','RELATION','SEASON','STYLE') NOT NULL;

ALTER TABLE action_logs
    MODIFY action_type ENUM(
        'ADMIN_USER_ROLE_CHANGE',
        'CURATION_RESULT_CLICK',
        'CURATION_START',
        'DICTIONARY_SEARCH',
        'FLOWER_DETAIL_VIEW'
    ) NOT NULL;
