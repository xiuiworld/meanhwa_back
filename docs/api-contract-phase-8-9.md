# Meanhwa API Contract - Phase 8-9

This document covers backend changes for frontend Phase F9 performance behavior and Phase F10 admin statistics dashboard.

## Current Backend Status

Phase 8 and Phase 9 are implemented.

- Public flower list and tag list responses are cacheable on the backend.
- In local/test profiles, the backend uses simple in-memory cache and does not require Redis.
- In prod deployment, GitHub Actions ensures an EC2 Docker Redis container is running and runs the backend with Redis cache enabled.
- Admin CMS mutations evict relevant caches so stale public list/tag data is not kept after create/update/delete.
- Admin statistics APIs are under `/api/v1/admin/statistics/**`.
- Statistics APIs require `ROLE_ADMIN`, using the same auth rule as other `/api/v1/admin/**` APIs.

## Phase F9: Performance Notes

Frontend does not need to call a separate cache API.

Cached backend endpoints:

```http
GET /api/v1/flowers
GET /api/v1/tags
```

Cache behavior:

- `GET /api/v1/flowers` cache key includes `keyword`, `page`, and `size`.
- `GET /api/v1/tags` caches the grouped tag list.
- Admin flower create/update/delete evicts flower list cache.
- Admin flower-tag mapping replacement evicts flower list cache.
- Admin tag create/update/delete evicts tag cache and flower cache.
- `GET /api/v1/flowers/{flowerId}` is intentionally not cached because it records user history and action logs.
- `GET /api/v1/curation` is intentionally not cached because it records action logs and has many query combinations.

Frontend recommendation:

- Keep using the same public APIs.
- Add client-side debounce for flower search.
- Refetch flower/tag lists after CMS mutations.
- Treat backend cache as transparent infrastructure, not frontend state.

## Phase F10: Admin Statistics APIs

All statistics APIs require:

```http
Authorization: Bearer {adminAccessToken}
```

Common query params:

| Param | Required | Format | Default |
| --- | --- | --- | --- |
| `from` | no | `yyyy-MM-dd` | 29 days before `to` |
| `to` | no | `yyyy-MM-dd` | today |
| `limit` | no | integer | 10, max 100; only popular APIs |

If `from` is later than `to`, the backend returns `400 INVALID_REQUEST`.

### GET /api/v1/admin/statistics/summary

Returns top-level dashboard counts for the selected period.

Request:

```http
GET /api/v1/admin/statistics/summary?from=2026-05-01&to=2026-05-08
```

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "from": "2026-05-01",
    "to": "2026-05-08",
    "totalUsers": 14,
    "activeFlowers": 12,
    "totalLikes": 5,
    "curationCount": 23,
    "searchCount": 9,
    "detailViewCount": 31,
    "curationClickCount": 7
  }
}
```

Field notes:

- `totalUsers`: all backend users.
- `activeFlowers`: non-deleted flowers.
- `totalLikes`: all current likes.
- Other count fields are based on `action_logs` within the selected period.

### GET /api/v1/admin/statistics/popular-tags

Counts selected curation tags from `CURATION_START.action_data.tagIds`.

Request:

```http
GET /api/v1/admin/statistics/popular-tags?from=2026-05-01&to=2026-05-08&limit=10
```

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": [
    {
      "tagId": 5,
      "category": "RELATION",
      "name": "연인",
      "count": 12
    }
  ]
}
```

If a tag was deleted after being logged:

```json
{
  "tagId": 99,
  "category": null,
  "name": "삭제된 태그",
  "count": 3
}
```

### GET /api/v1/admin/statistics/popular-flowers

Counts flower popularity from:

- `FLOWER_DETAIL_VIEW.action_data.flowerId`
- `CURATION_RESULT_CLICK.action_data.flowerId`

Request:

```http
GET /api/v1/admin/statistics/popular-flowers?from=2026-05-01&to=2026-05-08&limit=10
```

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": [
    {
      "flowerId": 1,
      "name": "장미",
      "imageUrl": "https://cdn.meanhwa.example/flowers/rose.jpg",
      "count": 18
    }
  ]
}
```

If a flower was deleted after being logged:

```json
{
  "flowerId": 99,
  "name": "삭제된 꽃/식물",
  "imageUrl": null,
  "count": 3
}
```

### GET /api/v1/admin/statistics/daily-active-users

Returns daily distinct authenticated users based on `action_logs.user_id`.

Request:

```http
GET /api/v1/admin/statistics/daily-active-users?from=2026-05-01&to=2026-05-08
```

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": [
    {
      "date": "2026-05-01",
      "activeUsers": 3
    },
    {
      "date": "2026-05-02",
      "activeUsers": 0
    }
  ]
}
```

The response includes zero-count dates inside the selected range.

## Frontend Implementation Notes

Phase F9:

- No route or type changes are required for public flower/tag reads.
- Use debounce for search input to reduce request count.
- Keep loading skeletons and empty states.
- After admin CMS changes, refetch the public list/tag data used by the admin screen.

Phase F10:

- Recommended admin dashboard route: `/admin/dashboard`.
- Fetch summary, popular tags, popular flowers, and DAU in parallel.
- Use the same date range filter for all four APIs.
- Use `limit` for popular tag/flower widgets.
- Empty arrays are valid responses; show an empty dashboard state instead of an error.
- `401` means login required.
- `403` means logged in but not admin.
- Production admin dashboard still depends on a real admin login/token issuance path.

## Deployment Notes

Prod deployment uses Redis with Docker on EC2:

```text
container: meanhwa-redis
image: redis:alpine
network: meanhwa-net
```

If the Redis container does not exist, deployment creates it. If it exists but is stopped, deployment starts it again.

Backend prod container gets:

```text
CACHE_TYPE=redis
REDIS_HOST=meanhwa-redis
REDIS_PORT=6379
```

Redis is not exposed publicly. It is only reachable from the backend container through the Docker network.
