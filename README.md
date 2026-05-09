# Meanhwa Backend

Meanhwa backend is a Spring Boot REST API for flower/plant curation, message generation, social login, personalization, admin CMS, action logging, statistics, image upload, caching, and production deployment.

## Tech Stack

- Java 21
- Spring Boot 3.5
- Spring Web, Validation, Security
- Spring Data JPA
- MySQL for production
- H2 for local/test
- Redis cache support
- AWS S3 image storage support
- JWT access/refresh tokens
- Kakao/Naver OAuth profile verification
- OpenAI message generation with template fallback
- Docker and GitHub Actions

## Local Requirements

Use JDK 21. On the current Windows development machine:

```powershell
$env:JAVA_HOME='C:\Users\xiuiw\.jdks\corretto-21.0.11'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

## Run

The default profile is `local`.

```powershell
.\gradlew.bat bootRun
```

Server:

```text
http://localhost:8080
```

## Test and Build

```powershell
.\gradlew.bat test --rerun-tasks
.\gradlew.bat bootJar
```

Linux/CI equivalent:

```bash
./gradlew test bootJar
```

## Profiles

| Profile | Purpose | DB | Storage | Cache |
| --- | --- | --- | --- | --- |
| `local` | Local development | H2/default local config | fake | simple |
| `test` | Automated tests | H2 in-memory | fake | simple |
| `prod` | Production | MySQL/RDS | S3 | simple or Redis |

## Main API Areas

- Public flower dictionary: `GET /api/v1/flowers`
- Public tag list: `GET /api/v1/tags`
- Public curation: `GET /api/v1/curation`
- Message generation: `POST /api/v1/messages/generate`
- Auth: `POST /api/v1/auth/login/{provider}`
- My page: `/api/v1/users/me/**`
- Admin CMS: `/api/v1/admin/**`
- Action logs: `/api/v1/action-logs/**`
- Admin statistics: `/api/v1/admin/statistics/**`

## Important Documents

- [API contract](docs/api-contract.md)
- [Database schema](docs/database-schema.md)
- [Deployment guide](docs/deployment.md)
- [Environment variables](docs/env-vars.md)
- [Admin guide](docs/admin-guide.md)
- [Troubleshooting](docs/troubleshooting.md)

## Production Notes

- Production deployment runs through GitHub Actions on `main`.
- The deploy workflow runs tests before building and deploying.
- The backend container is named `meanhwa-server`.
- Redis container is named `meanhwa-redis`.
- DB is configured through `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
- Sensitive values must be stored as GitHub Actions Secrets or server environment variables, never committed to source.
