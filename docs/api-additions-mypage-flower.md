# 마이페이지/꽃 도감 추가 API 명세

이 문서는 새로 추가할 API만 따로 정리한 명세입니다. 전체 API 계약에는 [api-contract.md](api-contract.md)에도 반영합니다.

## 공통

Base path:

```text
/api/v1
```

인증이 필요한 API는 모두 다음 헤더가 필요합니다.

```http
Authorization: Bearer {accessToken}
```

신규 에러 코드:

| errorCode | HTTP | 의미 |
| --- | ---: | --- |
| `CURATION_RESULT_NOT_FOUND` | 404 | 내 큐레이션 결과가 없거나 접근 권한이 없음 |
| `INVALID_FLOWER_FILTER` | 400 | 꽃 도감 필터 쿼리 값이 올바르지 않음 |

## Endpoint Summary

```http
GET  /api/v1/users/me/messages
POST /api/v1/messages/generate

GET  /api/v1/users/me/curation-results
GET  /api/v1/users/me/curation-results/latest
GET  /api/v1/users/me/curation-results/{resultId}
POST /api/v1/curation/results

GET  /api/v1/flowers
GET  /api/v1/flowers/{flowerId}
POST /api/v1/admin/flowers
PUT  /api/v1/admin/flowers/{flowerId}
GET  /api/v1/admin/flowers/{flowerId}
```

## 1. 메시지 저장/목록

### POST /api/v1/messages/generate

기존 메시지 생성 API입니다. 성공 시 로그인 유저의 메시지 이력으로 결과를 저장합니다.

Request 변경:

```json
{
  "flowerId": 1,
  "selectedTagIds": [5, 9],
  "curationResultId": 12,
  "senderName": "민수",
  "receiverName": "지은"
}
```

| Field | Required | Notes |
| --- | --- | --- |
| `flowerId` | yes | 메시지를 생성할 꽃 ID |
| `selectedTagIds` | no | 메시지 맥락에 사용할 태그 ID 목록 |
| `curationResultId` | no | 연결할 내 큐레이션 결과 ID. 전달하면 해당 결과가 로그인 유저 소유여야 함 |
| `senderName` | yes | 보내는 사람 |
| `receiverName` | yes | 받는 사람 |

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
    "message": "지은님께...",
    "createdAt": "2026-05-22T14:30:00"
  }
}
```

기존 프론트 호환을 위해 `data.flowerId`, `data.message`는 유지합니다. `id`는 저장된 메시지 이력 ID입니다.

### GET /api/v1/users/me/messages

내가 생성한 메시지 목록을 최신순으로 조회합니다.

Query parameters:

| Name | Type | Required | Default | Notes |
| --- | --- | --- | --- | --- |
| `page` | number | no | 0 | zero-based |
| `size` | number | no | 20 | max 100 |

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

저장 정책:

- 메시지 생성 성공 후 저장합니다.
- OpenAI 실패 후 템플릿 fallback으로 성공한 메시지도 저장합니다.
- 메시지 목록은 로그인 유저 본인 데이터만 반환합니다.

## 2. 큐레이션 결과 저장/조회

### POST /api/v1/curation/results

기존 큐레이션 결과 계산 API입니다. 인증은 계속 optional입니다.

- Bearer 토큰이 없으면 기존처럼 결과만 계산하고 저장하지 않습니다.
- 유효한 Bearer 토큰이 있으면 결과/선택값/추천 꽃 목록 snapshot을 로그인 유저 이력으로 저장합니다.
- 응답의 기존 page shape는 유지합니다.

> 향후 개선 메모(현재 계약 아님)
>
> 현재 `POST /api/v1/curation/results`는 큐레이션 결과를 저장하더라도 응답에 저장된 결과 ID를 직접 포함하지 않습니다. 프론트는 현재 명세대로 큐레이션 완료 응답의 `data.content`를 사용해 추천 결과 화면을 렌더링하고, 메시지 작성 등에서 저장된 결과 ID가 필요할 때 `GET /api/v1/users/me/curation-results/latest`를 별도로 호출해 `data.id`를 얻는 흐름을 사용합니다.
>
> 추후 프론트 호출 수를 줄이고 큐레이션 완료 직후 메시지 생성까지 더 안정적으로 연결하려면, 로그인 사용자에 한해 `POST /api/v1/curation/results` 응답에 `curationResultId` 또는 별도 metadata 필드를 추가하는 확장을 검토할 수 있습니다. 이 경우 익명 요청은 저장되지 않으므로 해당 값은 `null`이거나 생략되어야 합니다.
>
> 이 개선을 적용할 때는 기존 page shape를 깨지 않는 방식이어야 합니다. 예를 들어 `data.content`, `page`, `size`, `totalElements`, `totalPages`, `hasNext`는 유지하고, 추가 필드만 더하는 하위 호환 확장으로 설계해야 합니다. 또한 프론트가 이미 `latest` 조회 기반으로 구현 중이므로, 실제 계약 변경 전에는 프론트/백엔드 양쪽 일정과 배포 순서를 맞춘 뒤 별도 변경 이슈로 진행합니다.

저장 대상:

| Field | Notes |
| --- | --- |
| `flowVersion` | 요청의 flow version |
| `selections` | 6단계 선택값 전체 |
| `recommendations` | 응답 시점의 추천 꽃 snapshot. 다시보기 결과가 나중의 꽃/태그 수정으로 바뀌지 않도록 저장 |
| `createdAt` | 저장 시각 |

### GET /api/v1/users/me/curation-results

내 큐레이션 결과 이력을 최신순으로 조회합니다.

Query parameters:

| Name | Type | Required | Default | Notes |
| --- | --- | --- | --- | --- |
| `page` | number | no | 0 | zero-based |
| `size` | number | no | 20 | max 100 |

Response:

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
          { "step": "RECIPIENT", "code": "LOVER", "label": "연인" },
          { "step": "EMOTION", "code": "LOVE", "label": "사랑" },
          { "step": "FLOWER_MEANING", "code": "LOVE_3", "label": "깊은 애정" },
          { "step": "SPACE", "code": "DESK_SMALL", "label": "책상 위" },
          { "step": "BUDGET", "code": "BUDGET_MEDIUM", "label": "5~10만원" }
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

`topFlowers`는 목록 카드용 preview입니다. 최대 3개를 권장합니다.

### GET /api/v1/users/me/curation-results/latest

로그인 유저의 최신 큐레이션 결과를 반환합니다. 메시지 탭 초기 진입에서 이 API를 사용합니다.

Response:

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

내 결과가 없으면 `404 CURATION_RESULT_NOT_FOUND`를 반환합니다.

### GET /api/v1/users/me/curation-results/{resultId}

큐레이션 결과 다시보기 상세 조회입니다. 응답 shape는 `latest`와 동일합니다.

Rules:

- `resultId`가 로그인 유저 소유가 아니면 `404 CURATION_RESULT_NOT_FOUND`를 반환합니다.
- 상세 응답의 `recommendations`는 저장된 snapshot 기준입니다.

## 3. 꽃 도감 필터

### GET /api/v1/flowers

기존 꽃 도감 목록 API에 필터 쿼리 파라미터를 추가합니다.

Query parameters:

| Name | Type | Required | Default | Notes |
| --- | --- | --- | --- | --- |
| `keyword` | string | no | null | `name`, `coreMeaning`, `description`, `scientificName`, `origin` 검색 |
| `priceRange` | string | no | null | `LOW`, `MEDIUM`, `HIGH`, `PREMIUM` |
| `isPetSafe` | boolean | no | null | `true`: 반려동물 안전 꽃만, `false`: 반려동물 주의 꽃만, omitted: 전체 |
| `managementLevel` | string | no | null | `EASY`, `NORMAL`, `HARD` |
| `tagIds` | number[] | no | empty | 반복 쿼리. 예: `tagIds=5&tagIds=9`. 모든 태그를 가진 꽃만 반환 |
| `page` | number | no | 0 | zero-based |
| `size` | number | no | 20 | max 100 |

Example:

```http
GET /api/v1/flowers?keyword=장미&priceRange=MEDIUM&isPetSafe=true&managementLevel=NORMAL&tagIds=5&tagIds=9&page=0&size=20
```

Response item 추가 필드:

```json
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
```

Repository/cache 구현 조건:

- 필터는 paging 이전의 전체 결과에 적용합니다.
- `tagIds`가 있으면 `flower_tag_mappings`/`tags`를 join하고 `distinct` 또는 group by로 중복 꽃을 제거합니다.
- 여러 `tagIds`는 AND 조건입니다. 즉 요청한 모든 태그가 매핑된 꽃만 반환합니다.
- cache key에는 `keyword`, `priceRange`, `isPetSafe`, `managementLevel`, 정렬된 `tagIds`, `page`, `size`를 모두 포함합니다.
- 관리자 꽃/태그/매핑 변경 시 기존 `flowers` cache를 evict합니다.

## 4. 꽃 자체 정보 필드

관리 정보(`managementLevel`, `managementInfo`)와 별도로 꽃 자체 설명 필드를 추가합니다. 모든 신규 필드는 nullable로 시작해 기존 데이터 마이그레이션 부담을 줄입니다.

### Public response fields

`GET /api/v1/flowers/{flowerId}` 응답에 다음 필드를 추가합니다.

```json
{
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
  "tags": []
}
```

| Field | Type | Notes |
| --- | --- | --- |
| `description` | string | 꽃 소개/특징 본문 |
| `scientificName` | string | 학명 |
| `origin` | string | 원산지 또는 주요 분포 |
| `bloomingSeason` | string | 개화 시기 |
| `scent` | string | 향 정보 |

`GET /api/v1/flowers` 목록 응답에는 카드 노출용으로 `description`만 추가합니다.

### Admin request/response fields

`POST /api/v1/admin/flowers`, `PUT /api/v1/admin/flowers/{flowerId}`, `GET /api/v1/admin/flowers/{flowerId}`에도 같은 필드를 추가합니다.

Request example:

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

권장 DB 컬럼:

| Column | Type | Notes |
| --- | --- | --- |
| `description` | `TEXT` | nullable |
| `scientific_name` | `VARCHAR(150)` | nullable |
| `origin` | `VARCHAR(100)` | nullable |
| `blooming_season` | `VARCHAR(100)` | nullable |
| `scent` | `VARCHAR(100)` | nullable |
