# Meanhwa Backend

Meanhwa backend is a Spring Boot REST API for flower dictionary search, six-step curation, message generation, social login, user personalization, admin CMS, action logging, image upload, caching, and deployment.

## Stack

- Java 21
- Spring Boot 3.5.14
- Spring Web, Validation, Security, AOP
- Spring Data JPA
- MySQL in production, H2 for local/test
- JWT access/refresh tokens
- Kakao/Naver OAuth profile verification
- OpenAI message generation with template fallback
- Redis cache and rate-limit support
- AWS S3 image storage support
- Docker and GitHub Actions

## Quick Start

Use JDK 21.

```powershell
$env:JAVA_HOME='C:\Users\xiuiw\.jdks\corretto-21.0.11'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat bootRun
```

The default profile is `local`, and the API starts on:

```text
http://localhost:8080
```

Run tests and build:

```powershell
.\gradlew.bat test --rerun-tasks
.\gradlew.bat bootJar
```

CI uses the Linux equivalent:

```bash
./gradlew test bootJar
```

## Profiles

| Profile | Purpose | Data/storage/cache |
| --- | --- | --- |
| `local` | Local development | H2, fake storage, simple cache, in-memory rate limit |
| `test` | Automated tests | H2 in-memory, `data.sql`, fake storage, simple cache |
| `prod` | Production runtime | MySQL/RDS, S3, Redis or simple cache, Redis rate limit |

`Dockerfile` runs the application with `SPRING_PROFILES_ACTIVE=prod`.

## API Areas

All application APIs are under `/api/v1`.

| Area | Main paths |
| --- | --- |
| Auth | `/auth/login/{provider}`, `/auth/refresh`, `/auth/logout` |
| Flower dictionary | `/flowers`, `/flowers/{flowerId}` |
| Tags | `/tags` |
| Curation | `/curation`, `/curation/flow`, `/curation/steps/{stepKey}/options`, `/curation/results` |
| Messages | `/messages/generate`, `/users/me/messages` |
| My page | `/users/me`, `/users/me/likes`, `/users/me/histories`, `/users/me/curation-results` |
| Admin CMS | `/admin/flowers`, `/admin/tags`, `/admin/uploads/images`, `/admin/users`, `/admin/statistics` |
| Action logs | `/action-logs/curation-result-click` |

See [docs/api-contract.md](docs/api-contract.md) for the current frontend-facing contract.

## Documentation

- [docs/api-contract.md](docs/api-contract.md): current API contract
- [docs/curation-wizard-api.md](docs/curation-wizard-api.md): curation wizard option code table
- [docs/operations.md](docs/operations.md): environment variables, deployment, admin operations, troubleshooting
- [docs/database-schema.md](docs/database-schema.md): JPA-backed schema reference
- [docs/migration/README.md](docs/migration/README.md): one-off production DB migration notes

## Production Notes

- `main` pushes run `.github/workflows/deploy.yml`.
- Deployment runs `./gradlew test bootJar` before building and pushing the Docker image.
- The backend container is `meanhwa-server`; Redis is `meanhwa-redis`.
- Production secrets belong in GitHub Actions Secrets or server environment variables, never in source.
