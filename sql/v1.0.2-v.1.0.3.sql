BEGIN;

  ALTER TABLE killmail ADD COLUMN added_at TIMESTAMP NOT NULL;
  ALTER TABLE character ADD COLUMN last_updated TIMESTAMP NOT NULL;

COMMIT;