# Meanhwa Troubleshooting

운영/개발 중 자주 발생하는 문제와 확인 절차입니다.

## 테스트가 `JAVA_HOME is not set`으로 실패함

Windows 개발 환경에서 JDK 21을 지정합니다.

```powershell
$env:JAVA_HOME='C:\Users\xiuiw\.jdks\corretto-21.0.11'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat test --rerun-tasks
```

## `/admin` 접근이 안 됨

증상:

- 프론트에서 “관리자 권한이 필요합니다” 표시
- 마이페이지에 “일반 사용자”로 표시

확인:

```sql
SELECT id, provider, oauth_id, email, nickname, role
FROM users
WHERE email = 'user@example.com';
```

`role`이 `ROLE_USER`면 관리자 승격:

```sql
UPDATE users
SET role = 'ROLE_ADMIN'
WHERE provider = 'NAVER'
  AND email = 'user@example.com';
```

그 다음:

1. 프론트 로그아웃
2. 브라우저 쿠키 삭제 또는 시크릿 창 사용
3. 다시 소셜 로그인
4. `/api/v1/users/me` 응답의 `role` 확인

## DB에서는 `ROLE_ADMIN`인데 프론트는 일반 사용자로 보임

원인:

- 기존 access token/session에 예전 권한이 남아 있음
- 프론트 BFF 쿠키가 갱신되지 않음

조치:

1. 로그아웃
2. 사이트 쿠키 삭제
3. 다시 로그인
4. `/api/v1/users/me` 직접 확인

## OAuth 로그인 실패

증상:

- `INVALID_OAUTH_TOKEN`
- `UNSUPPORTED_OAUTH_PROVIDER`

확인:

- provider path가 `kakao` 또는 `naver`인지 확인
- 프론트가 provider access token을 보냈는지 확인
- Kakao/Naver userinfo URL override가 잘못되지 않았는지 확인
- 서버 로그 확인

```bash
sudo docker logs --tail=200 meanhwa-server
```

## 메시지 생성이 OpenAI가 아니라 템플릿으로만 나옴

가능한 원인:

- `OPENAI_API_KEY` 없음
- OpenAI timeout
- OpenAI non-2xx 응답
- OpenAI response parse 실패
- 네트워크 문제

현재 설계상 OpenAI 실패는 사용자 API 실패로 만들지 않고 템플릿 fallback으로 성공 응답을 반환합니다.

확인:

```bash
sudo docker inspect meanhwa-server \
  --format '{{range .Config.Env}}{{println .}}{{end}}' \
  | grep -E 'OPENAI_API_KEY|OPENAI_MODEL|OPENAI_BASE_URL|OPENAI_TIMEOUT_MILLIS'
```

`OPENAI_API_KEY` 값은 출력 후 공유하지 마세요.

## 이미지 업로드 실패

에러별 확인:

| errorCode | 원인 |
| --- | --- |
| `INVALID_FILE_TYPE` | jpeg/png/webp가 아님 |
| `FILE_TOO_LARGE` | max size 초과 |
| `UPLOAD_FAILED` | S3 업로드 실패 |

운영 확인:

```bash
sudo docker inspect meanhwa-server \
  --format '{{range .Config.Env}}{{println .}}{{end}}' \
  | grep -E 'AWS_S3_BUCKET|AWS_REGION|AWS_S3_PUBLIC_BASE_URL|STORAGE'
```

추가 확인:

- EC2/IAM credential 설정
- S3 bucket 존재 여부
- bucket 권한
- public base URL 설정

## Redis 연결 실패

확인:

```bash
sudo docker ps
sudo docker logs --tail=100 meanhwa-redis
```

backend env 확인:

```bash
sudo docker inspect meanhwa-server \
  --format '{{range .Config.Env}}{{println .}}{{end}}' \
  | grep -E 'CACHE_TYPE|REDIS_HOST|REDIS_PORT'
```

기대값:

```text
CACHE_TYPE=redis
REDIS_HOST=meanhwa-redis
REDIS_PORT=6379
```

Redis 재시작:

```bash
sudo docker restart meanhwa-redis
sudo docker restart meanhwa-server
```

## RDS 접속 실패

대표 에러:

```text
Unknown MySQL server host 'HOST'
```

원인:

- 예시 placeholder인 `HOST`를 그대로 입력함

정상 예시:

```bash
mysql -h meanhwa-mysql.example.ap-northeast-2.rds.amazonaws.com -P 3306 -u admin -p meanhwa
```

DB URL에서 host만 추출해야 합니다.

```text
jdbc:mysql://{HOST}:3306/meanhwa?serverTimezone=Asia/Seoul
```

## 배포 후 health check 실패

GitHub Actions에서 health check가 실패하면 서버에서 확인합니다.

```bash
sudo docker ps -a
sudo docker logs --tail=200 meanhwa-server
sudo docker logs --tail=100 meanhwa-redis
```

자주 보는 원인:

- 필수 env 누락
- DB 접속 실패
- JWT secret이 너무 짧음
- S3 env 누락
- Redis host 설정 오류
- OpenAI key 누락

수동 health check:

```bash
curl -v http://localhost:8080/api/v1/flowers
```

## seed 데이터가 안 들어옴

테스트 프로필은 `spring.sql.init.mode=always`로 `data.sql`을 로드합니다.

prod는 `spring.sql.init.mode=never`입니다. 운영 DB에는 `data.sql`이 자동 삽입되지 않습니다.

운영 DB에 seed가 필요하면 별도 migration/import 절차를 사용해야 합니다.

## 관리자 API가 401 또는 403을 반환함

| HTTP | 의미 |
| ---: | --- |
| 401 | 로그인 필요 또는 token invalid |
| 403 | 로그인은 됐지만 `ROLE_ADMIN`이 아님 |

확인:

```http
GET /api/v1/users/me
Authorization: Bearer {accessToken}
```

응답 `role`이 `ROLE_ADMIN`인지 봅니다.

## Docker container가 계속 재시작됨

확인:

```bash
sudo docker ps -a
sudo docker logs --tail=300 meanhwa-server
```

환경변수 확인:

```bash
sudo docker inspect meanhwa-server \
  --format '{{range .Config.Env}}{{println .}}{{end}}'
```

민감 정보가 포함되므로 출력 내용을 외부에 공유하지 마세요.
