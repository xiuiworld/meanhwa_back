# 운영 DB 마이그레이션 (큐레이션 위저드 v2)

분기형 큐레이션(6단계 위저드)이 `tags.code`로 태그를 찾습니다.  
운영 RDS에는 `data.sql`이 자동 실행되지 않으므로(`spring.sql.init.mode: never`) 아래 SQL을 **배포 전 또는 직후** 수동 적용합니다.

## 파일

| 파일 | 용도 |
| --- | --- |
| [2026-05-curation-wizard-prod.sql](2026-05-curation-wizard-prod.sql) | MySQL 8 / RDS용 일괄 스크립트 (재실행 가능) |

## 적용 시점

- 프론트가 `POST /api/v1/curation/results` 또는 `GET /curation/flow` 를 **운영**에서 쓰기 **전**
- 백엔드 위저드 코드가 포함된 이미지 배포와 같은 릴리스 권장

## 실행 방법

1. RDS 백업(스냅샷) 또는 `tags`, `flower_tag_mappings` 테이블 덤프
2. [deployment.md](../deployment.md) 의 RDS 접속 절차로 `mysql` 접속
3. 스크립트 실행:

```bash
mysql -h {RDS_HOST} -P 3306 -u {DB_USERNAME} -p meanhwa < docs/migration/2026-05-curation-wizard-prod.sql
```

4. 스크립트 말미 **검증 SELECT** 결과 확인 (`missing_wizard_codes` 가 0건)

## 스크립트가 하는 일

1. `tags.code` 컬럼 추가 (없을 때만)
2. 기존 태그(생일·연인·사랑 등)에 `code` 백필 — **이름·카테고리** 기준
3. 위저드용 신규 태그 INSERT (`code` 중복 시 스킵, PDF 2026-05 꽃말 표시명)
3b. 이미 INSERT 된 DB용 `UPDATE tags SET name=...` (MEANING 표시명만 PDF 문구로 갱신)
4. `flower_tag_mappings` — 신규 태그에 대해 유사 기존 태그 weight **복제** (로컬 `data.sql` 과 동일 전략)
5. 검증 쿼리

## 주의

- 태그 **이름을 운영에서 바꿔 둔 경우** `code` 백필이 안 될 수 있습니다. 검증 후 admin CMS에서 수정하세요.
- `weight` 는 기획 최종본이 아니라 **초기 시드(복제)** 입니다. 추천 품질 튜닝은 별도 작업입니다.
- `BUDGET_*` 코드는 DB 태그가 아니라 `PriceRange` 필터만 사용합니다 (YAML 기준).

## 롤백

- 컬럼 추가만 한 경우: 신규 tag row·mapping row 삭제 후 `code` 컬럼 drop (운영 정책에 맞게 결정)
- 배포 롤백: 이전 백엔드 이미지 + DB는 위저드 태그를 남겨도 레거시 API에는 영향 없음
