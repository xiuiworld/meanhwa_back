-- Correct COMFORT_4 display label typo without modifying already-applied V2.
UPDATE tags
SET name = '마음의 안식'
WHERE code = 'COMFORT_4'
  AND deleted_at IS NULL
  AND name = '마음의 안계';
