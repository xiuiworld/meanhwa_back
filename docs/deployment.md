# Meanhwa 배포 운영 가이드

이 문서는 운영 서버에서 Meanhwa backend를 배포, 확인, 복구할 때 쓰는 절차입니다.

## 운영 구성

현재 운영 구성:

| Component | Value |
| --- | --- |
| Backend container | `meanhwa-server` |
| Redis container | `meanhwa-redis` |
| Backend image | `xiuiworld/meanhwa-back` |
| Backend port | `127.0.0.1:8080 -> 8080` |
| Runtime profile | `prod` |
| DB | AWS RDS MySQL |
| Image storage | AWS S3 |
| Cache | Redis or simple cache, based on `CACHE_TYPE` |

## GitHub Actions 배포 흐름

`.github/workflows/deploy.yml`은 `main` 브랜치 push에서 실행됩니다.

순서:

1. Checkout
2. JDK 21 설정
3. `./gradlew test bootJar`
4. 배포 secret 검증
5. Docker image build
6. Docker Hub push
7. EC2 SSH 접속
8. Redis container 확인/생성
9. backend image pull
10. 기존 backend container 교체
11. `GET /api/v1/flowers` health check

테스트가 실패하면 배포는 진행되지 않습니다.

## 서버 접속 후 기본 확인

```bash
sudo docker ps
```

예상 container:

```text
meanhwa-server
meanhwa-redis
```

백엔드 로그:

```bash
sudo docker logs -f meanhwa-server
```

최근 로그만 확인:

```bash
sudo docker logs --tail=200 meanhwa-server
```

Redis 로그:

```bash
sudo docker logs --tail=100 meanhwa-redis
```

## Health Check

서버 내부에서:

```bash
curl -fsS http://localhost:8080/api/v1/flowers
```

성공하면 JSON 응답이 내려옵니다.

## Container 재시작

백엔드만 재시작:

```bash
sudo docker restart meanhwa-server
```

Redis만 재시작:

```bash
sudo docker restart meanhwa-redis
```

## 현재 backend 환경변수 확인

민감 정보가 출력되므로 공유하지 마세요.

```bash
sudo docker inspect meanhwa-server \
  --format '{{range .Config.Env}}{{println .}}{{end}}'
```

필요한 값만 확인:

```bash
sudo docker inspect meanhwa-server \
  --format '{{range .Config.Env}}{{println .}}{{end}}' \
  | grep -E 'SPRING_PROFILES_ACTIVE|DB_URL|CACHE_TYPE|REDIS_HOST|OPENAI_MODEL|AWS_REGION'
```

## RDS 접속

먼저 DB 접속 정보를 확인합니다.

```bash
sudo docker inspect meanhwa-server \
  --format '{{range .Config.Env}}{{println .}}{{end}}' \
  | grep -E 'DB_URL|DB_USERNAME|DB_PASSWORD'
```

`DB_URL` 예시:

```text
jdbc:mysql://meanhwa-mysql.example.ap-northeast-2.rds.amazonaws.com:3306/meanhwa?serverTimezone=Asia/Seoul
```

MySQL client 접속:

```bash
mysql -h {RDS_HOST} -P 3306 -u {DB_USERNAME} -p meanhwa
```

Docker MySQL client 사용:

```bash
sudo docker run --rm -it mysql:8 \
  mysql -h {RDS_HOST} -P 3306 -u {DB_USERNAME} -p meanhwa
```

## 수동 Docker 재배포

GitHub Actions가 실패했거나 임시로 서버에서 직접 교체해야 할 때만 사용합니다.

```bash
sudo docker pull xiuiworld/meanhwa-back
sudo docker stop meanhwa-server || true
sudo docker rm meanhwa-server || true
```

그 다음 기존 workflow와 동일한 환경변수를 넣어 `docker run`을 실행해야 합니다. 환경변수가 많으므로 가능하면 GitHub Actions 배포를 사용하세요.

## GitHub Secrets

필수 secret은 [env-vars.md](env-vars.md)를 기준으로 관리합니다.

대표 필수값:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
AWS_S3_BUCKET
AWS_REGION
AWS_S3_PUBLIC_BASE_URL
CACHE_TYPE
REDIS_HOST
REDIS_PORT
OPENAI_API_KEY
DOCKERHUB_USERNAME
DOCKERHUB_TOKEN
AWS_EC2_HOST
AWS_EC2_PEM_KEY
```

## 운영 DB 변경 주의

운영 DB에서 직접 `UPDATE`를 실행하기 전:

1. 반드시 `SELECT`로 대상 row를 확인합니다.
2. 조건은 `id`, `provider`, `email`, `oauth_id` 등으로 충분히 좁힙니다.
3. 변경 후 다시 `SELECT`로 결과를 확인합니다.
4. 토큰/비밀번호/secret 값을 채팅이나 문서에 붙이지 않습니다.
