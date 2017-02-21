BEGIN;

  CREATE TABLE killmail(
    kill_id BIGINT NOT NULL,
    ship_id BIGINT NOT NULL,
    character_id BIGINT,
    solarSystem_id BIGINT NOT NULL,
    kill_time TIMESTAMP NOT NULL,
    attacker_count BIGINT NOT NULL,
    final_blow BIGINT,
    position_x DOUBLE PRECISION NOT NULL,
    position_y DOUBLE PRECISION NOT NULL,
    position_z DOUBLE PRECISION NOT NULL,
    added_at TIMESTAMP NOT NULL,
    PRIMARY KEY (kill_id)
  );


  CREATE TABLE attackers(
    kill_id BIGINT NOT NULL,
    ship_id BIGINT,
    character_id BIGINT,
    weaponType_id BIGINT,
    damage_done BIGINT NOT NULL,
    security_status DOUBLE PRECISION NOT NULL,
    FOREIGN KEY (kill_id) REFERENCES killmail(kill_id)
  );


  CREATE TABLE item_type(
    kill_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity_dropped BIGINT NOT NULL,
    quantity_destroyed BIGINT NOT NULL,
    FOREIGN KEY (kill_id) REFERENCES killmail(kill_id)
  );


  CREATE TABLE character(
    character_id BIGINT NOT NULL,
    corporation_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    last_updated TIMESTAMP NOT NULL,
    PRIMARY KEY (character_id)
  );


  CREATE TABLE corporation(
    corporation_id BIGINT NOT NULL,
    alliance_id BIGINT,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (corporation_id)
  );


  CREATE TABLE zkb_metadata(
    kill_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    hash VARCHAR(50) NOT NULL,
    total_value DOUBLE PRECISION NOT NULL,
    points BIGINT NOT NULL,
    PRIMARY KEY (kill_id),
    FOREIGN KEY (kill_id) REFERENCES killmail(kill_id)
  );

COMMIT;