# 큐레이션 위저드 옵션 코드표

6단계 큐레이션 위저드의 현재 `flowVersion`은 `2026-05-v1`입니다. 이 문서는 프론트에서 상태로 저장해야 하는 `code`와 화면 표시용 `label`을 정리합니다.

실제 런타임 소스는 `src/main/resources/curation/flow-2026-05-v1.yml`입니다. API 요청/응답 규칙은 [api-contract.md](api-contract.md)의 Curation 섹션을 봅니다.

## 기본 규칙

- 클라이언트 상태에는 `label`이 아니라 `{ step, code }`를 저장합니다.
- `label`은 표시용입니다.
- `flowVersion`이 바뀌면 저장해 둔 위저드 상태를 폐기하고 1단계부터 다시 시작합니다.
- Step 1-5의 `code`는 `tags.code`로 tag id를 찾고 추천 점수에 반영합니다.
- Step 6 `BUDGET_*`는 태그가 아니라 `PriceRange` 필터로만 사용합니다.
- `tags.code`와 신규 위저드 태그는 로컬 seed `data.sql` 및 Flyway `V2__seed_curation_reference_data.sql`로 관리합니다.

## Step

| Order | key | 기본 질문 | 분기 |
| ---: | --- | --- | --- |
| 1 | `OCCASION` | 어떤 날인가요? | 없음 |
| 2 | `RECIPIENT` | 누구에게 전하는 선물인가요? | Step1 `OCCASION` |
| 3 | `EMOTION` | 어떤 마음을 전하고 싶나요? | Step1 `OCCASION` |
| 4 | `FLOWER_MEANING` | 어떤 꽃말의 결에 가까운가요? | Step3 `EMOTION` |
| 5 | `SPACE` | 받는 분의 공간 환경은? | 없음 |
| 6 | `BUDGET` | 어느 정도 예산으로 준비하나요? | 없음 |

Step4 질문 title은 Step2 수신자에 따라 일부 바뀝니다.

| 상황 | 문구 |
| --- | --- |
| Step3 기본 | 어떤 **마음**을 전하고 싶나요? 선물에 담을 감정을 골라주세요. |
| Step4 기본 | 어떤 **꽃말의 결**에 가까운가요? 전하고 싶은 마음을 조금 더 구체적으로 들려주세요. |
| Step4 분기 예시 | `RECIPIENT=FAMILY` -> "가족에게 전달하고 싶은 꽃말은 무엇인가요?" |

| Recipient code | Step4 questionTitle |
| --- | --- |
| `PARENT` | 부모님에게 전달하고 싶은 꽃말은 무엇인가요? |
| `FAMILY` | 가족에게 전달하고 싶은 꽃말은 무엇인가요? |
| `LOVER` | 연인에게 전달하고 싶은 꽃말은 무엇인가요? |
| default | 어떤 꽃말의 결에 가까운가요? |

## Step1 `OCCASION`

| code | label |
| --- | --- |
| `BIRTHDAY` | 생일 |
| `GRADUATION` | 졸업 |
| `PROMOTION` | 승진 |
| `WEDDING` | 결혼 |
| `HOUSEWARMING` | 집들이 |
| `RECOVERY` | 병문안·회복 |

## Step2 `RECIPIENT`

| OCCASION | options |
| --- | --- |
| `BIRTHDAY` | `FRIEND` 친구, `FAMILY` 가족, `LOVER` 연인, `COLLEAGUE` 직장 동료 |
| `GRADUATION` | `FRIEND` 친구, `FAMILY` 가족, `LOVER` 연인, `SENIOR_JUNIOR_MENTOR` 선후배·스승 |
| `PROMOTION` | `COLLEAGUE_JUNIOR` 동료·후배, `BOSS_SENIOR` 상사·선배, `FAMILY` 가족, `FRIEND_ACQUAINTANCE` 친구·지인 |
| `WEDDING` | `FRIEND` 친구, `FAMILY` 가족, `COLLEAGUE` 직장 동료, `VIP_MENTOR` 은사·귀빈 |
| `HOUSEWARMING` | `FRIEND` 친구, `FAMILY` 가족, `LOVER` 연인, `COLLEAGUE` 직장 동료 |
| `RECOVERY` | `FAMILY` 가족, `FRIEND` 친구, `LOVER` 연인, `COLLEAGUE` 직장 동료 |

## Step3 `EMOTION`

| OCCASION | options |
| --- | --- |
| `BIRTHDAY` | `CELEBRATION` 축하, `GRATITUDE` 감사, `LOVE` 사랑, `SUPPORT` 응원 |
| `GRADUATION` | `CELEBRATION` 축하, `SUPPORT` 응원, `ENCOURAGEMENT` 격려, `LEAP` 도약 |
| `PROMOTION` | `CELEBRATION` 축하, `RESPECT` 존경, `ENCOURAGEMENT` 격려, `PRIDE` 자부심 |
| `WEDDING` | `CELEBRATION` 축하, `BLESSING` 축복, `SINCERITY` 진심, `ETERNITY` 영원 |
| `HOUSEWARMING` | `CELEBRATION` 축하, `GRATITUDE` 감사, `PEACE` 평온, `PROSPERITY` 번창 |
| `RECOVERY` | `COMFORT` 위로, `SUPPORT` 응원, `GET_WELL` 쾌유, `PEACE` 평온 |

## Step4 `FLOWER_MEANING`

Step4 code는 `{EMOTION}_{1..4}` 형식입니다.

| emotion | code | label |
| --- | --- | --- |
| `LOVE` | `LOVE_1` | 변함없는 마음 |
| `LOVE` | `LOVE_2` | 첫사랑의 설렘 |
| `LOVE` | `LOVE_3` | 소중한 당신 |
| `LOVE` | `LOVE_4` | 진실한 사랑 |
| `SUPPORT` | `SUPPORT_1` | 언제나 응원해 |
| `SUPPORT` | `SUPPORT_2` | 변치 않는 우정 |
| `SUPPORT` | `SUPPORT_3` | 찬란한 미소 |
| `SUPPORT` | `SUPPORT_4` | 매일의 행복 |
| `ENCOURAGEMENT` | `ENCOURAGEMENT_1` | 용기와 자신감 |
| `ENCOURAGEMENT` | `ENCOURAGEMENT_2` | 새로운 도전 |
| `ENCOURAGEMENT` | `ENCOURAGEMENT_3` | 당당한 발걸음 |
| `ENCOURAGEMENT` | `ENCOURAGEMENT_4` | 무한한 가능성 |
| `CELEBRATION` | `CELEBRATION_1` | 화사한 축하 |
| `CELEBRATION` | `CELEBRATION_2` | 빛나는 성취 |
| `CELEBRATION` | `CELEBRATION_3` | 새로운 시작 |
| `CELEBRATION` | `CELEBRATION_4` | 함께한 기쁨 |
| `GRATITUDE` | `GRATITUDE_1` | 진심 어린 고마움 |
| `GRATITUDE` | `GRATITUDE_2` | 함께해서 행복 |
| `GRATITUDE` | `GRATITUDE_3` | 오래된 인연 |
| `GRATITUDE` | `GRATITUDE_4` | 따뜻한 기억 |
| `BLESSING` | `BLESSING_1` | 행복한 시작 |
| `BLESSING` | `BLESSING_2` | 아름다운 인연 |
| `BLESSING` | `BLESSING_3` | 아낌없는 축복 |
| `BLESSING` | `BLESSING_4` | 조화와 화합 |
| `SINCERITY` | `SINCERITY_1` | 진심을 담아 |
| `SINCERITY` | `SINCERITY_2` | 소중한 인연 |
| `SINCERITY` | `SINCERITY_3` | 영원한 약속 |
| `SINCERITY` | `SINCERITY_4` | 고귀한 사랑 |
| `ETERNITY` | `ETERNITY_1` | 영원히 하나됨 |
| `ETERNITY` | `ETERNITY_2` | 영원한 사랑 |
| `ETERNITY` | `ETERNITY_3` | 아름다운 시작 |
| `ETERNITY` | `ETERNITY_4` | 고귀한 인연 |
| `RESPECT` | `RESPECT_1` | 깊은 존경 |
| `RESPECT` | `RESPECT_2` | 명예와 인정 |
| `RESPECT` | `RESPECT_3` | 굳건한 신뢰 |
| `RESPECT` | `RESPECT_4` | 탄탄대로 |
| `PRIDE` | `PRIDE_1` | 값진 노력 |
| `PRIDE` | `PRIDE_2` | 빛나는 성공 |
| `PRIDE` | `PRIDE_3` | 끊임없는 성장 |
| `PRIDE` | `PRIDE_4` | 위풍당당 |
| `LEAP` | `LEAP_1` | 희망과 도약 |
| `LEAP` | `LEAP_2` | 밝은 앞날 |
| `LEAP` | `LEAP_3` | 무한한 가능성 |
| `LEAP` | `LEAP_4` | 꿈을 향해 |
| `PEACE` | `PEACE_1` | 평온한 일상 |
| `PEACE` | `PEACE_2` | 편안한 공간 |
| `PEACE` | `PEACE_3` | 마음의 안정 |
| `PEACE` | `PEACE_4` | 따뜻한 온기 |
| `PROSPERITY` | `PROSPERITY_1` | 풍요와 번영 |
| `PROSPERITY` | `PROSPERITY_2` | 피어나는 기쁨 |
| `PROSPERITY` | `PROSPERITY_3` | 번창하는 일상 |
| `PROSPERITY` | `PROSPERITY_4` | 가정의 행복 |
| `COMFORT` | `COMFORT_1` | 따뜻한 위안 |
| `COMFORT` | `COMFORT_2` | 깊은 배려 |
| `COMFORT` | `COMFORT_3` | 평온한 휴식 |
| `COMFORT` | `COMFORT_4` | 마음의 안계 |
| `GET_WELL` | `GET_WELL_1` | 빠른 회복 |
| `GET_WELL` | `GET_WELL_2` | 다시 찾은 활력 |
| `GET_WELL` | `GET_WELL_3` | 건강한 내일 |
| `GET_WELL` | `GET_WELL_4` | 희망의 빛 |

## Step5 `SPACE`

| code | label |
| --- | --- |
| `DESK_SMALL` | 책상·좁은 공간 |
| `LIVING_ROOM` | 거실·넓은 실내 |
| `WINDOW_BRIGHT` | 창가·밝은 실내 |
| `BALCONY_OUTDOOR` | 베란다·실외 |

## Step6 `BUDGET`

| code | label | PriceRange |
| --- | --- | --- |
| `BUDGET_LOW` | 실속형 (5만 원 이하) | `LOW` |
| `BUDGET_MEDIUM` | 기본형 (5~10만 원) | `MEDIUM` |
| `BUDGET_HIGH` | 고급형 (10~20만 원) | `HIGH` |
| `BUDGET_PREMIUM` | 프리미엄 (20만 원 이상) | `PREMIUM` |
