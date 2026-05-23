# API Contract

프론트 연동에 필요한 현재 백엔드 계약만 정리합니다. 모든 경로는 `/api/v1` 기준입니다.

## Common

성공 응답:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {}
}
```

생성/업로드는 HTTP `201`, body `status: 201`입니다. `data`가 `null`이면 응답에서 생략됩니다.

에러 응답:

```json
{
  "status": 400,
  "errorCode": "INVALID_REQUEST",
  "message": "요청값이 올바르지 않습니다."
}
```

페이지 응답:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "hasNext": false
}
```

`page`는 0부터 시작합니다. `size` 기본값은 20이고 최대 100으로 제한됩니다.

## Auth

Bearer token:

```http
Authorization: Bearer {accessToken}
```

| Access | Endpoints |
| --- | --- |
| Public | `GET /flowers`, `GET /flowers/{flowerId}`, `GET /tags`, `POST /curation/results`, `POST /auth/**`, `POST /action-logs/curation-result-click` |
| Login | `POST /messages/generate`, `GET /users/**`, `POST /users/**`, `DELETE /users/**` |
| Admin | `/admin/**` with `ROLE_ADMIN` |

`GET /flowers/{flowerId}`, `POST /curation/results`, `POST /action-logs/curation-result-click`는 유효한 Bearer token이 있으면 사용자 ID를 함께 기록하고, 없어도 요청은 허용합니다.

## Enums

```ts
type ManagementLevel = "EASY" | "NORMAL" | "HARD";
type PriceRange = "LOW" | "MEDIUM" | "HIGH" | "PREMIUM";
type TagCategory =
  | "EVENT" | "RELATION" | "EMOTION" | "MEANING"
  | "STYLE" | "CARE" | "SEASON" | "ENVIRONMENT";
type Role = "ROLE_USER" | "ROLE_ADMIN";
type OAuthProvider = "DEV" | "KAKAO" | "NAVER";
type CurationStepKey =
  | "OCCASION" | "RECIPIENT" | "EMOTION"
  | "FLOWER_MEANING" | "SPACE" | "BUDGET";
```

큐레이션 code 표는 [curation-wizard-api.md](curation-wizard-api.md)를 봅니다.

## Endpoints

### Auth

| Method | Path | Body | Data |
| --- | --- | --- | --- |
| `POST` | `/auth/login/kakao` | `{ "accessToken": string }` | `TokenResponse` |
| `POST` | `/auth/login/naver` | `{ "accessToken": string }` | `TokenResponse` |
| `POST` | `/auth/login/dev` | `{ "oauthId": string, "email"?: string, "nickname"?: string, "role"?: Role }` | `TokenResponse` |
| `POST` | `/auth/refresh` | `{ "refreshToken": string }` | `TokenResponse` |
| `POST` | `/auth/logout` | `{ "refreshToken": string }` | none |

`/auth/login/dev`는 local/test용이며 prod에서는 거부됩니다. Refresh 성공 시 기존 refresh token은 폐기되고 새 token pair가 발급됩니다.

`TokenResponse`:

```ts
{
  accessToken: string;
  refreshToken: string;
  tokenType: "Bearer";
  expiresInSeconds: number;
}
```

### Flowers

| Method | Path | Query | Data |
| --- | --- | --- | --- |
| `GET` | `/flowers` | `keyword`, `priceRange`, `isPetSafe`, `managementLevel`, `tagIds`, `page`, `size` | `PageResponse<FlowerSummary>` |
| `GET` | `/flowers/{flowerId}` | - | `FlowerDetail` |

`tagIds`는 반복 query입니다. 여러 개면 AND 조건으로 검색합니다.

`FlowerSummary` fields:

```ts
id, name, imageUrl, coreMeaning, description,
managementLevel, isPetSafe, priceRange
```

`FlowerDetail`은 `FlowerSummary`에 `tags: { id, category, name }[]`가 추가됩니다.

### Tags

| Method | Path | Data |
| --- | --- | --- |
| `GET` | `/tags` | `{ category: TagCategory, tags: { id: number, name: string }[] }[]` |

### Curation

| Method | Path | Auth | Data |
| --- | --- | --- | --- |
| `POST` | `/curation/results` | optional | `CurationResultsResponse` |

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

Rules:

- 정확히 6개 step이 필요합니다.
- Step 중복은 허용하지 않습니다.
- Step 1-5 code는 tag scoring에 사용합니다.
- Step 6 `BUDGET_*`는 `PriceRange` 필터로만 사용합니다.
- 정렬은 `score desc`, `name asc`입니다.
- 로그인 요청이면 결과 snapshot을 저장하고 `curationResultId`를 반환합니다.
- 익명 요청이면 저장하지 않고 `curationResultId`는 `null`입니다.

`CurationResultsResponse` fields:

```ts
content: {
  flowerId, name, imageUrl, coreMeaning, priceRange,
  isPetSafe, score, recommendationReason,
  matchedTags: { id, category, name }[]
}[];
page, size, totalElements, totalPages, hasNext, curationResultId
```

### Messages

| Method | Path | Auth | Data |
| --- | --- | --- | --- |
| `POST` | `/messages/generate` | login | `MessageGenerateResponse` |
| `GET` | `/users/me/messages` | login | `PageResponse<MessageGenerateResponse>` |

Generate request:

```json
{
  "flowerId": 1,
  "selectedTagIds": [5, 9],
  "curationResultId": 12,
  "senderName": "민수",
  "receiverName": "지은"
}
```

`selectedTagIds`와 `curationResultId`는 optional입니다. `senderName`, `receiverName`은 최대 30자입니다. 사용자별 메시지 생성 기본 제한은 1시간 10회입니다.

`MessageGenerateResponse` fields:

```ts
id, flowerId, flowerName, flowerImageUrl, coreMeaning,
selectedTags, curationResultId, senderName, receiverName,
createdAt, message
```

OpenAI 호출 실패 또는 키 없음은 사용자 API 실패로 전파하지 않고 템플릿 fallback으로 저장합니다.

### My Page

| Method | Path | Data |
| --- | --- | --- |
| `GET` | `/users/me` | `{ id, provider, oauthId, email, nickname, role }` |
| `GET` | `/users/me/likes` | `FlowerSummary[]` |
| `POST` | `/users/me/likes/{flowerId}` | `FlowerSummary` |
| `DELETE` | `/users/me/likes/{flowerId}` | none |
| `GET` | `/users/me/histories` | `FlowerSummary[]` |
| `DELETE` | `/users/me/histories` | none |
| `GET` | `/users/me/curation-results` | `PageResponse<CurationResultSummary>` |
| `GET` | `/users/me/curation-results/latest` | `CurationResultDetail` |
| `GET` | `/users/me/curation-results/{resultId}` | `CurationResultDetail` |

최근 본 식물은 인증 사용자의 꽃 상세 조회 때 저장됩니다. 같은 꽃은 `viewedAt`만 갱신하고 최대 50개를 유지합니다.

`CurationResultSummary` fields:

```ts
id, flowVersion,
selections: { step, code, label }[],
topFlowers: { rank, flowerId, name, imageUrl, coreMeaning }[], // up to 4
resultCount, createdAt
```

`topFlowers`는 저장된 추천 스냅샷의 앞 4개까지 내려줍니다. 저장된 `recommendations` 자체가 4개보다 적으면 저장된 개수만 반환합니다. 전체 저장 추천 목록이 필요하면 `/users/me/curation-results/{resultId}`의 `recommendations`를 사용합니다.

### Action Logs

| Method | Path | Auth | Body |
| --- | --- | --- | --- |
| `POST` | `/action-logs/curation-result-click` | optional | `CurationResultClickRequest` |

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

서버 자동 로그:

| ActionType | Trigger |
| --- | --- |
| `CURATION_START` | `POST /curation/results` |
| `DICTIONARY_SEARCH` | `GET /flowers?keyword=...` |
| `FLOWER_DETAIL_VIEW` | `GET /flowers/{flowerId}` |
| `ADMIN_USER_ROLE_CHANGE` | `PUT /admin/users/{userId}/role` |

### Admin

모든 admin endpoint는 `ROLE_ADMIN`이 필요합니다.

| Method | Path | Body/Query | Data |
| --- | --- | --- | --- |
| `POST` | `/admin/flowers` | `AdminFlowerRequest` | `AdminFlowerDetail` |
| `GET` | `/admin/flowers/{flowerId}` | - | `AdminFlowerDetail` |
| `PUT` | `/admin/flowers/{flowerId}` | `AdminFlowerRequest` | `AdminFlowerDetail` |
| `DELETE` | `/admin/flowers/{flowerId}` | - | none |
| `PUT` | `/admin/flowers/{flowerId}/tags` | `{ "tags": [{ "tagId": number, "weight": 1..5 }] }` | `AdminFlowerDetail` |
| `POST` | `/admin/tags` | `{ "category": TagCategory, "name": string }` | `AdminTag` |
| `PUT` | `/admin/tags/{tagId}` | `{ "category": TagCategory, "name": string }` | `AdminTag` |
| `DELETE` | `/admin/tags/{tagId}` | - | none |
| `POST` | `/admin/uploads/images` | multipart `file` | `ImageUploadResponse` |
| `GET` | `/admin/users` | `keyword`, `provider`, `role`, `page`, `size` | `PageResponse<AdminUserSummary>` |
| `GET` | `/admin/users/{userId}` | - | `AdminUserDetail` |
| `PUT` | `/admin/users/{userId}/role` | `{ "role": Role }` | `AdminUserDetail` |
| `GET` | `/admin/statistics/summary` | `from`, `to` | `StatisticsSummary` |
| `GET` | `/admin/statistics/popular-tags` | `from`, `to`, `limit` | `PopularTag[]` |
| `GET` | `/admin/statistics/popular-flowers` | `from`, `to`, `limit` | `PopularFlower[]` |
| `GET` | `/admin/statistics/daily-active-users` | `from`, `to` | `DailyActiveUser[]` |

`AdminFlowerRequest`:

```json
{
  "name": "꽃 이름",
  "imageUrl": "https://...",
  "coreMeaning": "대표 의미",
  "description": "상세 설명",
  "managementLevel": "EASY",
  "isToxicToPets": false,
  "priceRange": "LOW"
}
```

Admin rules:

- 꽃/태그 삭제는 soft delete입니다.
- 꽃-태그 매핑 수정은 기존 매핑 전체 교체입니다.
- Admin tag API는 `code`를 받지 않습니다. 위저드용 `tags.code`는 seed/Flyway로 관리합니다.
- 본인 role은 변경할 수 없습니다.
- 마지막 `ROLE_ADMIN`은 강등할 수 없습니다.
- role 변경 후 대상 사용자는 다시 로그인해야 새 JWT에 반영됩니다.
- 이미지 업로드 허용 타입은 `image/jpeg`, `image/png`, `image/webp`이고 기본 최대 크기는 5 MB입니다.

## Error Codes

| Code | HTTP |
| --- | ---: |
| `INVALID_REQUEST` | 400 |
| `INVALID_PRICE_RANGE` | 400 |
| `INVALID_FLOWER_FILTER` | 400 |
| `INVALID_CURATION_STEP` | 400 |
| `INVALID_CURATION_SELECTION` | 400 |
| `INCOMPLETE_CURATION_SELECTION` | 400 |
| `UNSUPPORTED_OAUTH_PROVIDER` | 400 |
| `INVALID_MAPPING` | 400 |
| `INVALID_FILE_TYPE` | 400 |
| `FILE_TOO_LARGE` | 400 |
| `CANNOT_CHANGE_OWN_ROLE` | 400 |
| `LAST_ADMIN_CANNOT_BE_DEMOTED` | 400 |
| `UNAUTHORIZED` | 401 |
| `INVALID_TOKEN` | 401 |
| `INVALID_OAUTH_TOKEN` | 401 |
| `FORBIDDEN` | 403 |
| `FLOWER_NOT_FOUND` | 404 |
| `TAG_NOT_FOUND` | 404 |
| `USER_NOT_FOUND` | 404 |
| `CURATION_RESULT_NOT_FOUND` | 404 |
| `CURATION_FLOW_NOT_FOUND` | 404 |
| `RESOURCE_NOT_FOUND` | 404 |
| `DUPLICATE_TAG` | 409 |
| `MESSAGE_GENERATION_RATE_LIMIT_EXCEEDED` | 429 |
| `UPLOAD_FAILED` | 500 |
| `INTERNAL_SERVER_ERROR` | 500 |
