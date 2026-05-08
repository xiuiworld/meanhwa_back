# Meanhwa API Contract - Phase 10

This document covers the external integration changes for real Kakao/Naver login and OpenAI-backed message generation.

## Current Backend Status

Phase 10 adds production-facing integrations while preserving the existing app API shape.

- Frontend obtains Kakao/Naver access tokens through each provider SDK.
- Backend verifies provider access tokens by calling the provider profile API.
- Backend still returns the same app JWT `accessToken` and `refreshToken`.
- Message generation now tries OpenAI first when `OPENAI_API_KEY` is configured.
- Message generation falls back to the existing template generator on OpenAI failures.

## OAuth Login

Endpoint:

```http
POST /api/v1/auth/login/{provider}
```

Supported providers:

```text
kakao
naver
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
    "tokenType": "Bearer",
    "accessToken": "{meanhwaAccessToken}",
    "refreshToken": "{meanhwaRefreshToken}",
    "expiresInSeconds": 1800
  }
}
```

Error behavior:

- Unknown provider returns `400 UNSUPPORTED_OAUTH_PROVIDER`.
- Invalid, expired, or rejected provider token returns `401 INVALID_OAUTH_TOKEN`.
- Provider profile responses without a usable provider user id return `401 INVALID_OAUTH_TOKEN`.

Development login:

```http
POST /api/v1/auth/login/dev
```

`dev` login remains available outside the `prod` profile and remains blocked in `prod`.

## OpenAI Message Generation

Endpoint stays unchanged:

```http
POST /api/v1/messages/generate
```

Request and response shape stay unchanged. When OpenAI is configured, the backend sends the flower, selected tags, sender, and receiver context to the OpenAI Responses API and returns the generated Korean message text.

Fallback behavior:

- Missing `OPENAI_API_KEY`
- OpenAI timeout
- OpenAI non-2xx response
- Malformed OpenAI response
- Any OpenAI client exception

All fallback cases return a successful template-generated message instead of failing the user request.

## Environment Variables

Required in prod:

```text
OPENAI_API_KEY
```

Provider profile URLs have production defaults, but can be overridden:

```text
KAKAO_USERINFO_URL=https://kapi.kakao.com/v2/user/me
NAVER_USERINFO_URL=https://openapi.naver.com/v1/nid/me
```

Optional OpenAI settings:

```text
OPENAI_BASE_URL=https://api.openai.com
OPENAI_MODEL=gpt-5.4-mini
OPENAI_TIMEOUT_MILLIS=5000
OPENAI_MAX_OUTPUT_TOKENS=300
```

Optional OAuth timeout settings:

```text
KAKAO_TIMEOUT_MILLIS=3000
NAVER_TIMEOUT_MILLIS=3000
```

No provider token, OpenAI API key, or provider secret should be stored in source.
