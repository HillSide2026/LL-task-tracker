-- Rewrite legacy Step 1 admin lifecycle state values after the canonical rename from Open -> Opened.
-- Run after the admin lifecycle columns exist and before Step 2A maintenance control is enabled.

UPDATE case_instance
SET admin_state = 'Opened'
WHERE admin_state = 'Open';

UPDATE case_instance
SET resume_to_state = 'Opened'
WHERE resume_to_state = 'Open';
