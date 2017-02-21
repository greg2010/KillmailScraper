BEGIN;
  ALTER TABLE killmail ALTER COLUMN kill_id TYPE BIGINT;
  ALTER TABLE killmail ALTER COLUMN ship_id TYPE BIGINT;
  ALTER TABLE killmail ALTER COLUMN character_id TYPE BIGINT;
  ALTER TABLE killmail ALTER COLUMN solarsystem_id TYPE BIGINT;
  ALTER TABLE killmail ALTER COLUMN attacker_count TYPE BIGINT;
  ALTER TABLE killmail ALTER COLUMN final_blow TYPE BIGINT;

  ALTER TABLE attackers ALTER COLUMN kill_id TYPE BIGINT;
  ALTER TABLE attackers ALTER COLUMN ship_id TYPE BIGINT;
  ALTER TABLE attackers ALTER COLUMN character_id TYPE BIGINT;
  ALTER TABLE attackers ALTER COLUMN weapontype_id TYPE BIGINT;

  ALTER TABLE character ALTER COLUMN character_id TYPE BIGINT;
  ALTER TABLE character ALTER COLUMN corporation_id TYPE BIGINT;

  ALTER TABLE corporation ALTER COLUMN alliance_id TYPE BIGINT;
  ALTER TABLE corporation ALTER COLUMN corporation_id TYPE BIGINT;

  ALTER TABLE item_type ALTER COLUMN kill_id TYPE BIGINT;
  ALTER TABLE item_type ALTER COLUMN item_id TYPE BIGINT;
  ALTER TABLE item_type ALTER COLUMN quantity_dropped TYPE BIGINT;
  ALTER TABLE item_type ALTER COLUMN quantity_destroyed TYPE BIGINT;


  ALTER TABLE zkb_metadata ALTER COLUMN kill_id TYPE BIGINT;
  ALTER TABLE zkb_metadata ALTER COLUMN location_id TYPE BIGINT;
  ALTER TABLE zkb_metadata ALTER COLUMN points TYPE BIGINT;
COMMIT;