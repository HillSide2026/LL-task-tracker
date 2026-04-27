-- Realign legacy Opened admin lifecycle rows after Opening was defined to
-- complete at Opened instead of Active.

UPDATE case_instance
SET stage = 'Opening'
WHERE admin_state = 'Opened'
  AND stage IN ('Maintenance', 'Open');
