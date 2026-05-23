# Meanhwa 운영 문서

운영/배포/관리자/장애 확인에 필요한 절차만 모은 문서입니다. API 계약은 [api-contract.md](api-contract.md), DB 구조는 [database-schema.md](database-schema.md)를 봅니다.

## 환경변수

Production runtime 필수값:

| Name | 설명 |
| --- | --- |
| `DB_URL` | MySQL JDBC URL |
| `DB_USERNAME` | DB username |
| `DB_PASSWORD` | DB password |
| `JWT_SECRET` | JWT signing secret, 32자 이상 |
| `AWS_S3_BUCKET` | S3 bucket |
| `AWS_REGION` | AWS region, 기본 `ap-northeast-2` |
| `AWS_S3_PUBLIC_BASE_URL` | 업로드 이미지 public base URL |
| `OPENAI_API_KEY` | 메시지 생성용 OpenAI API key |
| `CACHE_TYPE` | `redis` 권장 |
| `REDIS_HOST` | Docker network 안의 Redis host, 보통 `meanhwa-redis` |
| `REDIS_PORT` | Redis port, 보통 `6379` |

GitHub Actions 배포 추가 secret:

```text
DOCKERHUB_USERNAME
DOCKERHUB_TOKEN
AWS_EC2_HOST
AWS_EC2_PEM_KEY
```

Optional runtime settings:

| Name | Default |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod` in Dockerfile |
| `JWT_ACCESS_TOKEN_VALIDITY_MINUTES` | `30` |
| `JWT_REFRESH_TOKEN_VALIDITY_DAYS` | `14` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173` |
| `OPENAI_BASE_URL` | `https://api.openai.com` |
| `OPENAI_MODEL` | `gpt-5.4-mini` |
| `OPENAI_TIMEOUT_MILLIS` | `5000` |
| `OPENAI_MAX_OUTPUT_TOKENS` | `300` in app, `500` deploy fallback |
| `KAKAO_USERINFO_URL` | `https://kapi.kakao.com/v2/user/me` |
| `NAVER_USERINFO_URL` | `https://openapi.naver.com/v1/nid/me` |
| `KAKAO_TIMEOUT_MILLIS` | `3000` |
| `NAVER_TIMEOUT_MILLIS` | `3000` |
| `STORAGE_MAX_FILE_SIZE_BYTES` | `5242880` |
| `JPA_DDL_AUTO` | `update` |
| `CACHE_TTL_MILLIS` | `300000` |

Local/test는 H2, fake storage, simple cache, in-memory message rate limit을 사용합니다. Prod는 MySQL/RDS, S3, Redis를 사용합니다.

절대 커밋하거나 채팅에 붙이지 않을 값:

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

## 배포

`.github/workflows/deploy.yml`은 `main` branch push에서 실행됩니다.

순서:

1. JDK 21 설정
2. `./gradlew test bootJar`
3. 배포 secret 검증
4. Docker image build/push
5. EC2 SSH 접속
6. `meanhwa-net` 생성/연결
7. `meanhwa-redis` 확인/시작
8. `meanhwa-server` 컨테이너 교체
9. `GET http://localhost:8080/api/v1/flowers` health check

운영 구성:

| Component | Value |
| --- | --- |
| Backend container | `meanhwa-server` |
| Redis container | `meanhwa-redis` |
| Docker network | `meanhwa-net` |
| Backend image | `{DOCKERHUB_USERNAME}/meanhwa-back` |
| Port binding | `127.0.0.1:8080 -> 8080` |
| Runtime profile | `prod` |

EC2 확인 명령:

```bash
sudo docker ps
sudo docker logs --tail=200 meanhwa-server
sudo docker logs --tail=100 meanhwa-redis
curl -fsS http://localhost:8080/api/v1/flowers
```

재시작:

```bash
sudo docker restart meanhwa-redis
sudo docker restart meanhwa-server
```

런타임 env 확인은 secret을 출력하므로 공유하지 않습니다.

```bash
sudo docker inspect meanhwa-server \
  --format '{{range .Config.Env}}{{println .}}{{end}}'
```

필요한 키만 확인:

```bash
sudo docker inspect meanhwa-server \
  --format '{{range .Config.Env}}{{println .}}{{end}}' \
  | grep -E 'SPRING_PROFILES_ACTIVE|DB_URL|CACHE_TYPE|REDIS_HOST|OPENAI_MODEL|AWS_REGION'
```

## DB와 마이그레이션

`prod`는 `spring.sql.init.mode=never`라서 `data.sql`을 자동 실행하지 않습니다. 운영 seed/migration은 별도로 적용합니다.

큐레이션 위저드 운영 DB migration:

- Guide: [migration/README.md](migration/README.md)
- SQL: [migration/2026-05-curation-wizard-prod.sql](migration/2026-05-curation-wizard-prod.sql)

```bash
mysql -h {RDS_HOST} -P 3306 -u {DB_USERNAME} -p meanhwa < docs/migration/2026-05-curation-wizard-prod.sql
```

스크립트 마지막 `missing_wizard_code` 결과가 0행인지 확인합니다.

RDS 접속:

```bash
mysql -h {RDS_HOST} -P 3306 -u {DB_USERNAME} -p meanhwa
```

DB 직접 변경 규칙:

- API로 처리할 수 있으면 API를 우선 사용합니다.
- 직접 변경 전 `SELECT`로 대상 row를 확인합니다.
- `id`, `provider`, `email`, `oauth_id` 등으로 조건을 좁힙니다.
- 변경 후 다시 `SELECT`로 결과를 확인합니다.

## 관리자 운영

백오피스는 일반 소셜 로그인 후 JWT의 `role`이 `ROLE_ADMIN`인 사용자만 접근합니다.

권한 확인:

```http
GET /api/v1/users/me
Authorization: Bearer {accessToken}
```

관리자 API:

```http
POST   /api/v1/admin/flowers
GET    /api/v1/admin/flowers/{flowerId}
PUT    /api/v1/admin/flowers/{flowerId}
DELETE /api/v1/admin/flowers/{flowerId}
PUT    /api/v1/admin/flowers/{flowerId}/tags

POST   /api/v1/admin/tags
PUT    /api/v1/admin/tags/{tagId}
DELETE /api/v1/admin/tags/{tagId}

POST   /api/v1/admin/uploads/images

GET    /api/v1/admin/users
GET    /api/v1/admin/users/{userId}
PUT    /api/v1/admin/users/{userId}/role

GET    /api/v1/admin/statistics/summary
GET    /api/v1/admin/statistics/popular-tags
GET    /api/v1/admin/statistics/popular-flowers
GET    /api/v1/admin/statistics/daily-active-users
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
- 마지막 남은 `ROLE_ADMIN`은 강등할 수 없습니다.
- 변경 후 대상 사용자는 다시 로그인해야 새 JWT에 role이 반영됩니다.
- 최초 관리자 bootstrap처럼 관리자 API를 호출할 수 없는 경우에만 DB 직접 수정을 사용합니다.

꽃/태그 정책:

- 꽃과 태그 삭제는 soft delete입니다.
- 삭제된 꽃은 public list/detail/curation/message generation에서 숨겨집니다.
- Admin tag API는 `code`를 받지 않습니다. 위저드용 `tags.code`는 seed/migration으로 관리합니다.
- 꽃-태그 매핑 `PUT /admin/flowers/{flowerId}/tags`는 전체 교체입니다.
- 매핑 `weight`는 1-5입니다.

이미지 업로드:

```http
POST /api/v1/admin/uploads/images
Content-Type: multipart/form-data
```

Field: `file`

Allowed: `image/jpeg`, `image/png`, `image/webp`

Default max size: 5 MB

## 장애 확인

### 테스트가 `JAVA_HOME is not set`으로 실패

```powershell
$env:JAVA_HOME='C:\Users\xiuiw\.jdks\corretto-21.0.11'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat test --rerun-tasks
```

### `/admin` 접근 불가

1. `GET /api/v1/users/me`의 `role` 확인
2. `ROLE_USER`이면 관리자 API로 승격
3. 로그아웃, 쿠키 삭제 또는 시크릿 창, 재로그인

기존 JWT에는 예전 role이 들어 있을 수 있습니다.

### OAuth 로그인 실패

확인 항목:

- provider path가 `kakao` 또는 `naver`인지 확인
- 프론트가 provider access token을 보냈는지 확인
- prod에서 `/auth/login/dev`를 호출하지 않는지 확인
- Kakao/Naver userinfo URL override 확인
- `sudo docker logs --tail=200 meanhwa-server`

### 메시지 생성이 template fallback만 사용

OpenAI 실패는 사용자 API 실패로 전파하지 않고 template fallback으로 성공 처리합니다.

확인:

```bash
sudo docker inspect meanhwa-server \
  --format '{{range .Config.Env}}{{println .}}{{end}}' \
  | grep -E 'OPENAI_API_KEY|OPENAI_MODEL|OPENAI_BASE_URL|OPENAI_TIMEOUT_MILLIS'
```

### 메시지 생성 429

기본 정책은 사용자별 1시간 10회입니다. Prod는 Redis에 카운터를 저장합니다.

```bash
sudo docker inspect meanhwa-server \
  --format '{{range .Config.Env}}{{println .}}{{end}}' \
  | grep -E 'REDIS_HOST|REDIS_PORT|CACHE_TYPE'
sudo docker logs --tail=100 meanhwa-redis
```

### 이미지 업로드 실패

| errorCode | 원인 |
| --- | --- |
| `INVALID_FILE_TYPE` | jpeg/png/webp가 아님 |
| `FILE_TOO_LARGE` | max size 초과 |
| `UPLOAD_FAILED` | S3 업로드 실패 |

확인:

```bash
sudo docker inspect meanhwa-server \
  --format '{{range .Config.Env}}{{println .}}{{end}}' \
  | grep -E 'AWS_S3_BUCKET|AWS_REGION|AWS_S3_PUBLIC_BASE_URL|STORAGE'
```

### 배포 후 health check 실패

```bash
sudo docker ps -a
sudo docker logs --tail=200 meanhwa-server
sudo docker logs --tail=100 meanhwa-redis
curl -v http://localhost:8080/api/v1/flowers
```

자주 보는 원인:

- 필수 env 누락
- DB 접속 실패
- JWT secret 길이 부족
- S3 env 누락
- Redis host 설정 오류
- OpenAI key 누락

### 관리자 API 401/403

| HTTP | 의미 |
| ---: | --- |
| 401 | 로그인 필요 또는 token invalid |
| 403 | 로그인은 됐지만 `ROLE_ADMIN`이 아님 |

`GET /api/v1/users/me`로 현재 role을 확인합니다.
