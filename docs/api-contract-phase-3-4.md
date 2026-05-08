# Meanhwa API Contract - Phase 3-4

This document covers the backend contract for frontend Phase F4 authentication UI and Phase F5 personalization.

## Current Backend Status

Phase 3 and Phase 4 are implemented.

- JWT access token and refresh token are issued by the backend.
- Refresh tokens are stored in the backend DB as SHA-256 hashes, not as raw tokens.
- `dev` login is available for `local` and `test` profiles.
- Kakao/Naver real OAuth is not implemented yet. The frontend may show Kakao/Naver buttons, but actual login should use the `dev` backend endpoint until real OAuth is added.
- Core public APIs remain usable without login.
- Authenticated user APIs require `Authorization: Bearer {accessToken}`.

Verified command:

```powershell
$env:JAVA_HOME='C:\Users\xiuiw\.jdks\corretto-21.0.11'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat clean test bootJar --rerun-tasks
```

Result: `BUILD SUCCESSFUL`

## Common Auth Rules

Public APIs:

```http
GET  /api/v1/flowers
GET  /api/v1/flowers/{flowerId}
GET  /api/v1/tags
GET  /api/v1/curation
POST /api/v1/messages/generate
POST /api/v1/auth/**
```

Authenticated APIs:

```http
GET    /api/v1/users/me
GET    /api/v1/users/me/likes
POST   /api/v1/users/me/likes/{flowerId}
DELETE /api/v1/users/me/likes/{flowerId}
GET    /api/v1/users/me/histories
DELETE /api/v1/users/me/histories
```

Admin APIs are reserved for `ROLE_ADMIN`:

```http
/api/v1/admin/**
```

Frontend request header for authenticated APIs:

```http
Authorization: Bearer {accessToken}
```

Success responses use the existing `ApiResponse` wrapper.

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {}
}
```

Error responses:

```json
{
  "status": 401,
  "errorCode": "UNAUTHORIZED",
  "message": "인증이 필요합니다."
}
```

Auth-related error codes:

| errorCode | HTTP | Frontend meaning |
| --- | ---: | --- |
| `UNAUTHORIZED` | 401 | No access token or login required |
| `INVALID_TOKEN` | 401 | Token is malformed, expired, wrong type, revoked, or unknown |
| `FORBIDDEN` | 403 | Logged in but role is not allowed |
| `UNSUPPORTED_OAUTH_PROVIDER` | 400 | `kakao`/`naver` real OAuth is not implemented yet |
| `USER_NOT_FOUND` | 404 | Token points to a deleted or missing user |

## Phase F4: Auth APIs

### POST /api/v1/auth/login/{provider}

Current supported provider:

```text
dev
```

`kakao` and `naver` currently return `UNSUPPORTED_OAUTH_PROVIDER`.

Request for local/test dev login:

```http
POST /api/v1/auth/login/dev
Content-Type: application/json
```

```json
{
  "oauthId": "dev-user-1",
  "email": "dev@example.com",
  "nickname": "민화유저",
  "role": "ROLE_USER"
}
```

Fields:

| Field | Required | Notes |
| --- | --- | --- |
| `oauthId` | yes | Stable dev user id. Same value logs into the same backend user. |
| `email` | no | Stored on user profile. |
| `nickname` | no | Defaults to `민화유저` when omitted or blank. |
| `role` | no | Defaults to `ROLE_USER`. Use `ROLE_ADMIN` only for local admin testing. |

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "tokenType": "Bearer",
    "expiresInSeconds": 1800
  }
}
```

Frontend token handling policy:

- Preferred frontend architecture is BFF with HttpOnly cookies.
- Browser JavaScript should not need direct access to `accessToken` or `refreshToken`.
- Frontend internal server routes may store tokens in HttpOnly cookies or server-side session state.
- The BFF attaches only `accessToken` to backend API requests as `Authorization: Bearer {accessToken}`.
- The BFF uses `refreshToken` only with `/api/v1/auth/refresh` and `/api/v1/auth/logout`.
- On logout, call the backend logout endpoint, then clear frontend cookies/session state.
- If an authenticated API returns `401 INVALID_TOKEN`, try refresh once. If refresh fails, clear tokens and show logged-out UI.
- If an authenticated API returns `401 UNAUTHORIZED`, show login-required UI.

### POST /api/v1/auth/refresh

Request:

```json
{
  "refreshToken": "eyJ..."
}
```

Response shape is the same as login:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "accessToken": "new-eyJ...",
    "refreshToken": "new-eyJ...",
    "tokenType": "Bearer",
    "expiresInSeconds": 1800
  }
}
```

Important BFF behavior:

- Refresh token rotation is enabled.
- After refresh succeeds, replace both stored cookie/session token values with the new values.
- The old refresh token becomes invalid immediately.

### POST /api/v1/auth/logout

Request:

```json
{
  "refreshToken": "eyJ..."
}
```

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다."
}
```

Frontend behavior:

- Call this endpoint when the user clicks logout.
- Clear frontend cookies/session state even if logout fails because the user explicitly chose to log out.

### GET /api/v1/users/me

Request:

```http
GET /api/v1/users/me
Authorization: Bearer {accessToken}
```

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "id": 1,
    "provider": "DEV",
    "oauthId": "dev-user-1",
    "email": "dev@example.com",
    "nickname": "민화유저",
    "role": "ROLE_USER"
  }
}
```

Frontend usage:

- Use this endpoint on app boot through the BFF when auth cookies/session state exist.
- Use success to set global logged-in user state.
- Use failure to clear stale frontend cookies/session state.

## Phase F5: Personalization APIs

Personalization APIs all require `Authorization: Bearer {accessToken}`.

`FlowerSummaryResponse` is reused for likes and histories:

```json
{
  "id": 1,
  "name": "장미",
  "imageUrl": "https://cdn.meanhwa.example/flowers/rose.jpg",
  "coreMeaning": "사랑과 열정",
  "managementLevel": "NORMAL",
  "isPetSafe": true,
  "priceRange": "MEDIUM"
}
```

### GET /api/v1/users/me/likes

Returns the current user's liked flowers, newest like first.

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": [
    {
      "id": 1,
      "name": "장미",
      "imageUrl": "https://cdn.meanhwa.example/flowers/rose.jpg",
      "coreMeaning": "사랑과 열정",
      "managementLevel": "NORMAL",
      "isPetSafe": true,
      "priceRange": "MEDIUM"
    }
  ]
}
```

### POST /api/v1/users/me/likes/{flowerId}

Adds a like for the flower.

Behavior:

- Duplicate likes are idempotent.
- If the user already liked the flower, the API still returns success.
- If the flower does not exist, returns `FLOWER_NOT_FOUND`.

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
    "isPetSafe": true,
    "priceRange": "MEDIUM"
  }
}
```

### DELETE /api/v1/users/me/likes/{flowerId}

Removes a like for the flower.

Behavior:

- Deleting a non-existing like is idempotent.
- The API still returns success.

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다."
}
```

### GET /api/v1/users/me/histories

Returns recently viewed flowers, newest view first.

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": [
    {
      "id": 1,
      "name": "장미",
      "imageUrl": "https://cdn.meanhwa.example/flowers/rose.jpg",
      "coreMeaning": "사랑과 열정",
      "managementLevel": "NORMAL",
      "isPetSafe": true,
      "priceRange": "MEDIUM"
    }
  ]
}
```

History write behavior:

- `GET /api/v1/flowers/{flowerId}` records history only when a valid access token is included.
- If the same flower is viewed again, the backend updates `viewedAt` instead of creating a duplicate.
- Each user keeps up to 50 history items.
- When the count exceeds 50, the oldest items are removed.
- Anonymous flower detail views still work and do not create history.

### DELETE /api/v1/users/me/histories

Deletes all recent-view history for the current user.

Response:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다."
}
```

## Frontend Implementation Notes

Phase F4:

- Build login UI now, but wire Kakao/Naver buttons to a temporary dev-login path or disabled state until real OAuth exists.
- Use `POST /api/v1/auth/login/dev` for integration testing.
- Keep public pages usable without auth.
- Protect my page and personalization routes by checking global auth state.
- Add a BFF-side API client/interceptor that attaches `Authorization: Bearer {accessToken}` when an access token exists.
- Add a BFF-side response interceptor that handles one refresh attempt on `401 INVALID_TOKEN`.
- Production frontend should not expose the temporary `dev` login UI or route because backend rejects `dev` login in the `prod` profile.

Phase F5:

- Flower card/detail like button should require login.
- If unauthenticated, show login prompt instead of calling like APIs.
- On successful like/unlike, update local UI optimistically or refetch `/users/me/likes`.
- My page can render two sections from:
  - `GET /api/v1/users/me/likes`
  - `GET /api/v1/users/me/histories`
- Recent history appears automatically after authenticated users open flower detail pages.

Temporary dev login examples:

Regular user:

```json
{
  "oauthId": "dev-user-1",
  "email": "dev@example.com",
  "nickname": "민화유저",
  "role": "ROLE_USER"
}
```

Admin test user:

```json
{
  "oauthId": "dev-admin-1",
  "email": "admin@example.com",
  "nickname": "민화관리자",
  "role": "ROLE_ADMIN"
}
```
