# Meanhwa API Contract - Phase 6-7

This document covers backend contracts for frontend Phase F7 admin CMS and Phase F8 image upload/S3 integration.

## Current Backend Status

Phase 6 and Phase 7 are implemented.

- Admin APIs are under `/api/v1/admin/**`.
- Admin APIs require `Authorization: Bearer {accessToken}` with `ROLE_ADMIN`.
- A regular logged-in user receives `403 FORBIDDEN`.
- An anonymous user receives `401 UNAUTHORIZED`.
- Flower and tag deletion uses soft delete.
- Soft-deleted flowers/tags are hidden from public APIs:
  - `GET /api/v1/flowers`
  - `GET /api/v1/flowers/{flowerId}`
  - `GET /api/v1/tags`
  - `GET /api/v1/curation`
  - `POST /api/v1/messages/generate`
- Local/test image upload uses fake storage.
- Prod/S3 profiles use AWS S3 through the AWS SDK default credential chain.

Verified command:

```powershell
$env:JAVA_HOME='C:\Users\xiuiw\.jdks\corretto-21.0.11'
.\gradlew.bat test
```

Result: `BUILD SUCCESSFUL`

## Frontend Scope for F7-F8

Phase F7 should build an admin tool surface, not a marketing/card UI.

Recommended routes:

```text
/admin/flowers
/admin/flowers/new
/admin/flowers/[flowerId]/edit
/admin/tags
```

Phase F8 should extend the flower create/edit form with image upload.

Recommended CMS flow:

1. Load admin auth state with `GET /api/v1/users/me`.
2. Allow access only when `data.role === "ROLE_ADMIN"`.
3. Load active flowers with `GET /api/v1/flowers?page=0&size=...`.
4. Load active tags with `GET /api/v1/tags`.
5. For edit, load detail with `GET /api/v1/flowers/{flowerId}`.
6. If the admin selects a new image, call `POST /api/v1/admin/uploads/images`.
7. Put returned `imageUrl` into the flower create/update request.
8. Save flower metadata with admin flower API.
9. Save tag weights with `PUT /api/v1/admin/flowers/{flowerId}/tags`.

Important limitation:

- There is no admin-only list endpoint yet.
- There is no restore endpoint or deleted-data list.
- The admin table should use active data from public list/detail endpoints for now.

## Common Rules

Auth header:

```http
Authorization: Bearer {accessToken}
```

Success response wrapper:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {}
}
```

Create/upload responses return HTTP `201` and body `status: 201`.

Error response wrapper:

```json
{
  "status": 400,
  "errorCode": "INVALID_REQUEST",
  "message": "요청값이 올바르지 않습니다."
}
```

Admin-related error codes:

| errorCode | HTTP | Frontend meaning |
| --- | ---: | --- |
| `UNAUTHORIZED` | 401 | Login required |
| `INVALID_TOKEN` | 401 | Token expired, invalid, revoked, or wrong type |
| `FORBIDDEN` | 403 | Logged in but not admin |
| `FLOWER_NOT_FOUND` | 404 | Flower does not exist or is soft-deleted |
| `TAG_NOT_FOUND` | 404 | Tag does not exist or is soft-deleted |
| `DUPLICATE_TAG` | 409 | Active tag already exists with same category and name |
| `INVALID_MAPPING` | 400 | Duplicate tag id, invalid tag id, or invalid mapping request |
| `INVALID_FILE_TYPE` | 400 | Uploaded file is not jpeg/png/webp |
| `FILE_TOO_LARGE` | 400 | Uploaded file is larger than max size |
| `UPLOAD_FAILED` | 500 | S3 upload failed |

Enums currently used by forms:

```ts
type ManagementLevel = "EASY" | "NORMAL" | "HARD";
type PriceRange = "LOW" | "MEDIUM" | "HIGH" | "PREMIUM";
type TagCategory = "EVENT" | "RELATION" | "EMOTION" | "STYLE" | "CARE";
```

## Phase F7: Admin CMS APIs

### Existing Read APIs to Reuse

Admin screens should reuse the current public read APIs for active data.

#### GET /api/v1/flowers

Use this for the flower table.

```http
GET /api/v1/flowers?keyword=장미&page=0&size=20
```

Response data shape:

```json
{
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
  "totalElements": 1,
  "totalPages": 1,
  "hasNext": false
}
```

#### GET /api/v1/flowers/{flowerId}

Use this before opening an edit form.

Response data shape:

```json
{
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
```

Note the read response uses `isPetSafe`, but admin write requests use `isToxicToPets`.

#### GET /api/v1/tags

Use this for tag management and mapping selectors.

Response data shape:

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

### POST /api/v1/admin/flowers

Creates a flower.

Request:

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

Field rules:

| Field | Required | Notes |
| --- | --- | --- |
| `name` | yes | max 100 chars |
| `imageUrl` | no | max 255 chars; use upload result URL for F8 |
| `coreMeaning` | no | max 100 chars |
| `managementLevel` | yes | `EASY`, `NORMAL`, `HARD` |
| `managementInfo` | no | long text |
| `isToxicToPets` | yes | inverse of read-side `isPetSafe` |
| `priceRange` | yes | `LOW`, `MEDIUM`, `HIGH`, `PREMIUM` |

Response:

```json
{
  "status": 201,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "id": 13,
    "name": "관리자테스트꽃",
    "imageUrl": "https://cdn.meanhwa.example/admin-test.jpg",
    "coreMeaning": "처음 의미",
    "managementLevel": "EASY",
    "managementInfo": "관리자 테스트 관리법",
    "isPetSafe": true,
    "priceRange": "LOW",
    "tags": []
  }
}
```

Frontend behavior:

- Create the flower first.
- Then call `PUT /api/v1/admin/flowers/{flowerId}/tags` if the form includes tag weights.

### PUT /api/v1/admin/flowers/{flowerId}

Updates flower metadata.

Request body is the same as create.

Response body is the same `FlowerDetailResponse`.

Soft-deleted or missing flower:

```json
{
  "status": 404,
  "errorCode": "FLOWER_NOT_FOUND",
  "message": "꽃/식물 정보를 찾을 수 없습니다."
}
```

### DELETE /api/v1/admin/flowers/{flowerId}

Soft-deletes a flower.

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다."
}
```

Frontend behavior:

- Show a confirmation modal.
- After success, remove the row from the admin table or refetch `GET /api/v1/flowers`.
- A deleted flower is no longer returned by public list/detail/curation APIs.
- Existing likes and histories are preserved in DB, but hidden from read responses.

### POST /api/v1/admin/tags

Creates a tag.

Request:

```json
{
  "category": "EVENT",
  "name": "기념일"
}
```

Response:

```json
{
  "status": 201,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "id": 17,
    "category": "EVENT",
    "name": "기념일"
  }
}
```

Duplicate active tag:

```json
{
  "status": 409,
  "errorCode": "DUPLICATE_TAG",
  "message": "이미 존재하는 태그입니다."
}
```

Duplicate rule:

- Same `category + name`, case-insensitive, among active tags.
- Soft-deleted tags do not block recreating the same category/name.

### PUT /api/v1/admin/tags/{tagId}

Updates a tag category/name.

Request:

```json
{
  "category": "EMOTION",
  "name": "격려"
}
```

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "id": 17,
    "category": "EMOTION",
    "name": "격려"
  }
}
```

### DELETE /api/v1/admin/tags/{tagId}

Soft-deletes a tag.

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다."
}
```

Frontend behavior:

- Show a confirmation modal.
- After success, refetch `GET /api/v1/tags`.
- A deleted tag no longer appears in tag selectors.
- Curation with a deleted tag id returns `TAG_NOT_FOUND`.

### PUT /api/v1/admin/flowers/{flowerId}/tags

Replaces all tag mappings for a flower.

Request:

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

Field rules:

| Field | Required | Notes |
| --- | --- | --- |
| `tags` | yes | array, may be empty to clear all mappings |
| `tagId` | yes | must be an active tag id |
| `weight` | yes | integer from 1 to 5 |

Validation:

- Duplicate `tagId` values return `INVALID_MAPPING`.
- Non-positive or missing `tagId` returns `INVALID_MAPPING` or `INVALID_REQUEST`.
- Missing/deleted tag id returns `TAG_NOT_FOUND`.
- Missing/deleted flower id returns `FLOWER_NOT_FOUND`.

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "id": 13,
    "name": "관리자테스트꽃",
    "imageUrl": "https://cdn.meanhwa.example/admin-test.jpg",
    "coreMeaning": "처음 의미",
    "managementLevel": "EASY",
    "managementInfo": "관리자 테스트 관리법",
    "isPetSafe": true,
    "priceRange": "LOW",
    "tags": [
      {
        "id": 1,
        "category": "EVENT",
        "name": "생일"
      },
      {
        "id": 10,
        "category": "EMOTION",
        "name": "감사"
      }
    ]
  }
}
```

Important response limitation:

- The response includes tag ids/category/name.
- It does not currently include mapping `weight`.
- The frontend should keep edited weights in local form state after save, or refetch only to confirm selected tags.

## Phase F8: Image Upload API

### POST /api/v1/admin/uploads/images

Uploads one image file.

Request:

```http
POST /api/v1/admin/uploads/images
Authorization: Bearer {adminAccessToken}
Content-Type: multipart/form-data
```

Form field:

```text
file
```

Allowed content types:

```text
image/jpeg
image/png
image/webp
```

Max file size:

```text
5 MB by default
```

Response:

```json
{
  "status": 201,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "imageUrl": "https://fake.meanhwa.local/uploads/flowers/89aaa338-eab2-46a9-bb93-910c1a1d6d31.png",
    "originalFilename": "flower.png",
    "contentType": "image/png",
    "size": 10
  }
}
```

Frontend behavior:

- Validate obvious file type/size client-side for fast feedback.
- Still handle backend `INVALID_FILE_TYPE` and `FILE_TOO_LARGE`.
- Show upload progress if the frontend HTTP client supports it.
- On success, preview `data.imageUrl`.
- Put `data.imageUrl` into `POST/PUT /api/v1/admin/flowers` as `imageUrl`.
- If upload succeeds but flower save fails, keep the uploaded URL in the form so the admin can retry saving.

## Local Admin Test Login

Use dev login in local/test only.

```http
POST /api/v1/auth/login/dev
Content-Type: application/json
```

```json
{
  "oauthId": "dev-admin-1",
  "email": "admin@example.com",
  "nickname": "민화관리자",
  "role": "ROLE_ADMIN"
}
```

Use the returned access token:

```http
Authorization: Bearer {accessToken}
```

Production note:

- `dev` login is rejected in the `prod` profile.
- Real Kakao/Naver OAuth is still a later phase.

## UX Notes to Give Frontend

- Admin pages should be dense and operational: table, filters, forms, confirmation modals.
- Keep `/admin/**` separate from public visual styling. It should prioritize scanning and editing.
- Gate all admin pages by user role. If the user is not logged in, route to login. If logged in but not admin, show an access-denied state.
- For flower create/edit, split the form into:
  - basic metadata
  - image upload/preview
  - tag weights
- Save order should be image upload first, then flower metadata, then mapping replacement.
- Because mapping response does not include weights, preserve submitted weights in the client state after save.
- Deletion is soft delete. Use "삭제" in UI, but the user-facing effect is "hide from service".
- After any create/update/delete, refetch active flower/tag lists so public and admin views stay consistent.
- Do not show upload internals such as bucket, region, key, or AWS credentials in the UI.
