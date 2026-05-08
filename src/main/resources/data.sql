insert into flowers (id, name, image_url, core_meaning, management_level, management_info, is_toxic_to_pets, price_range, created_at, updated_at)
values
    (1, '장미', 'https://cdn.meanhwa.example/flowers/rose.jpg', '사랑과 열정', 'NORMAL', '햇빛이 잘 드는 곳에 두고 겉흙이 마르면 물을 주세요.', false, 'MEDIUM', current_timestamp, current_timestamp),
    (2, '해바라기', 'https://cdn.meanhwa.example/flowers/sunflower.jpg', '기다림과 존경', 'EASY', '직사광선을 좋아하며 키가 커질 수 있어 넓은 공간이 좋습니다.', false, 'LOW', current_timestamp, current_timestamp),
    (3, '튤립', 'https://cdn.meanhwa.example/flowers/tulip.jpg', '고백과 배려', 'NORMAL', '서늘한 환경을 좋아하고 과습을 피해야 합니다.', true, 'MEDIUM', current_timestamp, current_timestamp),
    (4, '라벤더', 'https://cdn.meanhwa.example/flowers/lavender.jpg', '평온과 위로', 'EASY', '통풍이 잘 되는 곳에서 건조하게 관리하세요.', false, 'LOW', current_timestamp, current_timestamp),
    (5, '백합', 'https://cdn.meanhwa.example/flowers/lily.jpg', '순수와 축복', 'NORMAL', '밝은 간접광에서 키우고 꽃가루는 빨리 제거하는 것이 좋습니다.', true, 'HIGH', current_timestamp, current_timestamp),
    (6, '몬스테라', 'https://cdn.meanhwa.example/plants/monstera.jpg', '성장과 여유', 'EASY', '밝은 간접광을 좋아하며 잎에 분무하면 생기가 유지됩니다.', true, 'HIGH', current_timestamp, current_timestamp),
    (7, '스투키', 'https://cdn.meanhwa.example/plants/sansevieria.jpg', '단단한 응원', 'EASY', '물을 자주 주지 않아도 되어 초보자에게 적합합니다.', true, 'LOW', current_timestamp, current_timestamp),
    (8, '안스리움', 'https://cdn.meanhwa.example/plants/anthurium.jpg', '환대와 행복', 'NORMAL', '습도를 좋아하며 강한 직사광선은 피하세요.', true, 'MEDIUM', current_timestamp, current_timestamp),
    (9, '아이비', 'https://cdn.meanhwa.example/plants/ivy.jpg', '우정과 신뢰', 'EASY', '반음지에서도 잘 자라며 흙이 마르면 충분히 물을 주세요.', true, 'LOW', current_timestamp, current_timestamp),
    (10, '카네이션', 'https://cdn.meanhwa.example/flowers/carnation.jpg', '감사와 존경', 'NORMAL', '서늘하고 햇빛이 드는 곳에서 오래 꽃을 볼 수 있습니다.', false, 'MEDIUM', current_timestamp, current_timestamp),
    (11, '호접란', 'https://cdn.meanhwa.example/flowers/orchid.jpg', '품격과 축하', 'HARD', '밝은 간접광과 일정한 습도를 유지해야 합니다.', false, 'PREMIUM', current_timestamp, current_timestamp),
    (12, '프리지아', 'https://cdn.meanhwa.example/flowers/freesia.jpg', '새로운 시작', 'NORMAL', '서늘한 곳에서 관리하면 향과 꽃을 오래 즐길 수 있습니다.', false, 'MEDIUM', current_timestamp, current_timestamp);

insert into tags (id, category, name)
values
    (1, 'EVENT', '생일'),
    (2, 'EVENT', '졸업'),
    (3, 'EVENT', '집들이'),
    (4, 'EVENT', '승진'),
    (5, 'RELATION', '연인'),
    (6, 'RELATION', '친구'),
    (7, 'RELATION', '부모님'),
    (8, 'RELATION', '동료'),
    (9, 'EMOTION', '사랑'),
    (10, 'EMOTION', '감사'),
    (11, 'EMOTION', '위로'),
    (12, 'EMOTION', '응원'),
    (13, 'STYLE', '화사한'),
    (14, 'STYLE', '차분한'),
    (15, 'CARE', '초보자'),
    (16, 'CARE', '반려동물 안전');

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
    (47, 12, 2, 5), (48, 12, 6, 4), (49, 12, 12, 4), (50, 12, 13, 3), (51, 12, 16, 3);
