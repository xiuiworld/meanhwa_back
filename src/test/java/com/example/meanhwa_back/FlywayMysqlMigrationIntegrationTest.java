package com.example.meanhwa_back;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.meanhwa_back.curation.wizard.config.CurationFlowCatalog;
import com.example.meanhwa_back.curation.wizard.config.CurationFlowDocument;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
/**
 * MySQL Testcontainers로 Flyway 마이그레이션과 JPA validate를 검증하는 통합 테스트.
 * 운영 DB에 가까운 환경에서 마이그레이션 누락과 enum 컬럼 불일치를 배포 전에 잡는다.
 */
@Testcontainers
@EnabledIfEnvironmentVariable(named = "ENABLE_MYSQL_FLYWAY_TESTS", matches = "true")
class FlywayMysqlMigrationIntegrationTest {
    @Container
    private final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("meanhwa")
            .withUsername("meanhwa")
            .withPassword("meanhwa");

    @Test
    void flywayMigrationsCreateSchemaAndHibernateValidatesAgainstMysql() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(MeanhwaBackApplication.class)
                .run(
                        "--spring.datasource.url=" + mysql.getJdbcUrl(),
                        "--spring.datasource.username=" + mysql.getUsername(),
                        "--spring.datasource.password=" + mysql.getPassword(),
                        "--spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
                        "--spring.flyway.enabled=true",
                        "--spring.flyway.locations=classpath:db/migration/mysql",
                        "--spring.flyway.baseline-on-migrate=false",
                        "--spring.jpa.hibernate.ddl-auto=validate",
                        "--spring.sql.init.mode=never",
                        "--app.storage.type=fake",
                        "--app.message.rate-limit.store=memory",
                        "--app.jwt.secret=test-jwt-secret-for-meanhwa-mysql-flyway-validation"
                )) {
            JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
            Set<String> requiredScoringCodes = requiredScoringCodes();

            Integer successfulMigrations = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1",
                    Integer.class
            );
            Integer activeRequiredTags = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM tags WHERE deleted_at IS NULL AND code IN ("
                            + placeholders(requiredScoringCodes.size()) + ")",
                    Integer.class,
                    requiredScoringCodes.toArray()
            );

            assertThat(successfulMigrations).isGreaterThanOrEqualTo(3);
            assertThat(activeRequiredTags).isEqualTo(requiredScoringCodes.size());
        }
    }

    @Test
    void referenceDataMigrationCopiesLegacyMappingsIntoWizardTags() {
        migrateToVersionOne();
        JdbcTemplate jdbcTemplate = jdbcTemplate();

        jdbcTemplate.update("""
                INSERT INTO flowers (
                    name, image_url, core_meaning, management_level,
                    is_toxic_to_pets, price_range, created_at, updated_at
                )
                VALUES (
                    'legacy rose', 'https://example.com/legacy-rose.jpg', 'legacy',
                    'EASY', false, 'LOW', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
                )
                """);
        jdbcTemplate.update("""
                INSERT INTO tags (category, name, code)
                VALUES
                    ('EVENT', '생일', NULL),
                    ('RELATION', '부모님', NULL),
                    ('RELATION', '동료', NULL),
                    ('RELATION', '친구', NULL),
                    ('EMOTION', '위로', NULL),
                    ('EMOTION', '응원', NULL),
                    ('EMOTION', '감사', NULL),
                    ('EMOTION', '사랑', NULL),
                    ('ENVIRONMENT', '실내', NULL)
                """);
        jdbcTemplate.update("""
                INSERT INTO flower_tag_mappings (flower_id, tag_id, weight)
                SELECT f.id, t.id, 5
                FROM flowers f
                CROSS JOIN tags t
                WHERE f.name = 'legacy rose'
                """);

        migrateToLatest();

        List<String> copiedCodes = List.of("WEDDING", "RECOVERY", "FAMILY", "WINDOW_BRIGHT", "LOVE_1", "GET_WELL_4");
        Integer copiedMappingCodes = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT t.code) "
                        + "FROM flower_tag_mappings m "
                        + "INNER JOIN tags t ON t.id = m.tag_id "
                        + "WHERE t.code IN (" + placeholders(copiedCodes.size()) + ")",
                Integer.class,
                copiedCodes.toArray()
        );

        assertThat(copiedMappingCodes).isEqualTo(copiedCodes.size());
    }

    private void migrateToVersionOne() {
        Flyway.configure()
                .dataSource(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword())
                .locations("classpath:db/migration/mysql")
                .target("1")
                .load()
                .migrate();
    }

    private void migrateToLatest() {
        Flyway.configure()
                .dataSource(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword())
                .locations("classpath:db/migration/mysql")
                .load()
                .migrate();
    }

    private JdbcTemplate jdbcTemplate() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                mysql.getJdbcUrl(),
                mysql.getUsername(),
                mysql.getPassword()
        );
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return new JdbcTemplate(dataSource);
    }

    private static Set<String> requiredScoringCodes() {
        CurationFlowDocument document = new CurationFlowCatalog().getDocument();
        Set<String> codes = new LinkedHashSet<>();
        document.getOccasionOptions().forEach(option -> codes.add(option.getCode()));
        document.getRecipientBranches().values().stream()
                .flatMap(Collection::stream)
                .forEach(option -> codes.add(option.getCode()));
        document.getEmotionBranches().values().stream()
                .flatMap(Collection::stream)
                .forEach(option -> codes.add(option.getCode()));
        document.getFlowerMeaningLabels().forEach((emotionCode, labels) -> {
            for (int index = 0; index < labels.size(); index++) {
                codes.add(emotionCode + "_" + (index + 1));
            }
        });
        document.getSpaceOptions().forEach(option -> codes.add(option.getCode()));
        return codes;
    }

    private static String placeholders(int count) {
        return java.util.stream.Stream.generate(() -> "?")
                .limit(count)
                .collect(Collectors.joining(","));
    }
}
