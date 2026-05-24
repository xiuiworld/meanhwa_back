# Meanhwa Backend API Contract

This is the consolidated frontend-facing API contract for the Meanhwa backend after Phase 11. The phase-specific documents are preserved for historical context; this file is the current single reference. New mypage/flower dictionary additions are also summarized separately in [api-additions-mypage-flower.md](api-additions-mypage-flower.md).

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
| `CURATION_RESULT_NOT_FOUND` | 404 | User curation result missing or owned by another user |
| `DUPLICATE_TAG` | 409 | Active tag already exists |
| `INVALID_MAPPING` | 400 | Invalid or duplicate flower-tag mapping |
| `INVALID_FLOWER_FILTER` | 400 | Invalid flower dictionary filter value |
| `INVALID_FILE_TYPE` | 400 | Uploaded file is not jpeg/png/webp |
| `FILE_TOO_LARGE` | 400 | Uploaded image is too large |
| `UPLOAD_FAILED` | 500 | Storage upload failed |
| `UNSUPPORTED_OAUTH_PROVIDER` | 400 | Unknown provider or dev login blocked |
| `USER_NOT_FOUND` | 404 | Token user no longer exists |
| `CANNOT_CHANGE_OWN_ROLE` | 400 | Admin tried to change their own role via admin user API |
| `LAST_ADMIN_CANNOT_BE_DEMOTED` | 400 | Cannot demote the only remaining `ROLE_ADMIN` |
| `MESSAGE_GENERATION_RATE_LIMIT_EXCEEDED` | 429 | Per-user message generation quota exceeded |
| `INVALID_CURATION_STEP` | 400 | Invalid wizard `stepKey` |
| `INVALID_CURATION_SELECTION` | 400 | Branch table does not allow this `code` combination |
| `INCOMPLETE_CURATION_SELECTION` | 400 | Fewer than 6 wizard steps in `POST /curation/results` |
| `CURATION_FLOW_NOT_FOUND` | 404 | Unknown `flowVersion` |
| `INTERNAL_SERVER_ERROR` | 500 | Unexpected server error |

Enums:

```ts
type ManagementLevel = "EASY" | "NORMAL" | "HARD";
type PriceRange = "LOW" | "MEDIUM" | "HIGH" | "PREMIUM";
type TagCategory = "EVENT" | "RELATION" | "EMOTION" | "MEANING" | "STYLE" | "CARE" | "SEASON" | "ENVIRONMENT";
// 큐레이션 위저드 v2는 EVENT, RELATION, EMOTION, MEANING, ENVIRONMENT만 사용. STYLE, CARE, SEASON은 레거시(태그 API·기존 매핑).
type Role = "ROLE_USER" | "ROLE_ADMIN";
type OAuthProvider = "DEV" | "KAKAO" | "NAVER";

type CurationStepKey =
  | "OCCASION"
  | "RECIPIENT"
  | "EMOTION"
  | "FLOWER_MEANING"
  | "SPACE"
  | "BUDGET";

type CurationSelection = {
  step: CurationStepKey;
  code: string;
};
```

## Authentication

Public APIs:

```http
GET  /api/v1/flowers
GET  /api/v1/flowers/{flowerId}
GET  /api/v1/tags
GET  /api/v1/curation
GET  /api/v1/curation/flow
GET  /api/v1/curation/steps/{stepKey}/options
POST /api/v1/curation/results
POST /api/v1/auth/**
POST /api/v1/action-logs/curation-result-click
```

Authenticated user APIs require:

```http
Authorization: Bearer {accessToken}
```

Authenticated user APIs include:

```http
POST /api/v1/messages/generate
GET  /api/v1/users/me/messages
GET  /api/v1/users/me/curation-results
GET  /api/v1/users/me/curation-results/latest
GET  /api/v1/users/me/curation-results/{resultId}
GET  /api/v1/users/me
GET  /api/v1/users/me/likes
POST /api/v1/users/me/likes/{flowerId}
DELETE /api/v1/users/me/likes/{flowerId}
GET  /api/v1/users/me/histories
DELETE /api/v1/users/me/histories
```

Admin APIs require a token whose user has `ROLE_ADMIN`:

```http
/api/v1/admin/**
```

Backoffice-only (not used by the public app):

```http
GET  /api/v1/admin/users
GET  /api/v1/admin/users/{userId}
PUT  /api/v1/admin/users/{userId}/role
```

Operational notes for admins: [admin-guide.md](admin-guide.md).

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
| `keyword` | string | no | null | Searches `name`, `coreMeaning`, `description`, `scientificName`, and `origin` |
| `priceRange` | string | no | null | `LOW`, `MEDIUM`, `HIGH`, `PREMIUM` |
| `isPetSafe` | boolean | no | null | `true`: pet-safe only, `false`: pet-unsafe only, omitted: all |
| `managementLevel` | string | no | null | `EASY`, `NORMAL`, `HARD` |
| `tagIds` | number[] | no | empty | Repeated query param, e.g. `tagIds=5&tagIds=9`; flower must have all requested tags |
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
        "description": "장미는 선명한 색과 풍성한 꽃잎으로 마음을 직접적으로 전하기 좋은 대표적인 꽃입니다.",
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

Filters apply before paging. `tagIds` are matched with AND semantics, and repository/cache keys must include all filters (`keyword`, `priceRange`, `isPetSafe`, `managementLevel`, sorted `tagIds`, `page`, `size`).

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
    "description": "장미는 선명한 색과 풍성한 꽃잎으로 마음을 직접적으로 전하기 좋은 대표적인 꽃입니다.",
    "scientificName": "Rosa",
    "origin": "아시아, 유럽",
    "bloomingSeason": "봄~초여름",
    "scent": "품종에 따라 은은하거나 진한 향",
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

### GET /api/v1/curation (legacy)

Deprecated for new UI. Use the [Curation wizard (v2)](#curation-wizard-v2-recommended) APIs below. This endpoint remains available for backward compatibility.

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
        "recommendationReason": "연인 조건과 잘 맞고 5~10만원 예산대에 어울리는 추천입니다. 반려동물에게도 비교적 안전한 식물입니다.",
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

`score` is the sum of matched flower-tag weights. `recommendationReason` is a short template-generated reason based on matched tags, price range, and pet safety. If `tagIds` is omitted, matching flowers are returned with `score: 0`.

Season and environment are represented as tags:

```text
SEASON: 봄, 여름, 가을, 겨울
ENVIRONMENT: 실내, 실외, 책상, 거실
```

### Curation wizard (v2, recommended)

Six-step branching curation. Store selections by stable `code` (not `label`). Full option code tables and branch matrices: [curation-wizard-api.md](curation-wizard-api.md).
Current branch rules are served from the static YAML catalog (`flow-2026-05-v1.yml`) plus `tags.code`/`flower_tag_mappings`; no `curation_option_rules` table is required for this PR.

Current `flowVersion`: `2026-05-v1`. When the backend bumps the version, reset the client wizard state.

| Step | key | Branching |
| ---: | --- | --- |
| 1 | `OCCASION` | Fixed 6 choices |
| 2 | `RECIPIENT` | By Step1 `OCCASION` |
| 3 | `EMOTION` | By Step1 `OCCASION` |
| 4 | `FLOWER_MEANING` | By Step3 `EMOTION` (4 sub-messages per emotion) |
| 5 | `SPACE` | Fixed 4 choices |
| 6 | `BUDGET` | Fixed 4 choices → maps to `PriceRange` filter |

#### GET /api/v1/curation/flow

Flow metadata for offline cache or Storybook.

Response `data`:

```json
{
  "flowVersion": "2026-05-v1",
  "totalSteps": 6,
  "steps": [
    {
      "key": "OCCASION",
      "order": 1,
      "defaultQuestionTitle": "어떤 날인가요?",
      "defaultQuestionSubtitle": "선물을 드리는 상황을 골라주세요.",
      "selectionMode": "SINGLE",
      "dependsOn": []
    }
  ]
}
```

Query: optional `flowVersion`. Unknown version → `404` `CURATION_FLOW_NOT_FOUND`.

#### GET /api/v1/curation/steps/{stepKey}/options

Returns allowed options for one step plus dynamic question copy.

Path `stepKey`: `OCCASION` | `RECIPIENT` | `EMOTION` | `FLOWER_MEANING` | `SPACE` | `BUDGET`

Query parameters:

| Name | Type | Required | Notes |
| --- | --- | --- | --- |
| `flowVersion` | string | no | Defaults to latest |
| `selections` | string | conditional | URL-encoded JSON array of prior `{ step, code }`; current/future steps are rejected |

`selections` example (requesting Step3 `EMOTION` after birthday + lover):

```json
[
  { "step": "OCCASION", "code": "BIRTHDAY" },
  { "step": "RECIPIENT", "code": "LOVER" }
]
```

| stepKey | Prior `selections` required |
| --- | --- |
| `OCCASION` | No |
| `RECIPIENT` | `OCCASION` |
| `EMOTION` | `OCCASION` |
| `FLOWER_MEANING` | `OCCASION`, `RECIPIENT`, `EMOTION` |
| `SPACE`, `BUDGET` | No (question text may still reflect Step2) |

Response `data`:

```json
{
  "flowVersion": "2026-05-v1",
  "step": "RECIPIENT",
  "order": 2,
  "questionTitle": "누구에게 전하는 선물인가요?",
  "questionSubtitle": "받으실 분을 선택해 주세요.",
  "options": [
    {
      "code": "LOVER",
      "label": "연인",
      "tagId": 5,
      "description": null
    }
  ]
}
```

| Field | Notes |
| --- | --- |
| `options[].code` | Persist in client state |
| `options[].label` | Display only |
| `options[].tagId` | DB `tags.id` when mapped; may be `null` before seed |
| `options[].description` | Optional helper text |

Step4 `questionTitle` may change by Step2 `RECIPIENT` (e.g. `FAMILY` → “가족에게 전달하고 싶은 꽃말은 무엇인가요?”).

#### POST /api/v1/curation/results

Final ranked flower list after all 6 steps.

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

Validation:

- Exactly 6 selections, unique `step`, any order.
- Invalid branch combination → `400` `INVALID_CURATION_SELECTION`.
- Missing step → `400` `INCOMPLETE_CURATION_SELECTION`.
- Step6 `BUDGET_*` applies `flowers.price_range` filter only (not tag score).

Scoring (same engine as legacy curation):

| Input | Processing |
| --- | --- |
| Steps 1–5 codes | Resolve `tags.code` → `tagId`, sum `flower_tag_mappings.weight` |
| Step 6 `BUDGET_*` | `BUDGET_LOW`→`LOW`, `BUDGET_MEDIUM`→`MEDIUM`, `BUDGET_HIGH`→`HIGH`, `BUDGET_PREMIUM`→`PREMIUM` |
| Sort | `score` desc, then `name` asc |

Response `data` keeps the existing page fields (`content`, `page`, `size`, `totalElements`, `totalPages`, `hasNext`). When a valid Bearer token is included and the snapshot is saved, the response also includes `curationResultId` (the saved row id). Anonymous requests and failed saves omit the field.

Example `data` (logged in, saved):

```json
{
  "curationResultId": 12,
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
  "totalElements": 3,
  "totalPages": 1,
  "hasNext": false
}
```

For message creation after the wizard, clients can pass `data.curationResultId` directly to `POST /api/v1/messages/generate` without calling `GET /api/v1/users/me/curation-results/latest`. The `latest` endpoint remains available for the message tab and other screens that need the most recent saved result on entry.

Wizard-specific errors (in addition to `TAG_NOT_FOUND`, `INVALID_PRICE_RANGE`):

| HTTP | errorCode | When |
| --- | --- | --- |
| 400 | `INVALID_CURATION_STEP` | Bad `stepKey` |
| 400 | `INVALID_CURATION_SELECTION` | Code not allowed for branch |
| 400 | `INCOMPLETE_CURATION_SELECTION` | Not 6 steps on results |
| 404 | `CURATION_FLOW_NOT_FOUND` | Bad `flowVersion` |
| 404 | `CURATION_RESULT_NOT_FOUND` | User curation result missing or owned by another user |

#### Saved curation results

All saved curation result APIs require `Authorization: Bearer {accessToken}` and return only the current user's data.

```http
GET /api/v1/users/me/curation-results
GET /api/v1/users/me/curation-results/latest
GET /api/v1/users/me/curation-results/{resultId}
```

`GET /api/v1/users/me/curation-results` returns a newest-first page of result summaries:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "content": [
      {
        "id": 12,
        "flowVersion": "2026-05-v1",
        "selections": [
          { "step": "OCCASION", "code": "BIRTHDAY", "label": "생일" },
          { "step": "RECIPIENT", "code": "LOVER", "label": "연인" }
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
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false
  }
}
```

`GET /api/v1/users/me/curation-results/latest` and `GET /api/v1/users/me/curation-results/{resultId}` return the replay detail shape:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "id": 12,
    "flowVersion": "2026-05-v1",
    "selections": [
      { "step": "OCCASION", "code": "BIRTHDAY", "label": "생일" }
    ],
    "recommendations": [
      {
        "rank": 1,
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
    "createdAt": "2026-05-22T14:00:00"
  }
}
```

If the user has no latest result, or the requested `resultId` is not owned by the current user, return `404 CURATION_RESULT_NOT_FOUND`. Saved `recommendations` are snapshots for replay and should not be recalculated from changed flower/tag data.

Frontend flow:

1. Call `GET .../flow` once (optional cache).
2. For each step, `GET .../steps/{stepKey}/options?selections=...` with accumulated selections.
3. On finish, `POST .../results` with all six `{ step, code }` pairs.
4. On back navigation, drop later steps and refetch options.

## Messages

### POST /api/v1/messages/generate

Requires:

```http
Authorization: Bearer {accessToken}
```

Rate limit (per logged-in user):

| Rule | Value |
| --- | --- |
| Max requests | 10 |
| Window | 1 hour from the first request in the current window |
| Scope | Counts successful validation before generation (OpenAI and template both count) |
| Storage | In-memory on local/test; Redis on prod |

When the quota is exceeded, the API returns HTTP `429`:

```json
{
  "status": 429,
  "errorCode": "MESSAGE_GENERATION_RATE_LIMIT_EXCEEDED",
  "message": "메시지 생성은 60분 동안 최대 10회까지 가능합니다. 약 45분 후에 다시 시도해 주세요."
}
```

The `message` field includes an approximate retry time in minutes. After the window expires, the user receives a fresh quota of 10 requests.

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

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
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
    "createdAt": "2026-05-22T14:30:00",
    "message": "지은님께..."
  }
}
```

`curationResultId` is optional. If supplied, it must belong to the authenticated user. The response keeps `data.flowerId` and `data.message` for existing clients and adds saved-message metadata. Successful generations are saved to the user's message list, including template fallback results.

When `OPENAI_API_KEY` is configured, the backend tries OpenAI first. Missing key, timeout, non-2xx response, malformed response, or client exceptions fall back to the template generator and still return success. Rate limiting applies before generation regardless of whether OpenAI or the template is used.

Common errors:

| HTTP | errorCode | When |
| --- | --- | --- |
| 401 | `UNAUTHORIZED` | Missing or invalid Bearer token |
| 404 | `FLOWER_NOT_FOUND` | Flower missing or soft-deleted |
| 404 | `TAG_NOT_FOUND` | One or more `selectedTagIds` are invalid |
| 404 | `CURATION_RESULT_NOT_FOUND` | `curationResultId` is missing or not owned by the current user |
| 429 | `MESSAGE_GENERATION_RATE_LIMIT_EXCEEDED` | More than 10 generations in the current 1-hour window |

### GET /api/v1/users/me/messages

Returns saved generated messages for the current user, newest first.

Query parameters:

| Name | Type | Required | Default | Notes |
| --- | --- | --- | --- | --- |
| `page` | number | no | 0 | Zero-based |
| `size` | number | no | 20 | Page size, max 100 |

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "content": [
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
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false
  }
}
```

## Personalization

All personalization endpoints require `Authorization: Bearer {accessToken}` (see list under Authentication).

Likes are idempotent and scoped by user. Histories are written by authenticated flower detail views, deduplicated by flower, sorted newest first, and capped at 50 items. Messages and saved curation results are user-owned and sorted newest first.

`GET /likes` and `GET /histories` return arrays of `FlowerSummaryResponse`.

## Action Logs

Automatic logs (server records these **after a successful response**; 4xx/5xx are not logged):

| ActionType | Trigger | Notes |
| --- | --- | --- |
| `CURATION_START` | `GET /api/v1/curation`, `POST /api/v1/curation/results` | 큐레이션 결과 목록이 정상 반환될 때 저장. 백오피스 통계(`curationCount` 등)에 사용. |
| `DICTIONARY_SEARCH` | `GET /api/v1/flowers?keyword=...` | `keyword`가 비어 있으면 기록하지 않음(전체 목록 조회와 구분). |
| `FLOWER_DETAIL_VIEW` | `GET /api/v1/flowers/{flowerId}` | `action_data`에 `flowerId` 포함. 인기 식물 집계에 사용. |
| `ADMIN_USER_ROLE_CHANGE` | `PUT /api/v1/admin/users/{userId}/role` | 관리자 권한 변경 성공 시 저장. payload는 `actorUserId`, `targetUserId`, `previousRole`, `newRole` 포함. |

`CURATION_START` payloads include `source`: `curation-legacy` (tag query) or `curation-v2` (wizard `selections` + `flowVersion`).  
서버가 해당 API 성공 시 자동으로 넣으며, 프론트는 body로 보내지 않는다.  
아래 클릭 로그 `POST`의 `source`와 문자열이 같을 필요는 없다.

Authentication is optional for public automatic logs: valid Bearer → `user_id` stored; otherwise anonymous (`user_id` null). Admin role-change audit logs require `ROLE_ADMIN`.

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
  "source": "curation-v2",
  "flowVersion": "2026-05-v1",
  "selections": [
    { "step": "OCCASION", "code": "BIRTHDAY" },
    { "step": "RECIPIENT", "code": "LOVER" },
    { "step": "EMOTION", "code": "LOVE" },
    { "step": "FLOWER_MEANING", "code": "LOVE_3" },
    { "step": "SPACE", "code": "DESK_SMALL" },
    { "step": "BUDGET", "code": "BUDGET_MEDIUM" }
  ]
}
```

`flowVersion` and `selections` are optional (legacy clients may omit). Wizard UI should send `source`, `flowVersion`, and six `selections`.

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
  "description": "꽃 자체에 대한 소개 문구",
  "scientificName": "Rosa",
  "origin": "아시아, 유럽",
  "bloomingSeason": "봄~초여름",
  "scent": "은은한 향",
  "managementLevel": "EASY",
  "managementInfo": "관리자 테스트 관리법",
  "isToxicToPets": false,
  "priceRange": "LOW"
}
```

Response data uses `AdminFlowerDetailResponse`. It matches public flower detail fields, but `tags[]` includes mapping `weight`.

```json
{
  "id": 1,
  "name": "관리자테스트꽃",
  "imageUrl": "https://cdn.meanhwa.example/admin-test.jpg",
  "coreMeaning": "처음 의미",
  "description": "꽃 자체에 대한 소개 문구",
  "scientificName": "Rosa",
  "origin": "아시아, 유럽",
  "bloomingSeason": "봄~초여름",
  "scent": "은은한 향",
  "managementLevel": "EASY",
  "managementInfo": "관리자 테스트 관리법",
  "isPetSafe": true,
  "priceRange": "LOW",
  "tags": [
    {
      "id": 1,
      "category": "EVENT",
      "name": "생일",
      "weight": 5
    }
  ]
}
```

### GET /api/v1/admin/flowers/{flowerId}

Returns `AdminFlowerDetailResponse` for CMS edit screens, including existing tag mapping weights.

### PUT /api/v1/admin/flowers/{flowerId}

Updates flower metadata, records `updated_by`, and returns `AdminFlowerDetailResponse`.

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

`weight` must be from 1 to 5. Duplicate `tagId` values return `INVALID_MAPPING`. Response data uses `AdminFlowerDetailResponse`, so CMS clients can render the saved `weight` values.

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

### Admin user management

Backoffice user list, detail, and role promotion/demotion. Replaces manual `UPDATE users SET role = ...` in production DB.

All endpoints require:

```http
Authorization: Bearer {adminAccessToken}
```

The caller's JWT must have `role: "ROLE_ADMIN"` (see `GET /api/v1/users/me`).

#### GET /api/v1/admin/users

Paged member list, newest sign-ups first.

Query parameters:

| Name | Type | Required | Default | Notes |
| --- | --- | --- | --- | --- |
| `keyword` | string | no | null | Partial match on `email`, `nickname`, `oauthId` |
| `provider` | string | no | null | `DEV`, `KAKAO`, `NAVER` |
| `role` | string | no | null | `ROLE_USER`, `ROLE_ADMIN` |
| `page` | number | no | 0 | Zero-based |
| `size` | number | no | 20 | Max 100 |

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "content": [
      {
        "id": 5,
        "provider": "NAVER",
        "oauthId": "provider-user-id",
        "email": "user@example.com",
        "nickname": "민화유저",
        "role": "ROLE_USER",
        "createdAt": "2026-05-01T10:00:00"
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

#### GET /api/v1/admin/users/{userId}

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "id": 5,
    "provider": "NAVER",
    "oauthId": "provider-user-id",
    "email": "user@example.com",
    "nickname": "민화유저",
    "role": "ROLE_USER",
    "createdAt": "2026-05-01T10:00:00",
    "updatedAt": "2026-05-08T12:00:00",
    "likeCount": 3,
    "historyCount": 7
  }
}
```

| Field | Meaning |
| --- | --- |
| `likeCount` | Number of rows in `user_likes` |
| `historyCount` | Number of rows in `user_histories` |

#### PUT /api/v1/admin/users/{userId}/role

Request:

```json
{
  "role": "ROLE_ADMIN"
}
```

Allowed values: `ROLE_USER`, `ROLE_ADMIN`.

Response `data` uses the same shape as `GET /api/v1/admin/users/{userId}`.

Rules:

- The authenticated admin cannot change their own role (`CANNOT_CHANGE_OWN_ROLE`).
- The last remaining `ROLE_ADMIN` cannot be demoted to `ROLE_USER` (`LAST_ADMIN_CANNOT_BE_DEMOTED`).
- After a role change, the target user must log in again so a new JWT includes the updated role.

Common errors for this group:

| HTTP | errorCode | When |
| --- | --- | --- |
| 401 | `UNAUTHORIZED` | Missing or invalid token |
| 403 | `FORBIDDEN` | Token user is not `ROLE_ADMIN` |
| 404 | `USER_NOT_FOUND` | Unknown `userId` |
| 400 | `CANNOT_CHANGE_OWN_ROLE` | Admin targets their own account |
| 400 | `LAST_ADMIN_CANNOT_BE_DEMOTED` | Only one admin left and demotion requested |
| 400 | `INVALID_REQUEST` | Invalid `page`/`size` or filter values |

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

`GET /api/v1/flowers` cache keys must include every query filter: normalized `keyword`, `priceRange`, `isPetSafe`, `managementLevel`, sorted `tagIds`, `page`, and `size`.

Message generation rate limits also use Redis in prod (`app.message.rate-limit.store=redis`). Local/test keep counters in memory.

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
```

Message generation rate limit defaults (Spring `application-prod.yml`, not separate env vars unless overridden):

```text
app.message.rate-limit.enabled=true
app.message.rate-limit.max-requests=10
app.message.rate-limit.window=1h
app.message.rate-limit.store=redis
```

Optional OAuth / storage settings:

```text
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
