# Meanhwa 백오피스 운영 가이드

이 문서는 `/admin` 백오피스 접근, 관리자 권한 부여, CMS 운영 방식을 정리합니다.

## 관리자 로그인 구조

백오피스에는 별도의 관리자 로그인 버튼이 없습니다.

흐름:

1. 사용자가 카카오/네이버 일반 소셜 로그인
2. 프론트가 provider access token을 백엔드에 전달
3. 백엔드가 `users` row를 생성하거나 조회
4. 프론트가 `GET /api/v1/users/me` 호출
5. 응답의 `role` 확인
6. `role === "ROLE_ADMIN"`이면 `/admin` 접근 허용
7. 아니면 “관리자 권한이 필요합니다” 화면 표시

`/api/v1/admin/**` 백엔드 API도 `ROLE_ADMIN` 토큰만 허용합니다.

## 현재 사용자 권한 확인

프론트에서 로그인 후:

```http
GET /api/v1/users/me
Authorization: Bearer {accessToken}
```

관리자 응답 예시:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "id": 1,
    "provider": "NAVER",
    "oauthId": "provider-user-id",
    "email": "user@example.com",
    "nickname": "운영자",
    "role": "ROLE_ADMIN"
  }
}
```

## 운영 DB에서 관리자 승격

먼저 SSH에서 RDS에 접속합니다. 자세한 접속 방법은 [deployment.md](deployment.md)를 참고합니다.

대상 계정 확인:

```sql
SELECT id, provider, oauth_id, email, nickname, role, created_at
FROM users
WHERE provider = 'NAVER'
  AND email = 'user@example.com';
```

한 명만 조회되는지 확인한 뒤 승격:

```sql
UPDATE users
SET role = 'ROLE_ADMIN'
WHERE provider = 'NAVER'
  AND email = 'user@example.com';
```

결과 확인:

```sql
SELECT id, provider, oauth_id, email, nickname, role
FROM users
WHERE provider = 'NAVER'
  AND email = 'user@example.com';
```

`role`이 `ROLE_ADMIN`이면 완료입니다.

## 권한 변경 후 프론트 확인

1. 프론트에서 로그아웃
2. 다시 소셜 로그인
3. `/admin` 접속

계속 일반 사용자로 보이면:

- 브라우저 쿠키 삭제
- 시크릿 창에서 재로그인
- `/api/v1/users/me` 응답 확인

기존 access token에는 예전 권한이 들어 있을 수 있으므로 재로그인이 필요합니다.

## 관리자 CMS 기능

관리자 API:

```http
POST   /api/v1/admin/flowers
PUT    /api/v1/admin/flowers/{flowerId}
DELETE /api/v1/admin/flowers/{flowerId}
PUT    /api/v1/admin/flowers/{flowerId}/tags

POST   /api/v1/admin/tags
PUT    /api/v1/admin/tags/{tagId}
DELETE /api/v1/admin/tags/{tagId}

POST   /api/v1/admin/uploads/images

GET    /api/v1/admin/statistics/summary
GET    /api/v1/admin/statistics/popular-tags
GET    /api/v1/admin/statistics/popular-flowers
GET    /api/v1/admin/statistics/daily-active-users
```

## 식물 운영 정책

- 생성/수정/삭제는 `ROLE_ADMIN`만 가능합니다.
- 삭제는 hard delete가 아니라 soft delete입니다.
- 삭제된 식물은 public list/detail/curation/message generation에서 숨겨집니다.
- 관리자 생성 시 `flowers.created_by`가 기록됩니다.
- 관리자 수정/삭제 시 `flowers.updated_by`가 기록됩니다.
- 식물 이미지 URL은 업로드 API 응답의 `imageUrl`을 저장합니다.

## 태그 운영 정책

- 태그도 soft delete입니다.
- active tag 기준으로 같은 `category + name` 중복은 허용하지 않습니다.
- 삭제된 태그는 public tag list와 curation에서 제외됩니다.
- 삭제된 tag id로 큐레이션/매핑을 요청하면 `TAG_NOT_FOUND`가 발생합니다.

## 식물-태그 매핑 운영 정책

매핑 교체 API:

```http
PUT /api/v1/admin/flowers/{flowerId}/tags
```

Request:

```json
{
  "tags": [
    {
      "tagId": 1,
      "weight": 5
    }
  ]
}
```

정책:

- 요청은 해당 식물의 전체 매핑을 교체합니다.
- `tags: []`는 모든 매핑 제거입니다.
- `weight`는 1~5입니다.
- 중복 `tagId`는 `INVALID_MAPPING`입니다.

## 이미지 업로드 운영 정책

업로드 API:

```http
POST /api/v1/admin/uploads/images
Content-Type: multipart/form-data
```

form field:

```text
file
```

허용 타입:

```text
image/jpeg
image/png
image/webp
```

기본 최대 크기:

```text
5 MB
```

local/test는 fake storage, prod는 S3를 사용합니다.

## 통계 대시보드

통계 API는 `action_logs`, `users`, `flowers`, `user_likes` 등을 집계합니다.

권장 프론트 구성:

- 요약 KPI
- 인기 태그
- 인기 식물
- DAU
- 날짜 범위 필터

공통 query:

```text
from=yyyy-MM-dd
to=yyyy-MM-dd
limit=10
```

## 운영 주의사항

- 운영 DB 직접 수정 전 반드시 `SELECT`로 대상 확인
- admin 권한은 필요한 계정에만 부여
- 퇴사/권한 회수 시 `ROLE_USER`로 되돌림
- DB password, JWT secret, access token, refresh token은 문서나 채팅에 남기지 않음
