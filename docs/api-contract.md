# Meanhwa Backend API Contract

This is the consolidated frontend-facing API contract for the Meanhwa backend after Phase 11. The phase-specific documents are preserved for historical context; this file is the current single reference.

## Base Rules

Base path:

```text
/api/v1
```

Success response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {}
}
```

Create and upload APIs return HTTP `201` and body `status: 201`.

Error response:

```json
{
  "status": 400,
  "errorCode": "INVALID_REQUEST",
  "message": "요청값이 올바르지 않습니다."
}
```

Common error codes:

| errorCode | HTTP | Meaning |
| --- | ---: | --- |
| `INVALID_REQUEST` | 400 | Invalid parameter or request body |
| `INVALID_PRICE_RANGE` | 400 | Unsupported `priceRange` |
| `UNAUTHORIZED` | 401 | Login required |
| `INVALID_TOKEN` | 401 | Expired, malformed, revoked, or wrong token type |
| `INVALID_OAUTH_TOKEN` | 401 | Kakao/Naver token rejected or profile invalid |
| `FORBIDDEN` | 403 | Logged in but role is not allowed |
| `FLOWER_NOT_FOUND` | 404 | Flower missing or soft-deleted |
| `TAG_NOT_FOUND` | 404 | Tag missing or soft-deleted |
| `DUPLICATE_TAG` | 409 | Active tag already exists |
| `INVALID_MAPPING` | 400 | Invalid or duplicate flower-tag mapping |
| `INVALID_FILE_TYPE` | 400 | Uploaded file is not jpeg/png/webp |
| `FILE_TOO_LARGE` | 400 | Uploaded image is too large |
| `UPLOAD_FAILED` | 500 | Storage upload failed |
| `UNSUPPORTED_OAUTH_PROVIDER` | 400 | Unknown provider or dev login blocked |
| `USER_NOT_FOUND` | 404 | Token user no longer exists |
| `INTERNAL_SERVER_ERROR` | 500 | Unexpected server error |

Enums:

```ts
type ManagementLevel = "EASY" | "NORMAL" | "HARD";
type PriceRange = "LOW" | "MEDIUM" | "HIGH" | "PREMIUM";
type TagCategory = "EVENT" | "RELATION" | "EMOTION" | "STYLE" | "CARE";
type Role = "ROLE_USER" | "ROLE_ADMIN";
type OAuthProvider = "DEV" | "KAKAO" | "NAVER";
```

## Authentication

Public APIs:

```http
GET  /api/v1/flowers
GET  /api/v1/flowers/{flowerId}
GET  /api/v1/tags
GET  /api/v1/curation
POST /api/v1/messages/generate
POST /api/v1/auth/**
POST /api/v1/action-logs/curation-result-click
```

Authenticated user APIs require:

```http
Authorization: Bearer {accessToken}
```

Admin APIs require a token whose user has `ROLE_ADMIN`:

```http
/api/v1/admin/**
```

### POST /api/v1/auth/login/{provider}

Production providers:

```text
kakao, naver
```

Request:

```json
{
  "accessToken": "{providerAccessTokenFromFrontendSdk}"
}
```

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "accessToken": "{meanhwaAccessToken}",
    "refreshToken": "{meanhwaRefreshToken}",
    "tokenType": "Bearer",
    "expiresInSeconds": 1800
  }
}
```

Local/test dev login:

```http
POST /api/v1/auth/login/dev
```

```json
{
  "oauthId": "dev-user-1",
  "email": "dev@example.com",
  "nickname": "민화유저",
  "role": "ROLE_USER"
}
```

`dev` login is rejected in the `prod` profile.

### POST /api/v1/auth/refresh

Refresh tokens rotate on every successful refresh.

```json
{
  "refreshToken": "{refreshToken}"
}
```

Response shape is the same as login.

### POST /api/v1/auth/logout

```json
{
  "refreshToken": "{refreshToken}"
}
```

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다."
}
```

### GET /api/v1/users/me

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "id": 1,
    "provider": "KAKAO",
    "oauthId": "123456789",
    "email": "user@example.com",
    "nickname": "민화유저",
    "role": "ROLE_USER"
  }
}
```

## Flower Dictionary

### GET /api/v1/flowers

Query parameters:

| Name | Type | Required | Default | Notes |
| --- | --- | --- | --- | --- |
| `keyword` | string | no | null | Searches `name` and `coreMeaning` |
| `page` | number | no | 0 | Zero-based |
| `size` | number | no | 20 | Page size |

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "장미",
        "imageUrl": "https://cdn.meanhwa.example/flowers/rose.jpg",
        "coreMeaning": "사랑과 열정",
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
}
```

`GET /flowers?keyword=...` records `DICTIONARY_SEARCH`. Calls without keyword do not.

### GET /api/v1/flowers/{flowerId}

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "id": 1,
    "name": "장미",
    "imageUrl": "https://cdn.meanhwa.example/flowers/rose.jpg",
    "coreMeaning": "사랑과 열정",
    "managementLevel": "NORMAL",
    "managementInfo": "햇빛이 잘 드는 곳에 두고 겉흙이 마르면 물을 주세요.",
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
}
```

If a valid Bearer token is included, this endpoint records user history. It always records `FLOWER_DETAIL_VIEW` analytics.

## Tags and Curation

### GET /api/v1/tags

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": [
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
}
```

### GET /api/v1/curation

Query parameters:

| Name | Type | Required | Default | Notes |
| --- | --- | --- | --- | --- |
| `tagIds` | number[] | no | empty | Repeated query param, e.g. `tagIds=5&tagIds=9` |
| `isPetSafe` | boolean | no | null | `true` excludes toxic flowers |
| `priceRange` | string | no | null | `LOW`, `MEDIUM`, `HIGH`, `PREMIUM` |
| `page` | number | no | 0 | Zero-based |
| `size` | number | no | 20 | Page size |

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "content": [
      {
        "flowerId": 1,
        "name": "장미",
        "imageUrl": "https://cdn.meanhwa.example/flowers/rose.jpg",
        "coreMeaning": "사랑과 열정",
        "priceRange": "MEDIUM",
        "isPetSafe": true,
        "score": 10,
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
    "hasNext": false
  }
}
```

`score` is the sum of matched flower-tag weights. If `tagIds` is omitted, matching flowers are returned with `score: 0`.

## Messages

### POST /api/v1/messages/generate

Request:

```json
{
  "flowerId": 1,
  "selectedTagIds": [5, 9],
  "senderName": "민수",
  "receiverName": "지은"
}
```

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "flowerId": 1,
    "message": "지은님께..."
  }
}
```

When `OPENAI_API_KEY` is configured, the backend tries OpenAI first. Missing key, timeout, non-2xx response, malformed response, or client exceptions fall back to the template generator and still return success.

## Personalization

All endpoints require `Authorization: Bearer {accessToken}`.

```http
GET    /api/v1/users/me/likes
POST   /api/v1/users/me/likes/{flowerId}
DELETE /api/v1/users/me/likes/{flowerId}
GET    /api/v1/users/me/histories
DELETE /api/v1/users/me/histories
```

Likes are idempotent and scoped by user. Histories are written by authenticated flower detail views, deduplicated by flower, sorted newest first, and capped at 50 items.

`GET /likes` and `GET /histories` return arrays of `FlowerSummaryResponse`.

## Action Logs

Automatic logs:

| ActionType | Trigger |
| --- | --- |
| `CURATION_START` | `GET /api/v1/curation` |
| `DICTIONARY_SEARCH` | `GET /api/v1/flowers?keyword=...` |
| `FLOWER_DETAIL_VIEW` | `GET /api/v1/flowers/{flowerId}` |

Frontend click log:

```http
POST /api/v1/action-logs/curation-result-click
```

Request:

```json
{
  "flowerId": 1,
  "tagIds": [5, 9],
  "rank": 1,
  "score": 10,
  "source": "curation"
}
```

Authentication is optional. If a valid Bearer token is provided, `userId` is stored; otherwise it is stored as anonymous.

## Admin CMS

All admin endpoints require `ROLE_ADMIN`.

### POST /api/v1/admin/flowers

Creates a flower. The backend records the admin user id in `created_by` and `updated_by`.

```json
{
  "name": "관리자테스트꽃",
  "imageUrl": "https://cdn.meanhwa.example/admin-test.jpg",
  "coreMeaning": "처음 의미",
  "managementLevel": "EASY",
  "managementInfo": "관리자 테스트 관리법",
  "isToxicToPets": false,
  "priceRange": "LOW"
}
```

Response data uses `FlowerDetailResponse`.

### PUT /api/v1/admin/flowers/{flowerId}

Updates flower metadata and records `updated_by`.

### DELETE /api/v1/admin/flowers/{flowerId}

Soft-deletes the flower and records `updated_by`. Soft-deleted flowers are hidden from public reads, curation, and message generation.

### PUT /api/v1/admin/flowers/{flowerId}/tags

Replaces all mappings.

```json
{
  "tags": [
    {
      "tagId": 1,
      "weight": 5
    },
    {
      "tagId": 10,
      "weight": 3
    }
  ]
}
```

`weight` must be from 1 to 5. Duplicate `tagId` values return `INVALID_MAPPING`.

### POST /api/v1/admin/tags

```json
{
  "category": "EVENT",
  "name": "기념일"
}
```

### PUT /api/v1/admin/tags/{tagId}

Same body as create.

### DELETE /api/v1/admin/tags/{tagId}

Soft-deletes the tag. Deleted tags are hidden from public tag reads and invalid for curation/mapping.

## Image Upload

### POST /api/v1/admin/uploads/images

Multipart form field:

```text
file
```

Allowed content types:

```text
image/jpeg, image/png, image/webp
```

Default max size: 5 MB.

Response:

```json
{
  "status": 201,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "imageUrl": "https://fake.meanhwa.local/uploads/flowers/example.png",
    "originalFilename": "flower.png",
    "contentType": "image/png",
    "size": 10
  }
}
```

Local/test uses fake storage. Prod uses AWS S3 through the AWS SDK default credential chain.

## Admin Statistics

All endpoints require `ROLE_ADMIN`.

Common query params:

| Param | Required | Format | Default |
| --- | --- | --- | --- |
| `from` | no | `yyyy-MM-dd` | 29 days before `to` |
| `to` | no | `yyyy-MM-dd` | today |
| `limit` | no | integer | 10, max 100 for popular APIs |

Endpoints:

```http
GET /api/v1/admin/statistics/summary
GET /api/v1/admin/statistics/popular-tags
GET /api/v1/admin/statistics/popular-flowers
GET /api/v1/admin/statistics/daily-active-users
```

Summary response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "from": "2026-05-01",
    "to": "2026-05-08",
    "totalUsers": 14,
    "activeFlowers": 20,
    "totalLikes": 5,
    "curationCount": 23,
    "searchCount": 9,
    "detailViewCount": 31,
    "curationClickCount": 7
  }
}
```

## Caching

Cached backend endpoints:

```http
GET /api/v1/flowers
GET /api/v1/tags
```

Cache behavior is transparent to the frontend. Admin flower/tag mutations evict related caches. Local/test use simple in-memory cache; prod can use Redis.

## Production Environment Variables

Required for deployment:

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

Optional settings:

```text
JWT_ACCESS_TOKEN_VALIDITY_MINUTES=30
JWT_REFRESH_TOKEN_VALIDITY_DAYS=14
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
OPENAI_BASE_URL=https://api.openai.com
OPENAI_MODEL=gpt-5.4-mini
OPENAI_TIMEOUT_MILLIS=5000
OPENAI_MAX_OUTPUT_TOKENS=300
KAKAO_USERINFO_URL=https://kapi.kakao.com/v2/user/me
NAVER_USERINFO_URL=https://openapi.naver.com/v1/nid/me
KAKAO_TIMEOUT_MILLIS=3000
NAVER_TIMEOUT_MILLIS=3000
STORAGE_MAX_FILE_SIZE_BYTES=5242880
JPA_DDL_AUTO=update
```

No provider token, OpenAI API key, JWT secret, DB password, or AWS credential should be committed to source.

## Local Verification

Use JDK 21:

```powershell
$env:JAVA_HOME='C:\Users\xiuiw\.jdks\corretto-21.0.11'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat test --rerun-tasks
.\gradlew.bat bootJar
```

CI runs the Linux equivalent:

```bash
./gradlew test bootJar
```
