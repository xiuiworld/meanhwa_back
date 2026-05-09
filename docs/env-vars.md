# Meanhwa 환경변수 문서

이 문서는 local/test/prod 및 GitHub Actions에서 사용하는 환경변수를 정리합니다.

## Production Required

운영 배포에 필요한 필수 값입니다.

| Name | Example | Description |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://host:3306/meanhwa?serverTimezone=Asia/Seoul` | MySQL JDBC URL |
| `DB_USERNAME` | `admin` | DB username |
| `DB_PASSWORD` | secret | DB password |
| `JWT_SECRET` | 32 chars or longer | JWT signing secret |
| `AWS_S3_BUCKET` | `meanhwa-images` | S3 bucket name |
| `AWS_REGION` | `ap-northeast-2` | AWS region |
| `AWS_S3_PUBLIC_BASE_URL` | `https://cdn.example.com` | Public image URL base |
| `CACHE_TYPE` | `redis` | Spring cache type |
| `REDIS_HOST` | `meanhwa-redis` | Redis host from backend container |
| `REDIS_PORT` | `6379` | Redis port |
| `OPENAI_API_KEY` | secret | OpenAI API key |

## GitHub Actions Required

배포 workflow에서 추가로 필요한 값입니다.

| Name | Description |
| --- | --- |
| `DOCKERHUB_USERNAME` | Docker Hub account |
| `DOCKERHUB_TOKEN` | Docker Hub token |
| `AWS_EC2_HOST` | EC2 public host or IP |
| `AWS_EC2_PEM_KEY` | SSH private key for EC2 deploy |

## Optional Runtime Settings

| Name | Default | Description |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod` in Dockerfile | Active Spring profile |
| `JWT_ACCESS_TOKEN_VALIDITY_MINUTES` | `30` | Access token lifetime |
| `JWT_REFRESH_TOKEN_VALIDITY_DAYS` | `14` | Refresh token lifetime |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173` | Allowed frontend origins |
| `OPENAI_BASE_URL` | `https://api.openai.com` | OpenAI-compatible API base URL |
| `OPENAI_MODEL` | `gpt-5.4-mini` | OpenAI model name |
| `OPENAI_TIMEOUT_MILLIS` | `5000` | OpenAI client timeout |
| `OPENAI_MAX_OUTPUT_TOKENS` | `300` | Message generation output token limit |
| `KAKAO_USERINFO_URL` | `https://kapi.kakao.com/v2/user/me` | Kakao profile endpoint |
| `NAVER_USERINFO_URL` | `https://openapi.naver.com/v1/nid/me` | Naver profile endpoint |
| `KAKAO_TIMEOUT_MILLIS` | `3000` | Kakao client timeout |
| `NAVER_TIMEOUT_MILLIS` | `3000` | Naver client timeout |
| `STORAGE_MAX_FILE_SIZE_BYTES` | `5242880` | Upload max size |
| `JPA_DDL_AUTO` | `update` | Hibernate schema mode |
| `CACHE_TTL_MILLIS` | `300000` | Redis cache TTL |

## Local Defaults

`application.yml` provides local defaults:

- default profile: `local`
- cache: `simple`
- storage: `fake`
- dev JWT secret for local/test only
- Kakao/Naver profile URLs
- OpenAI settings without a required API key

Local does not require Redis, S3, RDS, or OpenAI.

## Test Defaults

`application-test.yml` uses:

- H2 in-memory DB
- `ddl-auto=create-drop`
- `spring.sql.init.mode=always`
- fake storage
- simple cache
- test JWT secret

## Prod Profile

`application-prod.yml` reads secrets from env vars:

- MySQL datasource
- JWT secret and token lifetime
- CORS origins
- S3 bucket/region/public URL
- Redis config
- OpenAI key/model/client settings
- Kakao/Naver userinfo URL and timeout

## Security Rules

Never commit these values:

```text
DB_PASSWORD
JWT_SECRET
OPENAI_API_KEY
AWS credentials
provider access tokens
refresh tokens
Docker Hub token
EC2 private key
```

If a secret is exposed, rotate it immediately and update GitHub Actions Secrets/server env.
