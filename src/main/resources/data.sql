insert into flowers (id, name, image_url, core_meaning, description, scientific_name, origin, blooming_season, scent, management_level, management_info, is_toxic_to_pets, price_range, created_at, updated_at)
values
    (1, '장미', 'https://cdn.meanhwa.example/flowers/rose.jpg', '사랑과 열정', '선명한 색과 풍성한 꽃잎으로 마음을 직접적으로 전하기 좋은 대표적인 꽃입니다.', 'Rosa', '아시아, 유럽', '봄~초여름', '품종에 따라 은은하거나 진한 향', 'NORMAL', '햇빛이 잘 드는 곳에 두고 겉흙이 마르면 물을 주세요.', false, 'MEDIUM', current_timestamp, current_timestamp),
    (2, '해바라기', 'https://cdn.meanhwa.example/flowers/sunflower.jpg', '기다림과 존경', '큰 꽃송이와 밝은 색감으로 응원과 긍정의 분위기를 전합니다.', 'Helianthus annuus', '북아메리카', '여름', '약한 풀 향', 'EASY', '직사광선을 좋아하며 키가 커질 수 있어 넓은 공간이 좋습니다.', false, 'LOW', current_timestamp, current_timestamp),
    (3, '튤립', 'https://cdn.meanhwa.example/flowers/tulip.jpg', '고백과 배려', '단정한 실루엣과 다양한 색으로 담백한 고백에 잘 어울립니다.', 'Tulipa', '중앙아시아', '봄', '거의 없거나 은은함', 'NORMAL', '서늘한 환경을 좋아하고 과습을 피해야 합니다.', true, 'MEDIUM', current_timestamp, current_timestamp),
    (4, '라벤더', 'https://cdn.meanhwa.example/flowers/lavender.jpg', '평온과 위로', '보랏빛 꽃대와 차분한 향으로 안정감을 주는 허브성 꽃입니다.', 'Lavandula', '지중해 연안', '여름', '허브 계열의 진한 향', 'EASY', '통풍이 잘 되는 곳에서 건조하게 관리하세요.', false, 'LOW', current_timestamp, current_timestamp),
    (5, '백합', 'https://cdn.meanhwa.example/flowers/lily.jpg', '순수와 축복', '우아한 꽃형과 존재감 있는 향으로 축하 자리에서 돋보입니다.', 'Lilium', '북반구 온대 지역', '여름', '진하고 풍성한 향', 'NORMAL', '밝은 간접광에서 키우고 꽃가루는 빨리 제거하는 것이 좋습니다.', true, 'HIGH', current_timestamp, current_timestamp),
    (6, '몬스테라', 'https://cdn.meanhwa.example/plants/monstera.jpg', '성장과 여유', '갈라진 큰 잎이 공간에 시원한 인상을 주는 관엽식물입니다.', 'Monstera deliciosa', '중앙아메리카', '관엽 중심', '거의 없음', 'EASY', '밝은 간접광을 좋아하며 잎에 분무하면 생기가 유지됩니다.', true, 'HIGH', current_timestamp, current_timestamp),
    (7, '스투키', 'https://cdn.meanhwa.example/plants/sansevieria.jpg', '단단한 응원', '곧게 선 잎이 단정하고 관리 부담이 적은 실내 식물입니다.', 'Dracaena stuckyi', '아프리카', '관엽 중심', '거의 없음', 'EASY', '물을 자주 주지 않아도 되어 초보자에게 적합합니다.', true, 'LOW', current_timestamp, current_timestamp),
    (8, '안스리움', 'https://cdn.meanhwa.example/plants/anthurium.jpg', '환대와 행복', '광택 있는 포엽이 오래 유지되어 실내 포인트로 좋습니다.', 'Anthurium andraeanum', '열대 아메리카', '연중', '거의 없음', 'NORMAL', '습도를 좋아하며 강한 직사광선은 피하세요.', true, 'MEDIUM', current_timestamp, current_timestamp),
    (9, '아이비', 'https://cdn.meanhwa.example/plants/ivy.jpg', '우정과 신뢰', '덩굴성 잎이 부드럽게 늘어져 선반과 벽면 연출에 어울립니다.', 'Hedera helix', '유럽, 서아시아', '관엽 중심', '거의 없음', 'EASY', '반음지에서도 잘 자라며 흙이 마르면 충분히 물을 주세요.', true, 'LOW', current_timestamp, current_timestamp),
    (10, '카네이션', 'https://cdn.meanhwa.example/flowers/carnation.jpg', '감사와 존경', '잔잔한 프릴 꽃잎이 감사와 존중의 마음을 부드럽게 전합니다.', 'Dianthus caryophyllus', '지중해 연안', '봄~여름', '은은한 향', 'NORMAL', '서늘하고 햇빛이 드는 곳에서 오래 꽃을 볼 수 있습니다.', false, 'MEDIUM', current_timestamp, current_timestamp),
    (11, '호접란', 'https://cdn.meanhwa.example/flowers/orchid.jpg', '품격과 축하', '나비처럼 펼쳐진 꽃이 오래 지속되어 격식 있는 선물에 적합합니다.', 'Phalaenopsis', '동남아시아', '겨울~봄', '거의 없음', 'HARD', '밝은 간접광과 일정한 습도를 유지해야 합니다.', false, 'PREMIUM', current_timestamp, current_timestamp),
    (12, '프리지아', 'https://cdn.meanhwa.example/flowers/freesia.jpg', '새로운 시작', '맑은 색감과 산뜻한 향으로 시작과 응원의 메시지를 전합니다.', 'Freesia refracta', '남아프리카', '봄', '달콤하고 산뜻한 향', 'NORMAL', '서늘한 곳에서 관리하면 향과 꽃을 오래 즐길 수 있습니다.', false, 'MEDIUM', current_timestamp, current_timestamp),
    (13, '수국', 'https://cdn.meanhwa.example/flowers/hydrangea.jpg', '진심과 변함없는 마음', '풍성한 꽃송이가 부드럽고 진심 어린 분위기를 만듭니다.', 'Hydrangea macrophylla', '동아시아', '초여름~여름', '거의 없음', 'NORMAL', '물을 좋아하므로 흙이 마르지 않게 관리하고 직사광선은 피하세요.', true, 'HIGH', current_timestamp, current_timestamp),
    (14, '작약', 'https://cdn.meanhwa.example/flowers/peony.jpg', '풍요와 행복', '겹겹이 피는 큰 꽃잎이 화려하고 풍성한 인상을 줍니다.', 'Paeonia lactiflora', '동아시아', '늦봄~초여름', '은은하고 달콤한 향', 'NORMAL', '밝고 서늘한 곳에서 관리하면 큰 꽃을 오래 감상할 수 있습니다.', false, 'HIGH', current_timestamp, current_timestamp),
    (15, '거베라', 'https://cdn.meanhwa.example/flowers/gerbera.jpg', '희망과 즐거움', '선명한 원형 꽃이 밝고 경쾌한 분위기를 만드는 꽃입니다.', 'Gerbera jamesonii', '남아프리카', '봄~가을', '거의 없음', 'EASY', '햇빛을 좋아하고 물빠짐이 좋은 흙에서 잘 자랍니다.', false, 'LOW', current_timestamp, current_timestamp),
    (16, '유칼립투스', 'https://cdn.meanhwa.example/plants/eucalyptus.jpg', '치유와 기억', '은빛 잎과 청량한 향으로 차분한 공간감을 만드는 식물입니다.', 'Eucalyptus', '오스트레일리아', '관엽 중심', '청량한 허브 향', 'NORMAL', '통풍이 잘 되는 밝은 곳에 두고 과습을 피하세요.', true, 'MEDIUM', current_timestamp, current_timestamp),
    (17, '필로덴드론', 'https://cdn.meanhwa.example/plants/philodendron.jpg', '성장과 신뢰', '풍성한 잎과 안정적인 생장으로 실내에 생기를 더합니다.', 'Philodendron', '열대 아메리카', '관엽 중심', '거의 없음', 'EASY', '밝은 간접광과 적당한 습도를 유지하면 잎이 건강하게 자랍니다.', true, 'MEDIUM', current_timestamp, current_timestamp),
    (18, '금전수', 'https://cdn.meanhwa.example/plants/zz-plant.jpg', '번영과 행운', '두꺼운 잎과 강한 생명력으로 개업과 집들이 선물에 자주 쓰입니다.', 'Zamioculcas zamiifolia', '동아프리카', '관엽 중심', '거의 없음', 'EASY', '건조에 강해 물을 자주 주지 않아도 되며 반음지에서도 잘 자랍니다.', true, 'LOW', current_timestamp, current_timestamp),
    (19, '칼라디움', 'https://cdn.meanhwa.example/plants/caladium.jpg', '기쁨과 섬세함', '화려한 잎 무늬가 공간에 생동감과 장식성을 더합니다.', 'Caladium bicolor', '남아메리카', '관엽 중심', '거의 없음', 'HARD', '높은 습도와 따뜻한 환경을 좋아하며 찬바람을 피해야 합니다.', true, 'MEDIUM', current_timestamp, current_timestamp),
    (20, '로즈마리', 'https://cdn.meanhwa.example/plants/rosemary.jpg', '기억과 응원', '가느다란 잎과 향긋한 허브 향이 기억과 응원의 상징으로 쓰입니다.', 'Salvia rosmarinus', '지중해 연안', '봄~여름', '상쾌한 허브 향', 'EASY', '햇빛과 통풍을 좋아하고 흙이 충분히 마른 뒤 물을 주세요.', false, 'LOW', current_timestamp, current_timestamp);

-- tags.code: 위저드 선택 code ↔ DB 태그 (P2). null은 사전·레거시 전용 태그.
insert into tags (id, category, name, code)
values
    (1, 'EVENT', '생일', 'BIRTHDAY'),
    (2, 'EVENT', '졸업', 'GRADUATION'),
    (3, 'EVENT', '집들이', 'HOUSEWARMING'),
    (4, 'EVENT', '승진', 'PROMOTION'),
    (5, 'RELATION', '연인', 'LOVER'),
    (6, 'RELATION', '친구', 'FRIEND'),
    (7, 'RELATION', '부모님', 'PARENT'),
    (8, 'RELATION', '동료', 'COLLEAGUE'),
    (9, 'EMOTION', '사랑', 'LOVE'),
    (10, 'EMOTION', '감사', 'GRATITUDE'),
    (11, 'EMOTION', '위로', 'COMFORT'),
    (12, 'EMOTION', '응원', 'SUPPORT'),
    (13, 'STYLE', '화사한', null),
    (14, 'STYLE', '차분한', null),
    (15, 'CARE', '초보자', null),
    (16, 'CARE', '반려동물 안전', null),
    (17, 'SEASON', '봄', null),
    (18, 'SEASON', '여름', null),
    (19, 'SEASON', '가을', null),
    (20, 'SEASON', '겨울', null),
    (21, 'ENVIRONMENT', '실내', null),
    (22, 'ENVIRONMENT', '실외', 'BALCONY_OUTDOOR'),
    (23, 'ENVIRONMENT', '책상', 'DESK_SMALL'),
    (24, 'ENVIRONMENT', '거실', 'LIVING_ROOM'),
    (25, 'EVENT', '결혼', 'WEDDING'),
    (26, 'EVENT', '병문안·회복', 'RECOVERY'),
    (27, 'RELATION', '가족', 'FAMILY'),
    (28, 'RELATION', '선후배·스승', 'SENIOR_JUNIOR_MENTOR'),
    (29, 'RELATION', '동료·후배', 'COLLEAGUE_JUNIOR'),
    (30, 'RELATION', '상사·선배', 'BOSS_SENIOR'),
    (31, 'RELATION', '친구·지인', 'FRIEND_ACQUAINTANCE'),
    (32, 'RELATION', '은사·귀빈', 'VIP_MENTOR'),
    (33, 'EMOTION', '축하', 'CELEBRATION'),
    (34, 'EMOTION', '격려', 'ENCOURAGEMENT'),
    (35, 'EMOTION', '도약', 'LEAP'),
    (36, 'EMOTION', '존경', 'RESPECT'),
    (37, 'EMOTION', '자부심', 'PRIDE'),
    (38, 'EMOTION', '축복', 'BLESSING'),
    (39, 'EMOTION', '진심', 'SINCERITY'),
    (40, 'EMOTION', '영원', 'ETERNITY'),
    (41, 'EMOTION', '평온', 'PEACE'),
    (42, 'EMOTION', '번창', 'PROSPERITY'),
    (43, 'EMOTION', '쾌유', 'GET_WELL'),
    (44, 'ENVIRONMENT', '창가·밝은 실내', 'WINDOW_BRIGHT'),
    (45, 'MEANING', '변함없는 마음', 'LOVE_1'),
    (46, 'MEANING', '첫사랑의 설렘', 'LOVE_2'),
    (47, 'MEANING', '소중한 당신', 'LOVE_3'),
    (48, 'MEANING', '진실한 사랑', 'LOVE_4'),
    (49, 'MEANING', '언제나 응원해', 'SUPPORT_1'),
    (50, 'MEANING', '변치 않는 우정', 'SUPPORT_2'),
    (51, 'MEANING', '찬란한 미소', 'SUPPORT_3'),
    (52, 'MEANING', '매일의 행복', 'SUPPORT_4'),
    (53, 'MEANING', '용기와 자신감', 'ENCOURAGEMENT_1'),
    (54, 'MEANING', '새로운 도전', 'ENCOURAGEMENT_2'),
    (55, 'MEANING', '당당한 발걸음', 'ENCOURAGEMENT_3'),
    (56, 'MEANING', '무한한 가능성', 'ENCOURAGEMENT_4'),
    (57, 'MEANING', '화사한 축하', 'CELEBRATION_1'),
    (58, 'MEANING', '빛나는 성취', 'CELEBRATION_2'),
    (59, 'MEANING', '새로운 시작', 'CELEBRATION_3'),
    (60, 'MEANING', '함께한 기쁨', 'CELEBRATION_4'),
    (61, 'MEANING', '진심 어린 고마움', 'GRATITUDE_1'),
    (62, 'MEANING', '함께해서 행복', 'GRATITUDE_2'),
    (63, 'MEANING', '오래된 인연', 'GRATITUDE_3'),
    (64, 'MEANING', '따뜻한 기억', 'GRATITUDE_4'),
    (65, 'MEANING', '행복한 시작', 'BLESSING_1'),
    (66, 'MEANING', '아름다운 인연', 'BLESSING_2'),
    (67, 'MEANING', '아낌없는 축복', 'BLESSING_3'),
    (68, 'MEANING', '조화와 화합', 'BLESSING_4'),
    (69, 'MEANING', '진심을 담아', 'SINCERITY_1'),
    (70, 'MEANING', '소중한 인연', 'SINCERITY_2'),
    (71, 'MEANING', '영원한 약속', 'SINCERITY_3'),
    (72, 'MEANING', '고귀한 사랑', 'SINCERITY_4'),
    (73, 'MEANING', '영원히 하나됨', 'ETERNITY_1'),
    (74, 'MEANING', '영원한 사랑', 'ETERNITY_2'),
    (75, 'MEANING', '아름다운 시작', 'ETERNITY_3'),
    (76, 'MEANING', '고귀한 인연', 'ETERNITY_4'),
    (77, 'MEANING', '깊은 존경', 'RESPECT_1'),
    (78, 'MEANING', '명예와 인정', 'RESPECT_2'),
    (79, 'MEANING', '굳건한 신뢰', 'RESPECT_3'),
    (80, 'MEANING', '탄탄대로', 'RESPECT_4'),
    (81, 'MEANING', '값진 노력', 'PRIDE_1'),
    (82, 'MEANING', '빛나는 성공', 'PRIDE_2'),
    (83, 'MEANING', '끊임없는 성장', 'PRIDE_3'),
    (84, 'MEANING', '위풍당당', 'PRIDE_4'),
    (85, 'MEANING', '희망과 도약', 'LEAP_1'),
    (86, 'MEANING', '밝은 앞날', 'LEAP_2'),
    (87, 'MEANING', '무한한 가능성', 'LEAP_3'),
    (88, 'MEANING', '꿈을 향해', 'LEAP_4'),
    (89, 'MEANING', '평온한 일상', 'PEACE_1'),
    (90, 'MEANING', '편안한 공간', 'PEACE_2'),
    (91, 'MEANING', '마음의 안정', 'PEACE_3'),
    (92, 'MEANING', '따뜻한 온기', 'PEACE_4'),
    (93, 'MEANING', '풍요와 번영', 'PROSPERITY_1'),
    (94, 'MEANING', '피어나는 기쁨', 'PROSPERITY_2'),
    (95, 'MEANING', '번창하는 일상', 'PROSPERITY_3'),
    (96, 'MEANING', '가정의 행복', 'PROSPERITY_4'),
    (97, 'MEANING', '따뜻한 위안', 'COMFORT_1'),
    (98, 'MEANING', '깊은 배려', 'COMFORT_2'),
    (99, 'MEANING', '평온한 휴식', 'COMFORT_3'),
    (100, 'MEANING', '마음의 안계', 'COMFORT_4'),
    (101, 'MEANING', '빠른 회복', 'GET_WELL_1'),
    (102, 'MEANING', '다시 찾은 활력', 'GET_WELL_2'),
    (103, 'MEANING', '건강한 내일', 'GET_WELL_3'),
    (104, 'MEANING', '희망의 빛', 'GET_WELL_4');

insert into flower_tag_mappings (id, flower_id, tag_id, weight)
values
    (1, 1, 5, 5), (2, 1, 9, 5), (3, 1, 1, 3), (4, 1, 13, 3),
    (5, 2, 2, 4), (6, 2, 8, 3), (7, 2, 12, 5), (8, 2, 15, 4), (9, 2, 16, 2),
    (10, 3, 5, 4), (11, 3, 9, 4), (12, 3, 13, 4), (13, 3, 1, 3),
    (14, 4, 11, 5), (15, 4, 14, 5), (16, 4, 15, 4), (17, 4, 16, 5),
    (18, 5, 4, 3), (19, 5, 7, 4), (20, 5, 10, 4), (21, 5, 14, 3),
    (22, 6, 3, 5), (23, 6, 4, 3), (24, 6, 8, 3), (25, 6, 13, 2),
    (26, 7, 3, 4), (27, 7, 8, 3), (28, 7, 12, 4), (29, 7, 15, 5),
    (30, 8, 1, 3), (31, 8, 5, 3), (32, 8, 9, 3), (33, 8, 13, 4),
    (34, 9, 6, 5), (35, 9, 10, 3), (36, 9, 15, 4), (37, 9, 14, 3),
    (38, 10, 7, 5), (39, 10, 10, 5), (40, 10, 1, 3), (41, 10, 16, 4),
    (42, 11, 4, 5), (43, 11, 8, 4), (44, 11, 10, 4), (45, 11, 14, 4), (46, 11, 16, 3),
    (47, 12, 2, 5), (48, 12, 6, 4), (49, 12, 12, 4), (50, 12, 13, 3), (51, 12, 16, 3),
    (52, 13, 1, 3), (53, 13, 5, 3), (54, 13, 10, 4), (55, 13, 14, 5),
    (56, 14, 4, 4), (57, 14, 7, 3), (58, 14, 10, 4), (59, 14, 13, 3), (60, 14, 16, 2),
    (61, 15, 1, 4), (62, 15, 6, 3), (63, 15, 12, 4), (64, 15, 13, 5), (65, 15, 16, 4),
    (66, 16, 3, 3), (67, 16, 11, 5), (68, 16, 14, 4), (69, 16, 15, 3),
    (70, 17, 3, 4), (71, 17, 8, 3), (72, 17, 12, 3), (73, 17, 15, 4),
    (74, 18, 3, 4), (75, 18, 4, 4), (76, 18, 8, 3), (77, 18, 15, 5),
    (78, 19, 1, 3), (79, 19, 5, 3), (80, 19, 13, 5), (81, 19, 14, 2),
    (82, 20, 6, 4), (83, 20, 11, 3), (84, 20, 12, 5), (85, 20, 15, 5), (86, 20, 16, 4),
    (87, 1, 17, 5), (88, 1, 21, 3), (89, 1, 23, 2),
    (90, 2, 18, 5), (91, 2, 22, 5), (92, 2, 24, 3),
    (93, 3, 17, 5), (94, 3, 21, 4), (95, 3, 23, 4),
    (96, 4, 18, 4), (97, 4, 21, 5), (98, 4, 23, 5),
    (99, 5, 19, 4), (100, 5, 21, 3), (101, 5, 24, 4),
    (102, 6, 18, 4), (103, 6, 21, 5), (104, 6, 24, 5),
    (105, 7, 20, 4), (106, 7, 21, 5), (107, 7, 23, 5),
    (108, 8, 18, 4), (109, 8, 21, 4), (110, 8, 24, 4),
    (111, 9, 19, 4), (112, 9, 21, 5), (113, 9, 23, 4),
    (114, 10, 17, 5), (115, 10, 21, 4), (116, 10, 24, 3),
    (117, 11, 20, 5), (118, 11, 21, 5), (119, 11, 24, 5),
    (120, 12, 17, 5), (121, 12, 21, 4), (122, 12, 23, 3),
    (123, 13, 18, 5), (124, 13, 21, 4), (125, 13, 24, 5),
    (126, 14, 17, 5), (127, 14, 21, 3), (128, 14, 24, 4),
    (129, 15, 18, 5), (130, 15, 22, 4), (131, 15, 24, 3),
    (132, 16, 19, 5), (133, 16, 21, 4), (134, 16, 23, 3),
    (135, 17, 18, 4), (136, 17, 21, 5), (137, 17, 24, 5),
    (138, 18, 20, 5), (139, 18, 21, 5), (140, 18, 23, 4),
    (141, 19, 18, 5), (142, 19, 21, 4), (143, 19, 24, 4),
    (144, 20, 19, 5), (145, 20, 22, 4), (146, 20, 23, 3);

-- 위저드 신규 태그: 기존 유사 태그 매핑을 복제해 초기 추천 점수를 채운다.
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 25, weight from flower_tag_mappings where tag_id = 1;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 26, weight from flower_tag_mappings where tag_id = 11;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 27, weight from flower_tag_mappings where tag_id = 7;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 28, weight from flower_tag_mappings where tag_id = 8;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 29, weight from flower_tag_mappings where tag_id = 8;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 30, weight from flower_tag_mappings where tag_id = 8;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 31, weight from flower_tag_mappings where tag_id = 6;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 32, weight from flower_tag_mappings where tag_id = 7;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 33, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 34, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 35, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 36, weight from flower_tag_mappings where tag_id = 10;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 37, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 38, weight from flower_tag_mappings where tag_id = 9;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 39, weight from flower_tag_mappings where tag_id = 9;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 40, weight from flower_tag_mappings where tag_id = 9;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 41, weight from flower_tag_mappings where tag_id = 11;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 42, weight from flower_tag_mappings where tag_id = 10;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 43, weight from flower_tag_mappings where tag_id = 11;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 44, weight from flower_tag_mappings where tag_id = 21;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 45, weight from flower_tag_mappings where tag_id = 9;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 46, weight from flower_tag_mappings where tag_id = 9;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 47, weight from flower_tag_mappings where tag_id = 9;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 48, weight from flower_tag_mappings where tag_id = 9;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 49, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 50, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 51, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 52, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 53, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 54, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 55, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 56, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 57, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 58, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 59, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 60, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 61, weight from flower_tag_mappings where tag_id = 10;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 62, weight from flower_tag_mappings where tag_id = 10;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 63, weight from flower_tag_mappings where tag_id = 10;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 64, weight from flower_tag_mappings where tag_id = 10;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 65, weight from flower_tag_mappings where tag_id = 9;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 66, weight from flower_tag_mappings where tag_id = 9;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 67, weight from flower_tag_mappings where tag_id = 9;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 68, weight from flower_tag_mappings where tag_id = 9;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 69, weight from flower_tag_mappings where tag_id = 9;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 70, weight from flower_tag_mappings where tag_id = 9;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 71, weight from flower_tag_mappings where tag_id = 9;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 72, weight from flower_tag_mappings where tag_id = 9;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 73, weight from flower_tag_mappings where tag_id = 9;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 74, weight from flower_tag_mappings where tag_id = 9;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 75, weight from flower_tag_mappings where tag_id = 9;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 76, weight from flower_tag_mappings where tag_id = 9;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 77, weight from flower_tag_mappings where tag_id = 10;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 78, weight from flower_tag_mappings where tag_id = 10;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 79, weight from flower_tag_mappings where tag_id = 10;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 80, weight from flower_tag_mappings where tag_id = 10;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 81, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 82, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 83, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 84, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 85, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 86, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 87, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 88, weight from flower_tag_mappings where tag_id = 12;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 89, weight from flower_tag_mappings where tag_id = 11;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 90, weight from flower_tag_mappings where tag_id = 11;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 91, weight from flower_tag_mappings where tag_id = 11;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 92, weight from flower_tag_mappings where tag_id = 11;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 93, weight from flower_tag_mappings where tag_id = 10;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 94, weight from flower_tag_mappings where tag_id = 10;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 95, weight from flower_tag_mappings where tag_id = 10;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 96, weight from flower_tag_mappings where tag_id = 10;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 97, weight from flower_tag_mappings where tag_id = 11;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 98, weight from flower_tag_mappings where tag_id = 11;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 99, weight from flower_tag_mappings where tag_id = 11;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 100, weight from flower_tag_mappings where tag_id = 11;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 101, weight from flower_tag_mappings where tag_id = 11;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 102, weight from flower_tag_mappings where tag_id = 11;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 103, weight from flower_tag_mappings where tag_id = 11;
insert into flower_tag_mappings (flower_id, tag_id, weight) select flower_id, 104, weight from flower_tag_mappings where tag_id = 11;
