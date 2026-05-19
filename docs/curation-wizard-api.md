# 분기형 큐레이션 API 명세 (v2, 초안)

> **목적**: 프론트엔드 병렬 개발용. 백엔드 구현 전에 계약(Contract)을 고정합니다.  
> **상태**: `DRAFT` — 구현 전 리뷰 대상. `flowVersion` 변경 시 프론트는 캐시 무효화 필요.  
> **기존 API**: `GET /api/v1/tags`, `GET /api/v1/curation?tagIds=...` 는 유지·Deprecated 예정. 신규 플로우는 본 문서 API를 사용합니다.

---

## 1. 플로우 개요

총 **6단계**. 1~4단계는 **이전 선택에 따라 선택지가 달라짐**(분기). 5~6단계는 고정 선택지.

| Step | key | 질문 (기본 문구) | 분기 |
| ---: | --- | --- | --- |
| 1 | `OCCASION` | 어떤 날인가요? 선물을 드리는 **상황**을 골라주세요. | 없음 (6지선다) |
| 2 | `RECIPIENT` | 누구에게 전하는 선물인가요? **받으실 분**을 선택해 주세요. | Step1 `OCCASION` |
| 3 | `EMOTION` | 어떤 **마음**을 전하고 싶나요? 선물에 담을 감정을 골라주세요. | Step1 `OCCASION` |
| 4 | `FLOWER_MEANING` | 어떤 **꽃말의 결**에 가까운가요? 전하고 싶은 마음을 조금 더 구체적으로 들려주세요. | Step3 `EMOTION` (선택 메시지 15종) |
| 5 | `SPACE` | 받는 분의 **공간 환경**은? 식물이 놓일 공간을 알려주세요. | 없음 (4지선다) |
| 6 | `BUDGET` | 어느 정도 **예산**으로 준비하나요? | 없음 (4지선다) |

**동적 질문 문구 (UX)**  
서버는 각 단계마다 `questionTitle`, `questionSubtitle`을 내려줍니다.  
예: Step4에서 Step2=`PARENT`이면  
`"어떤 꽃말의 결에 가까운가요?"` → `"부모님에게 전달하고 싶은 꽃말은 무엇인가요?"`

---

## 2. 공통 규칙

### 2.1 Base path

```text
/api/v1/curation
```

### 2.2 선택값 전달 형식

모든 선택지는 **안정적인 문자열 코드 `code`** 로 식별합니다. (화면 표시 `label`과 분리)

클라이언트는 단계가 진행될 때마다 누적합니다.

```ts
type CurationSelection = {
  step: CurationStepKey;
  code: string;
};

type CurationStepKey =
  | "OCCASION"
  | "RECIPIENT"
  | "EMOTION"
  | "FLOWER_MEANING"
  | "SPACE"
  | "BUDGET";
```

### 2.3 플로우 버전

```text
flowVersion: "2026-05-v1"
```

- `GET .../flow` 응답에 포함.
- 백엔드가 규칙·태그 매핑을 바꾸면 버전을 올림.
- 프론트는 `localStorage` 등에 `(flowVersion, selections)` 저장 시 버전 불일치면 1단계부터 리셋 권장.

### 2.4 신규 에러 코드 (추가 예정)

| errorCode | HTTP | Meaning |
| --- | ---: | --- |
| `INVALID_CURATION_STEP` | 400 | 잘못된 step key |
| `INVALID_CURATION_SELECTION` | 400 | 허용되지 않는 code 조합 |
| `INCOMPLETE_CURATION_SELECTION` | 400 | 결과 요청 시 6단계 미완료 |
| `CURATION_FLOW_NOT_FOUND` | 404 | 알 수 없는 flowVersion |

기존 `TAG_NOT_FOUND`, `INVALID_PRICE_RANGE` 등은 그대로 사용 가능.

---

## 3. API 목록

| Method | Path | 용도 |
| --- | --- | --- |
| `GET` | `/api/v1/curation/flow` | 플로우 메타(버전, 단계 정의, 분기 규칙 요약) |
| `GET` | `/api/v1/curation/steps/{stepKey}/options` | 해당 단계 선택지 + 동적 질문 문구 |
| `POST` | `/api/v1/curation/results` | 최종 선택으로 꽃 목록(점수·페이지) |
| `GET` | `/api/v1/curation/results` | *(선택)* GET 호환용. POST 권장 |

---

## 4. `GET /api/v1/curation/flow`

플로우 전체 구조를 한 번에 받을 때 사용 (오프라인 캐시·스토리북용).

### Response `data`

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
    },
    {
      "key": "RECIPIENT",
      "order": 2,
      "defaultQuestionTitle": "누구에게 전하는 선물인가요?",
      "defaultQuestionSubtitle": "받으실 분을 선택해 주세요.",
      "selectionMode": "SINGLE",
      "dependsOn": ["OCCASION"]
    },
    {
      "key": "EMOTION",
      "order": 3,
      "defaultQuestionTitle": "어떤 마음을 전하고 싶나요?",
      "defaultQuestionSubtitle": "선물에 담을 감정을 골라주세요.",
      "selectionMode": "SINGLE",
      "dependsOn": ["OCCASION"]
    },
    {
      "key": "FLOWER_MEANING",
      "order": 4,
      "defaultQuestionTitle": "어떤 꽃말의 결에 가까운가요?",
      "defaultQuestionSubtitle": "전하고 싶은 마음을 조금 더 구체적으로 들려주세요.",
      "selectionMode": "SINGLE",
      "dependsOn": ["EMOTION"]
    },
    {
      "key": "SPACE",
      "order": 5,
      "defaultQuestionTitle": "받는 분의 공간 환경은?",
      "defaultQuestionSubtitle": "식물이 놓일 공간을 알려주세요.",
      "selectionMode": "SINGLE",
      "dependsOn": []
    },
    {
      "key": "BUDGET",
      "order": 6,
      "defaultQuestionTitle": "어느 정도 예산으로 준비하나요?",
      "defaultQuestionSubtitle": "원하시는 가격대를 알려주세요.",
      "selectionMode": "SINGLE",
      "dependsOn": []
    }
  ]
}
```

---

## 5. `GET /api/v1/curation/steps/{stepKey}/options`

현재까지의 선택을 쿼리로 넘기면, **해당 단계에서 고를 수 있는 옵션만** 반환합니다.

### Path

| Name | Values |
| --- | --- |
| `stepKey` | `OCCASION` \| `RECIPIENT` \| `EMOTION` \| `FLOWER_MEANING` \| `SPACE` \| `BUDGET` |

### Query

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `flowVersion` | string | no | 미지정 시 서버 최신 버전 |
| `selections` | string (JSON) | 조건부 | 이전 단계 선택 배열 URL-encoded JSON |

`selections` 예시 (Step3 요청 시):

```json
[
  { "step": "OCCASION", "code": "BIRTHDAY" },
  { "step": "RECIPIENT", "code": "LOVER" }
]
```

요청 URL 예:

```http
GET /api/v1/curation/steps/EMOTION/options?selections=%5B%7B%22step%22%3A%22OCCASION%22%2C%22code%22%3A%22BIRTHDAY%22%7D%5D
```

| stepKey | selections 필수 여부 |
| --- | --- |
| `OCCASION` | 불필요 |
| `RECIPIENT` | `OCCASION` 1개 필수 |
| `EMOTION` | `OCCASION` 필수 |
| `FLOWER_MEANING` | `OCCASION`, `RECIPIENT`, `EMOTION` 필수 |
| `SPACE`, `BUDGET` | 불필요 (질문 문구만 Step2 반영 가능) |

### Response `data`

```json
{
  "flowVersion": "2026-05-v1",
  "step": "RECIPIENT",
  "order": 2,
  "questionTitle": "누구에게 전하는 선물인가요?",
  "questionSubtitle": "받으실 분을 선택해 주세요.",
  "options": [
    {
      "code": "FRIEND",
      "label": "친구",
      "tagId": 106,
      "description": null
    }
  ]
}
```

| Field | Type | Notes |
| --- | --- | --- |
| `options[].code` | string | 클라이언트·서버 공통 식별자 |
| `options[].label` | string | UI 표시 |
| `options[].tagId` | number \| null | DB `tags.id`. 구현 전 `null` 허용(Mock) |
| `options[].description` | string \| null | 보조 설명 (선택) |

**Step4 질문 문구 분기 예** (`RECIPIENT=PARENT`):

```json
{
  "step": "FLOWER_MEANING",
  "questionTitle": "부모님에게 전달하고 싶은 꽃말은 무엇인가요?",
  "questionSubtitle": null
}
```

---

## 6. 선택지 코드표 (기획안 v2026-05-v1)

> `tagId`는 백엔드 마이그레이션 후 채웁니다. 프론트는 **`code` 기준**으로 개발합니다.

### 6.1 Step1 — `OCCASION`

| code | label |
| --- | --- |
| `BIRTHDAY` | 생일 |
| `GRADUATION` | 졸업 |
| `PROMOTION` | 승진 |
| `WEDDING` | 결혼 |
| `HOUSEWARMING` | 집들이 |
| `RECOVERY` | 병문안·회복 |

### 6.2 Step2 — `RECIPIENT` (분기)

**`OCCASION=BIRTHDAY`**

| code | label |
| --- | --- |
| `FRIEND` | 친구 |
| `FAMILY` | 가족 |
| `LOVER` | 연인 |
| `COLLEAGUE` | 직장 동료 |

**`OCCASION=GRADUATION`**

| code | label |
| --- | --- |
| `FRIEND` | 친구 |
| `FAMILY` | 가족 |
| `LOVER` | 연인 |
| `SENIOR_JUNIOR_MENTOR` | 선후배·스승 |

**`OCCASION=PROMOTION`**

| code | label |
| --- | --- |
| `COLLEAGUE_JUNIOR` | 동료·후배 |
| `BOSS_SENIOR` | 상사·선배 |
| `FAMILY` | 가족 |
| `FRIEND_ACQUAINTANCE` | 친구·지인 |

**`OCCASION=WEDDING`**

| code | label |
| --- | --- |
| `FRIEND` | 친구 |
| `FAMILY` | 가족 |
| `COLLEAGUE` | 직장 동료 |
| `VIP_MENTOR` | 은사·귀빈 |

**`OCCASION=HOUSEWARMING`**

| code | label |
| --- | --- |
| `FRIEND` | 친구 |
| `FAMILY` | 가족 |
| `LOVER` | 연인 |
| `COLLEAGUE` | 직장 동료 |

**`OCCASION=RECOVERY`**

| code | label |
| --- | --- |
| `FAMILY` | 가족 |
| `FRIEND` | 친구 |
| `LOVER` | 연인 |
| `COLLEAGUE` | 직장 동료 |

### 6.3 Step3 — `EMOTION` (Step1 상황별)

| OCCASION | codes (label) |
| --- | --- |
| `BIRTHDAY` | `CELEBRATION`(축하), `GRATITUDE`(감사), `LOVE`(사랑), `SUPPORT`(응원) |
| `GRADUATION` | `CELEBRATION`, `SUPPORT`, `ENCOURAGEMENT`(격려), `LEAP`(도약) |
| `PROMOTION` | `CELEBRATION`, `RESPECT`(존경), `ENCOURAGEMENT`, `PRIDE`(자부심) |
| `WEDDING` | `CELEBRATION`, `BLESSING`(축복), `SINCERITY`(진심), `ETERNITY`(영원) |
| `HOUSEWARMING` | `CELEBRATION`, `GRATITUDE`, `PEACE`(평온), `PROSPERITY`(번창) |
| `RECOVERY` | `COMFORT`(위로), `SUPPORT`, `GET_WELL`(쾌유), `PEACE` |

### 6.4 Step4 — `FLOWER_MEANING` (Step3 `EMOTION` = 선택 메시지)

각 감정 코드당 4개 하위 꽃말. `code` 패턴: `{EMOTION}_{INDEX}` (예: `LOVE_1`).

**`EMOTION=LOVE` (사랑)**

| code | label |
| --- | --- |
| `LOVE_1` | 변함없는 마음 |
| `LOVE_2` | 첫사랑의 설렘 |
| `LOVE_3` | 소중한 당신 |
| `LOVE_4` | 진실한 사랑 |

**`EMOTION=SUPPORT` (응원)**

| code | label |
| --- | --- |
| `SUPPORT_1` | 언제나 응원해 |
| `SUPPORT_2` | 변치 않는 우정 |
| `SUPPORT_3` | 찬란한 미소 |
| `SUPPORT_4` | 매일의 행복 |

**`EMOTION=ENCOURAGEMENT` (격려)**

| code | label |
| --- | --- |
| `ENCOURAGEMENT_1` | 용기와 자신감 |
| `ENCOURAGEMENT_2` | 새로운 도전 |
| `ENCOURAGEMENT_3` | 당당한 발걸음 |
| `ENCOURAGEMENT_4` | 무한한 가능성 |

**`EMOTION=CELEBRATION` (축하)**

| code | label |
| --- | --- |
| `CELEBRATION_1` | 화사한 축하 |
| `CELEBRATION_2` | 빛나는 성취 |
| `CELEBRATION_3` | 새로운 시작 |
| `CELEBRATION_4` | 함께한 기쁨 |

**`EMOTION=GRATITUDE` (감사)**

| code | label |
| --- | --- |
| `GRATITUDE_1` | 진심 어린 고마움 |
| `GRATITUDE_2` | 함께해서 행복 |
| `GRATITUDE_3` | 오래된 인연 |
| `GRATITUDE_4` | 따뜻한 기억 |

**`EMOTION=BLESSING` (축복)**

| code | label |
| --- | --- |
| `BLESSING_1` | 행복한 시작 |
| `BLESSING_2` | 아름다운 인연 |
| `BLESSING_3` | 아낌없는 축복 |
| `BLESSING_4` | 조화와 화합 |

**`EMOTION=SINCERITY` (진심)**

| code | label |
| --- | --- |
| `SINCERITY_1` | 진심을 담아 |
| `SINCERITY_2` | 소중한 인연 |
| `SINCERITY_3` | 영원한 약속 |
| `SINCERITY_4` | 고귀한 사랑 |

**`EMOTION=ETERNITY` (영원)**

| code | label |
| --- | --- |
| `ETERNITY_1` | 영원히 하나됨 |
| `ETERNITY_2` | 영원한 사랑 |
| `ETERNITY_3` | 아름다운 시작 |
| `ETERNITY_4` | 고귀한 인연 |

**`EMOTION=RESPECT` (존경)**

| code | label |
| --- | --- |
| `RESPECT_1` | 깊은 존경 |
| `RESPECT_2` | 영예와 인정 |
| `RESPECT_3` | 굳건한 신뢰 |
| `RESPECT_4` | 탄탄대로 |

**`EMOTION=PRIDE` (자부심)**

| code | label |
| --- | --- |
| `PRIDE_1` | 값진 노력 |
| `PRIDE_2` | 빛나는 성공 |
| `PRIDE_3` | 끊임없는 성장 |
| `PRIDE_4` | 위풍당당 |

**`EMOTION=LEAP` (도약)**

| code | label |
| --- | --- |
| `LEAP_1` | 희망과 도약 |
| `LEAP_2` | 밝은 앞날 |
| `LEAP_3` | 무한한 가능성 |
| `LEAP_4` | 꿈을 향해 |

**`EMOTION=PEACE` (평온)**

| code | label |
| --- | --- |
| `PEACE_1` | 평온한 일상 |
| `PEACE_2` | 편안한 공간 |
| `PEACE_3` | 마음의 안정 |
| `PEACE_4` | 따뜻한 온기 |

**`EMOTION=PROSPERITY` (번창)**

| code | label |
| --- | --- |
| `PROSPERITY_1` | 풍요와 번창 |
| `PROSPERITY_2` | 피어나는 기쁨 |
| `PROSPERITY_3` | 번창하는 일상 |
| `PROSPERITY_4` | 가정의 행복 |

**`EMOTION=COMFORT` (위로)**

| code | label |
| --- | --- |
| `COMFORT_1` | 따뜻한 위안 |
| `COMFORT_2` | 깊은 배려 |
| `COMFORT_3` | 평온한 휴식 |
| `COMFORT_4` | 마음의 안계 |

**`EMOTION=GET_WELL` (쾌유)**

| code | label |
| --- | --- |
| `GET_WELL_1` | 빠른 회복 |
| `GET_WELL_2` | 다시 찾은 활력 |
| `GET_WELL_3` | 건강한 내일 |
| `GET_WELL_4` | 희망의 빛 |

### 6.5 Step5 — `SPACE`

| code | label | 비고 (알고리즘) |
| --- | --- | --- |
| `DESK_SMALL` | 책상·좁은 공간 | ENVIRONMENT 태그 가중 |
| `LIVING_ROOM` | 거실·넓은 실내 | |
| `WINDOW_BRIGHT` | 창가·밝은 실내 | |
| `BALCONY_OUTDOOR` | 베란다·실외 | |

### 6.6 Step6 — `BUDGET`

| code | label | `priceRange` (기존 enum) |
| --- | --- | --- |
| `BUDGET_LOW` | 실속형 (5만 원 이하) | `LOW` |
| `BUDGET_MEDIUM` | 기본형 (5~10만 원) | `MEDIUM` |
| `BUDGET_HIGH` | 고급형 (10~20만 원) | `HIGH` |
| `BUDGET_PREMIUM` | 프리미엄 (20만 원 이상) | `PREMIUM` |

---

## 7. `POST /api/v1/curation/results`

6단계 선택을내면 꽃 목록을 점수 순으로 반환합니다.

### Request body

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

### Validation

- `selections`는 정확히 6개, `step` 중복 없음, `order` 1→6 순서와 무관하게 서버가 검증.
- 분기표에 없는 조합 → `400` + `INVALID_CURATION_SELECTION`.
- Step6 `BUDGET_*`는 **필터**로 `flowers.price_range` 적용 (태그 점수와 분리).

### Scoring (백엔드 구현 방향, 프론트 참고용)

| 입력 | 처리 |
| --- | --- |
| Step1~4, Step5 | 각 `code` → `tagId` 매핑 후 `flower_tag_mappings.weight` 합산 (기존과 동일) |
| Step6 | `priceRange` 필터 (미일치 제외) |
| 정렬 | `score` 내림차순 → `name` 오름차순 |

응답 `data`는 기존과 동일한 페이지 구조:

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "content": [
      {
        "flowerId": 1,
        "name": "장미",
        "imageUrl": "https://...",
        "coreMeaning": "사랑과 열정",
        "priceRange": "MEDIUM",
        "isPetSafe": true,
        "score": 18,
        "recommendationReason": "연인, 사랑 조건과 잘 맞고 5~10만원 예산대에 어울리는 추천입니다.",
        "matchedTags": [
          { "id": 205, "category": "RELATION", "name": "연인" },
          { "id": 309, "category": "EMOTION", "name": "사랑" }
        ]
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

추가 필드 (선택, 구현 시):

```json
{
  "curationSummary": {
    "occasionLabel": "생일",
    "recipientLabel": "연인",
    "emotionLabel": "사랑",
    "flowerMeaningLabel": "소중한 당신",
    "spaceLabel": "책상·좁은 공간",
    "budgetLabel": "기본형 (5~10만 원)"
  }
}
```

---

## 8. DB·태그 카테고리 변경 (백엔드 작업 메모, FE 참고)

| 항목 | 변경 |
| --- | --- |
| `TagCategory` | `MEANING` (꽃말 결) 추가 검토 |
| `tags` | 상황·대상·마음·꽃말·공간 코드별 row + `code` 컬럼 |
| `curation_option_rules` | (신규 테이블) `flow_version`, `step`, `parent_code`, `option_code`, `sort_order` |
| `flower_tag_mappings` | 신규 태그에 weight 재설정 |
| 기존 `GET /api/v1/tags` | 관리·사전용 유지, 위저드 UI는 **steps/options API** 사용 |

---

## 9. 프론트엔드 구현 체크리스트

1. Step 진행 시 `selections` 배열 누적 후 `GET .../steps/{step}/options?selections=...` 호출.
2. **항상 `code`로 상태 저장**, `label`은 표시만.
3. 뒤로 가기 시 이후 step 선택 제거 후 options 재조회.
4. Mock: 본 문서 §6 코드표로 options 하드코딩 가능 → 백엔드 연동 시 URL만 교체.
5. 결과 화면: `POST /api/v1/curation/results` (기존 `GET /api/v1/curation?tagIds=` 대체).
6. 클릭 로그: `POST /api/v1/action-logs/curation-result-click`에 `source: "curation-v2"`, `flowVersion`, `selections`(6단계) 전송. (`api-contract.md` Action Logs)

---

## 10. 구현 일정 제안

| Phase | 백엔드 | 프론트 |
| --- | --- | --- |
| P0 | 본 명세 확정 | Mock + UI 6단계 |
| P1 | `flow`, `steps/.../options` (정적 JSON/YAML) | API 연동 |
| P2 | 태그·매핑 DB 마이그레이션 + `results` 점수 | 결과·상세 연동 |
| P3 | `INVALID_CURATION_SELECTION` 검증, 로그 필드 | E2E |

---

## 11. 변경 이력

| Version | Date | Note |
| --- | --- | --- |
| `2026-05-v1` | 2026-05-17 | 분기형 6단계 큐레이션 초안 |
