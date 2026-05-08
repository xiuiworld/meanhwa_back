# Meanhwa API Contract - Phase 0-2

This document covers the MVP backend contract for the flower dictionary, tag curation, and template message generation APIs.

## Common Response

Success responses are wrapped with `ApiResponse`.

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {}
}
```

Error responses are returned by the global exception handler.

```json
{
  "status": 400,
  "errorCode": "INVALID_REQUEST",
  "message": "요청값이 올바르지 않습니다."
}
```

Current error codes:

| errorCode | HTTP | Description |
| --- | ---: | --- |
| `INVALID_REQUEST` | 400 | Invalid request parameter or body |
| `INVALID_PRICE_RANGE` | 400 | Unsupported `priceRange` value |
| `FLOWER_NOT_FOUND` | 404 | Flower does not exist |
| `TAG_NOT_FOUND` | 404 | One or more tags do not exist |
| `INTERNAL_SERVER_ERROR` | 500 | Unexpected server error |

## Enum Values

`managementLevel`

```text
EASY, NORMAL, HARD
```

`priceRange`

```text
LOW, MEDIUM, HIGH, PREMIUM
```

`tag.category`

```text
EVENT, RELATION, EMOTION, STYLE, CARE
```

## GET /api/v1/flowers

Returns a paginated flower/plant dictionary list.

Query parameters:

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `keyword` | string | no | null | Searches `name` and `coreMeaning` |
| `page` | number | no | 0 | Zero-based page index |
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
    "totalElements": 12,
    "totalPages": 1,
    "hasNext": false
  }
}
```

## GET /api/v1/flowers/{flowerId}

Returns flower detail metadata and connected tags.

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

## GET /api/v1/tags

Returns tags grouped by category.

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

## GET /api/v1/curation

Returns recommended flowers sorted by total matched tag weight.

Query parameters:

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `tagIds` | number[] | no | empty | Repeated query parameter, such as `tagIds=1&tagIds=9` |
| `isPetSafe` | boolean | no | null | `true` returns only non-toxic flowers |
| `priceRange` | string | no | null | One of `LOW`, `MEDIUM`, `HIGH`, `PREMIUM` |
| `page` | number | no | 0 | Zero-based page index |
| `size` | number | no | 20 | Page size |

Example:

```http
GET /api/v1/curation?tagIds=5&tagIds=9&isPetSafe=true&priceRange=MEDIUM
```

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
          },
          {
            "id": 9,
            "category": "EMOTION",
            "name": "사랑"
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

Frontend notes:

- `score` is the sum of selected tag mapping weights.
- If `tagIds` is omitted, the API returns all flowers matching filters with `score: 0`.
- `isPetSafe` is derived from backend toxicity data. The DB field is `is_toxic_to_pets`; the API exposes the safer frontend flag `isPetSafe`.

## POST /api/v1/messages/generate

Generates a gift card message. Phase 2 uses a template generator behind the `MessageGenerator` interface.

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
    "message": "지은님께,\n\n민수님이 연인, 사랑의 마음을 담아 장미을(를) 전합니다.\n장미의 꽃말은 \"사랑과 열정\"입니다.\n오늘의 마음이 오래 기억되는 선물이 되길 바랍니다.\n\n- 민수 드림"
  }
}
```

Frontend notes:

- Validate `senderName` and `receiverName` before submit.
- `selectedTagIds` may be empty or omitted. In that case the template uses a generic phrase.
- Future LLM replacement should keep this endpoint contract stable.
