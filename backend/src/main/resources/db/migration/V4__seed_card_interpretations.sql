INSERT INTO card_interpretations
  (card_id, orientation, position_code, keywords, interpretation, document_version, is_active, created_at)
SELECT
  card_docs.card_id,
  'UPRIGHT',
  positions.position_code,
  JSON_ARRAY(card_docs.keyword_one, card_docs.keyword_two, positions.keyword),
  CONCAT(
    card_docs.name_ko,
    ' 카드는 ',
    card_docs.base_meaning,
    ' ',
    positions.guide_text
  ),
  'tarot-doc-v1.0',
  TRUE,
  NOW()
FROM (
  SELECT 1 card_id, '광대' name_ko, '시작' keyword_one, '가능성' keyword_two, '새로운 출발과 열린 가능성을 보여줍니다.' base_meaning
  UNION ALL SELECT 2, '마법사', '의지', '실행력', '가진 자원과 의지를 구체적인 행동으로 연결하는 힘을 보여줍니다.'
  UNION ALL SELECT 3, '여사제', '직감', '내면', '겉으로 드러나지 않은 감정과 직감을 살펴야 함을 보여줍니다.'
  UNION ALL SELECT 4, '여황제', '풍요', '돌봄', '관계와 상황이 자라날 수 있는 따뜻한 기반을 보여줍니다.'
  UNION ALL SELECT 5, '황제', '질서', '책임', '기준과 책임을 세워 흔들리는 상황을 안정시키는 힘을 보여줍니다.'
  UNION ALL SELECT 6, '교황', '가치관', '조언', '신뢰할 수 있는 기준과 조언을 참고해야 하는 흐름을 보여줍니다.'
  UNION ALL SELECT 7, '연인', '선택', '관계', '마음의 끌림과 선택 이후의 책임을 함께 살펴야 함을 보여줍니다.'
  UNION ALL SELECT 8, '전차', '추진', '방향', '목표를 향해 나아가려는 강한 추진력과 집중을 보여줍니다.'
  UNION ALL SELECT 9, '힘', '인내', '용기', '강하게 맞서기보다 부드럽게 다루는 용기와 자기조절을 보여줍니다.'
  UNION ALL SELECT 10, '은둔자', '성찰', '탐색', '잠시 거리를 두고 내면의 답을 찾는 시간이 필요함을 보여줍니다.'
  UNION ALL SELECT 11, '운명의 수레바퀴', '전환', '흐름', '상황이 고정되어 있지 않고 변화의 흐름 안에 있음을 보여줍니다.'
  UNION ALL SELECT 12, '정의', '공정함', '균형', '감정과 사실을 나누어 공정하게 판단해야 함을 보여줍니다.'
  UNION ALL SELECT 13, '매달린 사람', '멈춤', '관점 전환', '억지로 밀기보다 다른 관점에서 상황을 바라볼 필요를 보여줍니다.'
  UNION ALL SELECT 14, '죽음', '종료', '변화', '오래된 방식이 끝나고 새로운 단계로 넘어가는 전환을 보여줍니다.'
  UNION ALL SELECT 15, '절제', '조화', '회복', '극단을 피하고 서로 다른 요소를 조율하는 회복의 흐름을 보여줍니다.'
  UNION ALL SELECT 16, '악마', '집착', '묶임', '욕구나 습관, 관계에 과하게 묶여 있는 부분을 보여줍니다.'
  UNION ALL SELECT 17, '탑', '충격', '드러남', '숨겨졌던 문제가 드러나며 기존 구조를 다시 세워야 함을 보여줍니다.'
  UNION ALL SELECT 18, '별', '희망', '치유', '긴 호흡으로 회복하고 희망을 현실에 연결하는 흐름을 보여줍니다.'
  UNION ALL SELECT 19, '달', '불안', '혼란', '추측과 두려움이 커질 수 있어 사실 확인이 필요함을 보여줍니다.'
  UNION ALL SELECT 20, '태양', '명확함', '활력', '밝게 드러나는 흐름과 솔직한 표현의 힘을 보여줍니다.'
  UNION ALL SELECT 21, '심판', '각성', '결산', '과거를 돌아보고 다음 단계로 넘어갈 결단을 보여줍니다.'
  UNION ALL SELECT 22, '세계', '완성', '통합', '경험이 하나로 모이며 마무리와 확장을 준비하는 흐름을 보여줍니다.'
) card_docs
JOIN (
  SELECT 'PRESENT' position_code, '현재' keyword, '현재 위치에서는 지금 고민의 중심 흐름을 설명하는 근거로 사용합니다.' guide_text
  UNION ALL SELECT 'OBSTACLE', '장애물', '장애물 위치에서는 반복되는 어려움이나 숨겨진 원인을 점검하는 근거로 사용합니다.'
  UNION ALL SELECT 'ADVICE', '조언', '조언 위치에서는 사용자가 현실적으로 취할 수 있는 다음 행동을 제안하는 근거로 사용합니다.'
) positions;
