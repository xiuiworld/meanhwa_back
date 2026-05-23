package com.example.meanhwa_back;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@EnabledIfEnvironmentVariable(named = "ENABLE_MYSQL_FLYWAY_TESTS", matches = "true")
class FlywayMysqlMigrationIntegrationTest {
    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("meanhwa")
            .withUsername("meanhwa")
            .withPassword("meanhwa");

    @Test
    void flywayMigrationsCreateSchemaAndHibernateValidatesAgainstMysql() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(MeanhwaBackApplication.class)
                .properties(
                        "spring.datasource.url=" + MYSQL.getJdbcUrl(),
                        "spring.datasource.username=" + MYSQL.getUsername(),
                        "spring.datasource.password=" + MYSQL.getPassword(),
                        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
                        "spring.flyway.enabled=true",
                        "spring.flyway.locations=classpath:db/migration/mysql",
                        "spring.flyway.baseline-on-migrate=false",
                        "spring.jpa.hibernate.ddl-auto=validate",
                        "spring.sql.init.mode=never",
                        "app.storage.type=fake",
                        "app.message.rate-limit.store=memory",
                        "app.jwt.secret=test-jwt-secret-for-meanhwa-mysql-flyway-validation"
                )
                .run()) {
            JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);

            Integer successfulMigrations = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1",
                    Integer.class
            );
            Integer windowBrightTags = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM tags WHERE code = 'WINDOW_BRIGHT' AND deleted_at IS NULL",
                    Integer.class
            );

            assertThat(successfulMigrations).isGreaterThanOrEqualTo(2);
            assertThat(windowBrightTags).isEqualTo(1);
        }
    }
}
