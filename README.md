# Meanhwa Backend

꽃 도감, 큐레이션, 메시지 생성, 소셜 로그인, 마이페이지, 관리자 CMS를 제공하는 Spring Boot REST API입니다.

## Stack

- Java 21, Gradle 8.14
- Spring Boot 3.5, Web, Validation, Security, AOP, Actuator
- Spring Data JPA, Flyway
- H2(local/test), MySQL(prod)
- JWT access/refresh token, Kakao/Naver OAuth
- OpenAI 메시지 생성 + 템플릿 fallback
- Redis cache/rate limit, AWS S3 image upload
- Docker, GitHub Actions

## Local Run

기본 profile은 `local`입니다. 로컬은 H2, fake storage, simple cache, memory rate limit을 사용합니다.

```powershell
$env:JAVA_HOME='C:\Users\xiuiw\.jdks\corretto-21.0.11'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat bootRun
```

API base URL:

```text
http://localhost:8080/api/v1
```

테스트와 빌드:

```powershell
.\gradlew.bat test
.\gradlew.bat bootJar
```

MySQL Flyway 검증은 Docker가 켜진 상태에서 실행합니다.

```powershell
$env:ENABLE_MYSQL_FLYWAY_TESTS='true'
.\gradlew.bat test --tests "*FlywayMysqlMigrationIntegrationTest" --rerun-tasks
```

## Profiles

| Profile | DB | Storage | Cache | Rate limit |
| --- | --- | --- | --- | --- |
| `local` | H2 | fake | simple | memory |
| `test` | H2 in-memory + `data.sql` | fake | simple | memory |
| `prod` | MySQL + Flyway | S3 | simple/Redis | Redis |

`Dockerfile`은 `SPRING_PROFILES_ACTIVE=prod`로 실행합니다.

## API Areas

모든 애플리케이션 API는 `/api/v1` 아래에 있습니다.

| Area | Paths |
| --- | --- |
| Auth | `/auth/login/{provider}`, `/auth/refresh`, `/auth/logout` |
| Flowers | `/flowers`, `/flowers/{flowerId}` |
| Tags | `/tags` |
| Curation | `/curation/results` |
| Messages | `/messages/generate`, `/users/me/messages` |
| My page | `/users/me`, `/users/me/likes`, `/users/me/histories`, `/users/me/curation-results` |
| Admin | `/admin/flowers`, `/admin/tags`, `/admin/uploads/images`, `/admin/users`, `/admin/statistics` |
| Logs | `/action-logs/curation-result-click` |

상세 계약은 [docs/api-contract.md](docs/api-contract.md)를 봅니다.

## Docs

- [docs/api-contract.md](docs/api-contract.md): 프론트 연동용 API 계약
- [docs/curation-wizard-api.md](docs/curation-wizard-api.md): 큐레이션 `flowVersion`, `step`, `code` 표
- [docs/operations.md](docs/operations.md): 운영 env, 배포, Flyway, 장애 확인

DB 스키마의 원본은 JPA 엔티티와 `src/main/resources/db/migration/mysql/V*.sql`입니다. 별도 장문 스키마 문서는 유지하지 않습니다.

## Production

- `main` push 시 `.github/workflows/deploy.yml`이 테스트, Flyway MySQL 검증, Docker build/push, EC2 배포를 수행합니다.
- prod는 `spring.sql.init.mode=never`, `spring.jpa.hibernate.ddl-auto=validate`, `spring.flyway.enabled=true`가 기본입니다.
- 운영 secret은 GitHub Actions Secrets 또는 서버 env에만 둡니다.
