# Meanhwa API Contract - Phase 5

This document covers backend behavior logging for recommendation improvement and future statistics APIs.

## Current Backend Status

Phase 5 stores major user actions in `action_logs`.

- Logging is best-effort.
- Log write failures are caught inside `ActionLogService` and do not fail user-facing APIs.
- Anonymous users are recorded with `userId: null`.
- Authenticated users are recorded with their backend user id when a valid Bearer token is provided.
- `action_data` is stored as a JSON string in a text column for H2/MySQL compatibility.

## Logged Actions

| ActionType | Trigger | Frontend action needed |
| --- | --- | --- |
| `CURATION_START` | `GET /api/v1/curation` | no |
| `DICTIONARY_SEARCH` | `GET /api/v1/flowers?keyword=...` | no |
| `FLOWER_DETAIL_VIEW` | `GET /api/v1/flowers/{flowerId}` | no |
| `CURATION_RESULT_CLICK` | `POST /api/v1/action-logs/curation-result-click` | yes |

`GET /api/v1/flowers` without `keyword` does not create a `DICTIONARY_SEARCH` log.

## DB Shape

Table: `action_logs`

```text
id BIGINT PK
user_id BIGINT NULL
action_type VARCHAR(50)
action_data TEXT
created_at DATETIME
```

Current index intent:

```text
(action_type, created_at)
(user_id, created_at)
```

## Automatic Logs

### Curation

Request:

```http
GET /api/v1/curation?tagIds=5&tagIds=9&isPetSafe=true&priceRange=MEDIUM
```

Stored action:

```text
CURATION_START
```

Payload example:

```json
{
  "tagIds": [5, 9],
  "isPetSafe": true,
  "priceRange": "MEDIUM",
  "page": 0,
  "size": 20,
  "resultCount": 1,
  "totalElements": 1,
  "resultFlowerIds": [1]
}
```

### Dictionary Search

Request:

```http
GET /api/v1/flowers?keyword=사랑&page=0&size=20
```

Stored action:

```text
DICTIONARY_SEARCH
```

Payload example:

```json
{
  "keyword": "사랑",
  "page": 0,
  "size": 20,
  "resultCount": 1,
  "totalElements": 1
}
```

### Flower Detail View

Request:

```http
GET /api/v1/flowers/1
```

Stored action:

```text
FLOWER_DETAIL_VIEW
```

Payload example:

```json
{
  "flowerId": 1
}
```

This is separate from `user_histories`. History is a user feature, while action logs are analytics data.

## Frontend-Triggered Click Log

### POST /api/v1/action-logs/curation-result-click

This endpoint records which recommendation result the user clicked.

Authentication:

- Optional.
- If the frontend BFF provides `Authorization: Bearer {accessToken}` and it is valid, backend stores `userId`.
- If no token is provided, backend stores `userId = null`.

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

Fields:

| Field | Required | Notes |
| --- | --- | --- |
| `flowerId` | yes | Clicked flower id |
| `tagIds` | no | Selected curation tags |
| `rank` | no | 1-based rank in the rendered result list |
| `score` | no | Recommendation score returned by curation API |
| `source` | no | Suggested value: `curation` |

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다."
}
```

Frontend behavior:

- Fire this request when the user clicks a flower from the curation result list.
- Do not block navigation on this request.
- Do not show an error toast if this request fails.
- The BFF should attach Bearer token when the user is logged in, but allow anonymous click logging.
- For logged-in flower detail pages, prefer one backend `GET /api/v1/flowers/{flowerId}` through the BFF with Authorization attached. Calling the same detail endpoint once anonymously and once authenticated is allowed, but it creates extra traffic and duplicate `FLOWER_DETAIL_VIEW` analytics logs.

## Notes for Future Statistics

The current payloads are designed to support:

- Curation usage count
- Popular selected tags
- Popular clicked flowers
- Search keyword count
- Detail view count
- Authenticated vs anonymous behavior split
