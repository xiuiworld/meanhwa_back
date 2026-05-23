# Operations

운영 배포, env, DB migration, 장애 확인에 필요한 내용만 남깁니다. API 계약은 [api-contract.md](api-contract.md)를 봅니다.

## Runtime Env

Prod 필수값:

| Name | Description |
| --- | --- |
| `DB_URL` | MySQL JDBC URL |
| `DB_USERNAME` | DB username |
| `DB_PASSWORD` | DB password |
| `JWT_SECRET` | JWT signing secret, 32자 이상 |
| `AWS_S3_BUCKET` | S3 bucket |
| `AWS_REGION` | AWS region |
| `AWS_S3_PUBLIC_BASE_URL` | 업로드 이미지 public base URL |
| `CACHE_TYPE` | `redis` 권장 |
| `REDIS_HOST` | Docker network 안의 Redis host |
| `REDIS_PORT` | 보통 `6379` |

배포 secret:

```text
DOCKERHUB_USERNAME
DOCKERHUB_TOKEN
AWS_EC2_HOST
AWS_EC2_PEM_KEY
```

주요 optional 값:

| Name | Default |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod` in Dockerfile |
| `JWT_ACCESS_TOKEN_VALIDITY_MINUTES` | `30` |
| `JWT_REFRESH_TOKEN_VALIDITY_DAYS` | `14` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173` |
| `OPENAI_API_KEY` | blank, template fallback |
| `OPENAI_BASE_URL` | `https://api.openai.com` |
| `OPENAI_MODEL` | `gpt-5.4-mini` |
| `OPENAI_TIMEOUT_MILLIS` | `5000` |
| `OPENAI_MAX_OUTPUT_TOKENS` | app `300`, deploy fallback `500` |
| `KAKAO_USERINFO_URL` | Kakao userinfo URL |
| `NAVER_USERINFO_URL` | Naver userinfo URL |
| `STORAGE_MAX_FILE_SIZE_BYTES` | `5242880` |
| `JPA_DDL_AUTO` | `validate` |
| `FLYWAY_ENABLED` | `true` |
| `FLYWAY_BASELINE_ON_MIGRATE` | `false` |
| `CACHE_TTL_MILLIS` | `300000` |

커밋하거나 채팅에 남기면 안 되는 값:

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

## Deploy

`main` push 시 `.github/workflows/deploy.yml`이 실행됩니다.

배포 순서:

1. JDK 21 설정
2. `./gradlew test bootJar`
3. `ENABLE_MYSQL_FLYWAY_TESTS=true`로 MySQL Flyway 검증
4. 필수 secret 검증
5. Docker image build/push
6. EC2에서 `meanhwa-redis` 확인 또는 시작
7. `meanhwa-server` 컨테이너 교체
8. `GET /actuator/health/readiness` 확인

운영 컨테이너:

| Component | Value |
| --- | --- |
| Backend container | `meanhwa-server` |
| Redis container | `meanhwa-redis` |
| Docker network | `meanhwa-net` |
| Backend image | `{DOCKERHUB_USERNAME}/meanhwa-back` |
| Port binding | `127.0.0.1:8080 -> 8080` |

EC2 확인:

```bash
sudo docker ps
sudo docker logs --tail=200 meanhwa-server
sudo docker logs --tail=100 meanhwa-redis
curl -fsS http://localhost:8080/actuator/health/readiness
```

재시작:

```bash
sudo docker restart meanhwa-redis
sudo docker restart meanhwa-server
```

## DB And Flyway

Prod는 `data.sql`을 실행하지 않습니다.

```yaml
spring.sql.init.mode: never
spring.jpa.hibernate.ddl-auto: ${JPA_DDL_AUTO:validate}
spring.flyway.enabled: ${FLYWAY_ENABLED:true}
spring.flyway.locations: classpath:db/migration/mysql
spring.flyway.baseline-on-migrate: ${FLYWAY_BASELINE_ON_MIGRATE:false}
```

운영 DB 규칙:

- schema 변경은 `src/main/resources/db/migration/mysql/V*.sql`에 다음 버전 Flyway migration으로 추가합니다.
- 기존 운영 DB를 처음 Flyway에 편입할 때만 `FLYWAY_BASELINE_ON_MIGRATE=true`를 사용합니다.
- 편입 성공 후 `FLYWAY_BASELINE_ON_MIGRATE=false`로 되돌립니다.
- validation 실패를 `JPA_DDL_AUTO=update`로 우회하지 않습니다.
- 운영 DB 직접 수정 전에는 백업과 `SELECT` 확인을 먼저 합니다.

로컬 MySQL migration 검증:

```powershell
$env:ENABLE_MYSQL_FLYWAY_TESTS='true'
.\gradlew.bat test --tests "*FlywayMysqlMigrationIntegrationTest" --rerun-tasks
```

`docs/migration/*.sql`은 Flyway 도입 전 수동 적용 이력입니다. Flyway 관리 DB에는 다시 실행하지 않습니다.

## Admin

관리자 API는 `ROLE_ADMIN` JWT가 필요합니다.

권한 확인:

```http
GET /api/v1/users/me
Authorization: Bearer {accessToken}
```

권한 변경:

```http
PUT /api/v1/admin/users/{userId}/role
Authorization: Bearer {adminAccessToken}
Content-Type: application/json
```

```json
{
  "role": "ROLE_ADMIN"
}
```

정책:

- 본인 role은 변경할 수 없습니다.
- 마지막 `ROLE_ADMIN`은 강등할 수 없습니다.
- 변경 후 대상 사용자는 다시 로그인해야 새 JWT에 반영됩니다.
- 최초 관리자 bootstrap처럼 API 호출이 불가능한 경우에만 DB를 직접 수정합니다.

이미지 업로드:

```http
POST /api/v1/admin/uploads/images
Content-Type: multipart/form-data
```

Field는 `file`입니다. 허용 타입은 `jpeg`, `png`, `webp`이고 기본 최대 크기는 5 MB입니다. 꽃 등록/수정의 `imageUrl`은 업로드 응답의 `data.imageUrl`을 사용합니다.

## Post Deploy Check

운영 배포 직후 필요한 최소 확인입니다. 토큰과 secret은 로그나 문서에 남기지 않습니다.

1. Health: `curl -fsS "$BASE_URL/api/v1/flowers"`
2. Flyway: `flyway_schema_history`의 최신 migration 성공 여부 확인
3. OAuth: Kakao/Naver login 후 `GET /api/v1/users/me`
4. Curation: 익명/로그인 `POST /api/v1/curation/results`
5. Upload: 관리자 이미지 업로드 후 반환 URL 접근 확인
6. CMS: 테스트 꽃 생성 후 상세 조회, 확인 뒤 soft delete
7. Message: 로그인 사용자로 `POST /api/v1/messages/generate`

Dummy image URL 점검:

```sql
SELECT id, name, image_url
FROM flowers
WHERE image_url LIKE 'https://cdn.meanhwa.example/%';
```

결과가 있으면 CMS에서 이미지를 다시 업로드하고 실제 S3/CDN URL로 교체합니다.

## Troubleshooting

### `/admin` 401 또는 403

- 401: token 없음, 만료, 위조, 폐기
- 403: 로그인은 됐지만 `ROLE_ADMIN`이 아님
- `GET /api/v1/users/me`로 현재 role을 확인합니다.

### OAuth Login Failed

- path가 `/auth/login/kakao` 또는 `/auth/login/naver`인지 확인합니다.
- 프론트가 provider access token을 보냈는지 확인합니다.
- prod에서 `/auth/login/dev`를 호출하지 않습니다.
- `sudo docker logs --tail=200 meanhwa-server`를 확인합니다.

### Message Uses Template Fallback

OpenAI 실패는 사용자 API 실패로 전파되지 않고 template fallback으로 처리됩니다. 반복 실패 시 `OPENAI_API_KEY`, `OPENAI_MODEL`, `OPENAI_BASE_URL`, `OPENAI_TIMEOUT_MILLIS`를 확인합니다.

### Message 429

기본 제한은 사용자별 1시간 10회입니다. Prod는 Redis를 사용합니다. `REDIS_HOST`, `REDIS_PORT`, `CACHE_TYPE`와 `meanhwa-redis` 로그를 확인합니다.

### Image Upload Failed

| errorCode | Cause |
| --- | --- |
| `INVALID_FILE_TYPE` | jpeg/png/webp가 아님 |
| `FILE_TOO_LARGE` | max size 초과 |
| `UPLOAD_FAILED` | S3 업로드 실패 |

S3 관련 env는 `AWS_S3_BUCKET`, `AWS_REGION`, `AWS_S3_PUBLIC_BASE_URL`입니다.

### Flyway Or Schema Validation Failed

1. 운영 DB 백업을 확인합니다.
2. `sudo docker logs --tail=200 meanhwa-server`에서 table/column 이름을 확인합니다.
3. 누락된 변경을 다음 순번의 새 Flyway migration으로 추가합니다.
4. 실패한 migration 기록이 남았으면 원인 수정 후 `flyway repair`를 적용합니다.
5. DB clone 또는 staging에서 먼저 재기동해 확인합니다.
