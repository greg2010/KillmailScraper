BEGIN;

  ALTER TABLE corporation ALTER COLUMN alliance_id DROP NOT NULL;

COMMIT;