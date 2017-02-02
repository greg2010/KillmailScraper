BEGIN;

  CREATE TABLE killmail(
    kill_id INT NOT NULL,
    ship_id INT NOT NULL,
    character_id INT,
    solarSystem_id INT NOT NULL,
    kill_time TIMESTAMP NOT NULL,
    attacker_count INT NOT NULL,
    final_blow INT,
    position_x DOUBLE PRECISION NOT NULL,
    position_y DOUBLE PRECISION NOT NULL,
    position_z DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (kill_id)
  );


  CREATE TABLE attackers(
    kill_id INT NOT NULL,
    ship_id INT NOT NULL,
    character_id INT,
    weaponType_id INT NOT NULL,
    damage_done BIGINT NOT NULL,
    security_status DOUBLE PRECISION NOT NULL,
    FOREIGN KEY (kill_id) REFERENCES killmail(kill_id)
  );


  CREATE TABLE item_type(
    kill_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity_dropped INT NOT NULL,
    quantity_destroyed INT NOT NULL,
    FOREIGN KEY (kill_id) REFERENCES killmail(kill_id)
  );


  CREATE TABLE character(
    character_id INT NOT NULL,
    corporation_id INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (character_id)
  );


  CREATE TABLE corporation(
    corporation_id INT NOT NULL,
    alliance_id INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (corporation_id)
  );


  CREATE TABLE zkb_metadata(
    kill_id INT NOT NULL,
    location_id INT NOT NULL,
    hash VARCHAR(50) NOT NULL,
    total_value DOUBLE PRECISION NOT NULL,
    points INT NOT NULL,
    PRIMARY KEY (kill_id),
    FOREIGN KEY (kill_id) REFERENCES killmail(kill_id)
  );

COMMIT;