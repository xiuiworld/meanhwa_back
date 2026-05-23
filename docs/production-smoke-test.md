# Production Smoke Test Checklist

운영 배포 직후 실제 credential과 외부 서비스 연결을 확인하는 수동 체크리스트입니다. 토큰, 비밀번호, provider access token, AWS/OpenAI secret은 문서나 채팅에 남기지 않습니다.

## 준비

- `BASE_URL`: 운영 API base URL. 예: `https://api.example.com`
- 일반 사용자 access token 1개
- 관리자 access token 1개
- Kakao/Naver provider access token 각 1개
- 업로드 테스트용 `jpg`, `png`, 또는 `webp` 이미지 1개
- 테스트 후 삭제 가능한 smoke test 꽃 이름. 예: `smoke-test-20260523`
- 메시지 생성에 사용할 기존 활성 꽃 id 1개

## 1. Health

```bash
curl -fsS "$BASE_URL/api/v1/flowers"
```

기대 결과:

- HTTP 200
- 공통 응답 body의 `status`가 200
- 서버 로그에 startup 또는 schema validation error가 없음

## 2. Flyway/DB

DB에서 확인:

```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

기대 결과:

- 기존 운영 DB 최초 편입 배포: baseline version `1` 기록이 있음. 이후 배포에서는 `FLYWAY_BASELINE_ON_MIGRATE=false` 상태로 기동함
- 신규 빈 DB: `V1__baseline_current_schema.sql`, `V2__seed_curation_reference_data.sql` 성공 기록이 있음
- `tags`에 `WINDOW_BRIGHT` 등 위저드용 code가 active 상태로 존재함
- 앱 로그에 Hibernate schema validation 실패가 없음

확인 SQL:

```sql
SELECT code, name
FROM tags
WHERE code IN ('BIRTHDAY', 'FAMILY', 'WINDOW_BRIGHT', 'LOVE_1')
  AND deleted_at IS NULL;
```

기존 꽃/태그 매핑이 있는 DB에서는 V2가 새 위저드 태그 매핑을 복제했는지도 확인합니다:

```sql
SELECT t.code, COUNT(m.id) AS mapping_count
FROM tags t
LEFT JOIN flower_tag_mappings m ON m.tag_id = t.id
WHERE t.code IN ('WEDDING', 'RECOVERY', 'FAMILY', 'WINDOW_BRIGHT', 'LOVE_1', 'GET_WELL_4')
  AND t.deleted_at IS NULL
GROUP BY t.code
ORDER BY t.code;
```

기존 운영 데이터가 있는 DB라면 각 `mapping_count`가 1 이상이어야 합니다. 신규 빈 DB는 CMS로 꽃과 매핑을 넣기 전까지 0일 수 있습니다.

## 3. Kakao/Naver Login

Kakao:

```bash
curl -fsS -X POST "$BASE_URL/api/v1/auth/login/kakao" \
  -H "Content-Type: application/json" \
  -d '{"accessToken":"<KAKAO_PROVIDER_ACCESS_TOKEN>"}'
```

Naver:

```bash
curl -fsS -X POST "$BASE_URL/api/v1/auth/login/naver" \
  -H "Content-Type: application/json" \
  -d '{"accessToken":"<NAVER_PROVIDER_ACCESS_TOKEN>"}'
```

발급된 access token으로 사용자 확인:

```bash
curl -fsS "$BASE_URL/api/v1/users/me" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

기대 결과:

- login 응답에 `accessToken`, `refreshToken`, `tokenType=Bearer`가 있음
- `/users/me`가 provider, oauthId, role을 반환함
- `POST /api/v1/auth/login/dev`는 prod에서 실패함

## 4. S3 Image Upload And CMS Reflection

이미지 업로드:

```bash
curl -fsS -X POST "$BASE_URL/api/v1/admin/uploads/images" \
  -H "Authorization: Bearer <ADMIN_ACCESS_TOKEN>" \
  -F "file=@/path/to/smoke-test.webp"
```

반환된 `data.imageUrl` 접근 확인:

```bash
curl -I "<RETURNED_IMAGE_URL>"
```

테스트 꽃 생성:

```bash
curl -fsS -X POST "$BASE_URL/api/v1/admin/flowers" \
  -H "Authorization: Bearer <ADMIN_ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "smoke-test-20260523",
    "imageUrl": "<RETURNED_IMAGE_URL>",
    "coreMeaning": "운영 점검",
    "description": "운영 smoke test용 임시 데이터",
    "scientificName": "Smoke test",
    "origin": "운영 점검",
    "bloomingSeason": "연중",
    "scent": "없음",
    "managementLevel": "EASY",
    "managementInfo": "점검 후 삭제",
    "isToxicToPets": false,
    "priceRange": "LOW"
  }'
```

기대 결과:

- 업로드 응답의 `imageUrl`이 `AWS_S3_PUBLIC_BASE_URL` 하위 URL임
- URL을 브라우저 또는 `curl -I`로 열 수 있음
- 생성된 꽃 상세에서 같은 `imageUrl`이 반환됨

정리:

```bash
curl -fsS -X DELETE "$BASE_URL/api/v1/admin/flowers/<SMOKE_FLOWER_ID>" \
  -H "Authorization: Bearer <ADMIN_ACCESS_TOKEN>"
```

## 5. OpenAI Message Generation

```bash
curl -fsS -X POST "$BASE_URL/api/v1/messages/generate" \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "flowerId": <ACTIVE_FLOWER_ID>,
    "selectedTagIds": [],
    "curationResultId": null,
    "senderName": "운영점검",
    "receiverName": "수신자"
  }'
```

기대 결과:

- HTTP 200
- 응답에 생성된 `message`가 있음
- 서버 로그에 OpenAI 인증/timeout 오류가 반복되지 않음

## 6. OpenAI Fallback

이 항목은 운영이 아니라 staging에서만 수행합니다.

1. staging에서 `OPENAI_BASE_URL`을 실패 응답을 주는 endpoint로 바꾸거나 `OPENAI_API_KEY`를 제거합니다.
2. 앱을 재기동합니다.
3. 5번 메시지 생성 요청을 다시 보냅니다.

기대 결과:

- HTTP 200
- template fallback 메시지가 저장됨
- 사용자 API 실패로 전파되지 않음

## 7. Dummy Image URL 점검

운영 DB에서 확인:

```sql
SELECT id, name, image_url
FROM flowers
WHERE image_url LIKE 'https://cdn.meanhwa.example/%';
```

기대 결과:

- 0행
- 결과가 있으면 CMS에서 이미지를 다시 업로드하고 반환된 S3/CDN URL로 교체
