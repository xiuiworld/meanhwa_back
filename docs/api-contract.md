# Meanhwa Backend API Contract

현재 백엔드 코드 기준의 프론트엔드 API 계약입니다. 모든 경로는 `/api/v1` 아래에 있습니다.

## 공통 응답

성공:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {}
}
```

생성/업로드 API는 HTTP `201`과 body `status: 201`을 반환합니다.

에러:

```json
{
  "status": 400,
  "errorCode": "INVALID_REQUEST",
  "message": "요청값이 올바르지 않습니다."
}
```

`data`는 `null`이면 응답에서 생략됩니다.

## 인증 규칙

Bearer 토큰:

```http
Authorization: Bearer {accessToken}
```

Public:

```http
GET  /flowers
GET  /flowers/{flowerId}
GET  /tags
POST /curation/results
POST /auth/**
POST /action-logs/curation-result-click
```

로그인 필요:

```http
POST   /messages/generate
GET    /users/me
GET    /users/me/messages
GET    /users/me/curation-results
GET    /users/me/curation-results/latest
GET    /users/me/curation-results/{resultId}
GET    /users/me/likes
POST   /users/me/likes/{flowerId}
DELETE /users/me/likes/{flowerId}
GET    /users/me/histories
DELETE /users/me/histories
```

관리자 필요:

```http
/admin/**
```

관리자 API는 JWT의 권한이 `ROLE_ADMIN`이어야 합니다.

## Enum

```ts
type ManagementLevel = "EASY" | "NORMAL" | "HARD";
type PriceRange = "LOW" | "MEDIUM" | "HIGH" | "PREMIUM";
type TagCategory =
  | "EVENT"
  | "RELATION"
  | "EMOTION"
  | "MEANING"
  | "STYLE"
  | "CARE"
  | "SEASON"
  | "ENVIRONMENT";
type Role = "ROLE_USER" | "ROLE_ADMIN";
type OAuthProvider = "DEV" | "KAKAO" | "NAVER";
type CurationStepKey =
  | "OCCASION"
  | "RECIPIENT"
  | "EMOTION"
  | "FLOWER_MEANING"
  | "SPACE"
  | "BUDGET";
```

큐레이션 위저드의 `code`/`label` 전체 표는 [curation-wizard-api.md](curation-wizard-api.md)를 봅니다.

## 에러 코드

| errorCode | HTTP | 의미 |
| --- | ---: | --- |
| `INVALID_REQUEST` | 400 | 요청 body, query, path 값 오류 |
| `INVALID_PRICE_RANGE` | 400 | 큐레이션 `priceRange` 값 오류 |
| `INVALID_FLOWER_FILTER` | 400 | 꽃 도감 필터 값 오류 |
| `INVALID_CURATION_STEP` | 400 | 위저드 step key 오류 |
| `INVALID_CURATION_SELECTION` | 400 | 위저드 code 또는 분기 조합 오류 |
| `INCOMPLETE_CURATION_SELECTION` | 400 | 위저드 결과 요청에 6단계 선택 누락 |
| `UNAUTHORIZED` | 401 | 로그인 필요 |
| `INVALID_TOKEN` | 401 | 만료, 위조, 폐기, 타입 불일치 토큰 |
| `INVALID_OAUTH_TOKEN` | 401 | Kakao/Naver 토큰 검증 실패 |
| `FORBIDDEN` | 403 | 권한 부족 |
| `FLOWER_NOT_FOUND` | 404 | 꽃/식물 없음 또는 soft delete |
| `TAG_NOT_FOUND` | 404 | 태그 없음 또는 soft delete |
| `RESOURCE_NOT_FOUND` | 404 | 요청 경로에 매핑된 API 없음 |
| `USER_NOT_FOUND` | 404 | 회원 없음 |
| `CURATION_RESULT_NOT_FOUND` | 404 | 내 큐레이션 결과 없음 또는 소유자 불일치 |
| `CURATION_FLOW_NOT_FOUND` | 404 | 알 수 없는 `flowVersion` |
| `DUPLICATE_TAG` | 409 | 활성 태그 중복 |
| `INVALID_MAPPING` | 400 | 꽃-태그 매핑 요청 오류 |
| `INVALID_FILE_TYPE` | 400 | jpeg/png/webp가 아닌 이미지 |
| `FILE_TOO_LARGE` | 400 | 업로드 제한 초과 |
| `UPLOAD_FAILED` | 500 | 스토리지 업로드 실패 |
| `UNSUPPORTED_OAUTH_PROVIDER` | 400 | 지원하지 않는 provider 또는 prod dev login |
| `CANNOT_CHANGE_OWN_ROLE` | 400 | 관리자가 자기 권한 변경 시도 |
| `LAST_ADMIN_CANNOT_BE_DEMOTED` | 400 | 마지막 관리자 강등 시도 |
| `MESSAGE_GENERATION_RATE_LIMIT_EXCEEDED` | 429 | 메시지 생성 quota 초과 |
| `INTERNAL_SERVER_ERROR` | 500 | 서버 내부 오류 |

## Auth

### POST `/auth/login/{provider}`

`provider`: `kakao`, `naver`

Request:

```json
{
  "accessToken": "{providerAccessToken}"
}
```

Response `data`:

```json
{
  "accessToken": "{meanhwaAccessToken}",
  "refreshToken": "{meanhwaRefreshToken}",
  "tokenType": "Bearer",
  "expiresInSeconds": 1800
}
```

### POST `/auth/login/dev`

Local/test 개발용 로그인입니다. `prod` profile에서는 거부됩니다.

```json
{
  "oauthId": "dev-user-1",
  "email": "dev@example.com",
  "nickname": "민화유저",
  "role": "ROLE_USER"
}
```

### POST `/auth/refresh`

Refresh token은 성공 시 회전되고 기존 토큰은 폐기됩니다.

```json
{
  "refreshToken": "{refreshToken}"
}
```

### POST `/auth/logout`

```json
{
  "refreshToken": "{refreshToken}"
}
```

성공 시 `data` 없이 공통 성공 응답을 반환합니다.

## Flower Dictionary

### GET `/flowers`

Query:

| Name | Type | Default | 설명 |
| --- | --- | --- | --- |
| `keyword` | string | null | `name`, `coreMeaning`, `description` 검색 |
| `priceRange` | enum | null | `LOW`, `MEDIUM`, `HIGH`, `PREMIUM` |
| `isPetSafe` | boolean string | null | `true`, `false`; 생략 시 전체 |
| `managementLevel` | enum | null | `EASY`, `NORMAL`, `HARD` |
| `tagIds` | number[] | empty | 반복 query. 여러 개면 AND 조건 |
| `page` | number | 0 | zero-based |
| `size` | number | 20 | page size |

Response `data`:

```json
{
  "content": [
    {
      "id": 1,
      "name": "장미",
      "imageUrl": "https://cdn.meanhwa.example/flowers/rose.jpg",
      "coreMeaning": "사랑과 열정",
      "description": "선명한 색과 풍성한 꽃잎으로 마음을 직접적으로 전하기 좋은 대표적인 꽃입니다.",
      "managementLevel": "NORMAL",
      "isPetSafe": true,
      "priceRange": "MEDIUM"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 20,
  "totalPages": 1,
  "hasNext": false
}
```

`keyword`가 있으면 `DICTIONARY_SEARCH` 로그가 기록됩니다. 꽃 목록 cache key에는 모든 필터, 정렬된 `tagIds`, `page`, `size`가 포함됩니다.

### GET `/flowers/{flowerId}`

Response `data`:

```json
{
  "id": 1,
  "name": "장미",
  "imageUrl": "https://cdn.meanhwa.example/flowers/rose.jpg",
  "coreMeaning": "사랑과 열정",
  "description": "선명한 색과 풍성한 꽃잎으로 마음을 직접적으로 전하기 좋은 대표적인 꽃입니다.",
  "managementLevel": "NORMAL",
  "isPetSafe": true,
  "priceRange": "MEDIUM",
  "tags": [
    {
      "id": 5,
      "category": "RELATION",
      "name": "연인"
    }
  ]
}
```

항상 `FLOWER_DETAIL_VIEW` 로그를 기록합니다. 유효한 Bearer 토큰이 있으면 사용자 최근 본 식물도 저장합니다.

## Tags

### GET `/tags`

카테고리별 활성 태그를 반환합니다.

```json
[
  {
    "category": "EVENT",
    "tags": [
      {
        "id": 1,
        "name": "생일"
      }
    ]
  }
]
```

## Curation

### POST `/curation/results`

인증은 optional입니다. 프론트는 로컬 큐레이션 플로우에서 고른 `{ step, code }`를 전송합니다. 유효한 Bearer 토큰이 있으면 결과 snapshot을 사용자 이력으로 저장하고, 익명 요청은 저장하지 않습니다.

Request:

```json
{
  "flowVersion": "2026-05-v1",
  "selections": [
    { "step": "OCCASION", "code": "BIRTHDAY" },
    { "step": "RECIPIENT", "code": "LOVER" },
    { "step": "EMOTION", "code": "LOVE" },
    { "step": "FLOWER_MEANING", "code": "LOVE_3" },
    { "step": "SPACE", "code": "DESK_SMALL" },
    { "step": "BUDGET", "code": "BUDGET_MEDIUM" }
  ],
  "page": 0,
  "size": 20
}
```

규칙:

- 정확히 6개 step이 필요합니다.
- 순서는 무관하지만 step 중복은 허용하지 않습니다.
- Step 1-5 `code`는 tag scoring에 사용됩니다.
- Step 6 `BUDGET_*`는 `PriceRange` 필터로만 사용됩니다.
- 정렬은 `score` desc, `name` asc입니다.

Response `data`:

```json
{
  "content": [
    {
      "flowerId": 1,
      "name": "장미",
      "imageUrl": "https://cdn.meanhwa.example/flowers/rose.jpg",
      "coreMeaning": "사랑과 열정",
      "priceRange": "MEDIUM",
      "isPetSafe": true,
      "score": 10,
      "recommendationReason": "연인 조건과 잘 맞고 5~10만원 예산대에 어울리는 추천입니다.",
      "matchedTags": [
        {
          "id": 5,
          "category": "RELATION",
          "name": "연인"
        }
      ]
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "hasNext": false,
  "curationResultId": 12
}
```

로그인 사용자로 저장에 성공하면 `curationResultId`는 저장된 결과 ID입니다. 익명 요청이면 저장하지 않으며 `curationResultId`는 `null`입니다.

## Messages

### POST `/messages/generate`

로그인 필요. 사용자별 rate limit 기본값은 1시간 10회입니다.

Request:

```json
{
  "flowerId": 1,
  "selectedTagIds": [5, 9],
  "curationResultId": 12,
  "senderName": "민수",
  "receiverName": "지은"
}
```

`curationResultId`는 optional이며, 전달하면 현재 로그인 사용자의 결과여야 합니다.

Response `data`:

```json
{
  "id": 101,
  "flowerId": 1,
  "flowerName": "장미",
  "flowerImageUrl": "https://cdn.meanhwa.example/flowers/rose.jpg",
  "coreMeaning": "사랑과 열정",
  "selectedTags": [
    {
      "id": 5,
      "category": "RELATION",
      "name": "연인"
    }
  ],
  "curationResultId": 12,
  "senderName": "민수",
  "receiverName": "지은",
  "message": "지은님께...",
  "createdAt": "2026-05-22T14:30:00"
}
```

`OPENAI_API_KEY`가 있으면 OpenAI를 먼저 시도합니다. 키 없음, timeout, non-2xx, 응답 파싱 실패, client exception은 template fallback으로 성공 처리됩니다. 성공한 생성 결과는 fallback 여부와 관계없이 저장됩니다.

### GET `/users/me/messages`

로그인 사용자의 메시지 이력을 최신순으로 반환합니다.

Query: `page=0`, `size=20` 기본값. `size`는 100으로 cap 됩니다.

## My Page

### GET `/users/me`

Response `data`:

```json
{
  "id": 1,
  "provider": "KAKAO",
  "oauthId": "123456789",
  "email": "user@example.com",
  "nickname": "민화유저",
  "role": "ROLE_USER"
}
```

### Likes

```http
GET    /users/me/likes
POST   /users/me/likes/{flowerId}
DELETE /users/me/likes/{flowerId}
```

Likes are idempotent and scoped to the current user. List responses are arrays of `FlowerSummaryResponse`.

### Histories

```http
GET    /users/me/histories
DELETE /users/me/histories
```

Authenticated flower detail views update history. The same flower is deduplicated by updating `viewedAt`, and histories are capped at 50.

### Saved curation results

```http
GET /users/me/curation-results
GET /users/me/curation-results/latest
GET /users/me/curation-results/{resultId}
```

List response item:

```json
{
  "id": 12,
  "flowVersion": "2026-05-v1",
  "selections": [
    { "step": "OCCASION", "code": "BIRTHDAY", "label": "생일" }
  ],
  "topFlowers": [
    {
      "rank": 1,
      "flowerId": 1,
      "name": "장미",
      "imageUrl": "https://cdn.meanhwa.example/flowers/rose.jpg",
      "coreMeaning": "사랑과 열정"
    }
  ],
  "resultCount": 20,
  "createdAt": "2026-05-22T14:00:00"
}
```

`latest`와 `{resultId}`는 저장된 `recommendations` snapshot 전체를 반환합니다. 소유자가 다르면 `CURATION_RESULT_NOT_FOUND`입니다.

## Action Logs

서버 자동 로그는 성공 응답 후 best-effort로 저장됩니다.

| ActionType | Trigger |
| --- | --- |
| `CURATION_START` | `POST /curation/results` |
| `DICTIONARY_SEARCH` | `GET /flowers?keyword=...` |
| `FLOWER_DETAIL_VIEW` | `GET /flowers/{flowerId}` |
| `ADMIN_USER_ROLE_CHANGE` | `PUT /admin/users/{userId}/role` |

프론트 클릭 로그:

```http
POST /action-logs/curation-result-click
```

Request:

```json
{
  "flowerId": 1,
  "tagIds": [5, 9],
  "rank": 1,
  "score": 10,
  "source": "curation-v2",
  "flowVersion": "2026-05-v1",
  "selections": [
    { "step": "OCCASION", "code": "BIRTHDAY" }
  ]
}
```

인증은 optional입니다. 유효한 Bearer 토큰이 있으면 `userId`가 저장되고, 없으면 anonymous 로그로 저장됩니다.

## Admin

모든 admin endpoint는 `ROLE_ADMIN`이 필요합니다.

### Flowers

```http
POST   /admin/flowers
GET    /admin/flowers/{flowerId}
PUT    /admin/flowers/{flowerId}
DELETE /admin/flowers/{flowerId}
PUT    /admin/flowers/{flowerId}/tags
```

Create/update request:

```json
{
  "name": "관리자테스트꽃",
  "imageUrl": "https://cdn.meanhwa.example/admin-test.jpg",
  "coreMeaning": "처음 의미",
  "description": "꽃 상세정보 문구",
  "managementLevel": "EASY",
  "isToxicToPets": false,
  "priceRange": "LOW"
}
```

`POST /admin/flowers`는 HTTP `201`입니다. 삭제는 soft delete입니다. 관리자 생성/수정/삭제 시 `created_by`, `updated_by`가 기록됩니다.

Replace mappings:

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

`weight`는 1-5입니다. 요청은 기존 매핑 전체를 교체합니다.

### Tags

```http
POST   /admin/tags
PUT    /admin/tags/{tagId}
DELETE /admin/tags/{tagId}
```

Request:

```json
{
  "category": "EVENT",
  "name": "기념일"
}
```

`POST /admin/tags`는 HTTP `201`입니다. Admin tag API는 `code`를 받지 않습니다. 위저드용 `tags.code`는 로컬 `data.sql`과 Flyway `V2__seed_curation_reference_data.sql`로 관리합니다.

### Uploads

```http
POST /admin/uploads/images
Content-Type: multipart/form-data
```

Field: `file`

허용 content type: `image/jpeg`, `image/png`, `image/webp`

기본 최대 크기: 5 MB

성공 응답: HTTP `201`

### Users

```http
GET /admin/users?keyword=&provider=&role=&page=0&size=20
GET /admin/users/{userId}
PUT /admin/users/{userId}/role
```

Role update request:

```json
{
  "role": "ROLE_ADMIN"
}
```

규칙:

- 본인 role은 변경할 수 없습니다.
- 마지막 남은 `ROLE_ADMIN`은 `ROLE_USER`로 내릴 수 없습니다.
- 변경 후 대상 사용자는 다시 로그인해야 새 JWT에 role이 반영됩니다.

### Statistics

```http
GET /admin/statistics/summary
GET /admin/statistics/popular-tags
GET /admin/statistics/popular-flowers
GET /admin/statistics/daily-active-users
```

Query:

| Param | Format | Default |
| --- | --- | --- |
| `from` | `yyyy-MM-dd` | `to` 기준 29일 전 |
| `to` | `yyyy-MM-dd` | 오늘 |
| `limit` | number | 10, max 100 for popular APIs |

## Cache and Rate Limit

Cached endpoints:

```http
GET /flowers
GET /tags
```

Admin flower/tag mutations evict related caches. Local/test use simple cache. Prod can use Redis through `CACHE_TYPE=redis`.

Message generation rate limit:

| Setting | Default |
| --- | --- |
| `app.message.rate-limit.enabled` | `true` |
| `app.message.rate-limit.max-requests` | `10` |
| `app.message.rate-limit.window` | `1h` |
| `app.message.rate-limit.store` | `memory` local/test, `redis` prod |
