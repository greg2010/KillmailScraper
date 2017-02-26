BEGIN;

ALTER TABLE killmail ALTER COLUMN position_x DROP NOT NULL;
ALTER TABLE killmail ALTER COLUMN position_y DROP NOT NULL;
ALTER TABLE killmail ALTER COLUMN position_z DROP NOT NULL;


ALTER TABLE character ALTER COLUMN corporation_id DROP NOT NULL;
ALTER TABLE character DROP COLUMN name;


ALTER TABLE corporation DROP COLUMN name;

ALTER TABLE zkb_metadata DROP COLUMN location_id;

COMMIT;