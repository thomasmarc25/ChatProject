-- Run only in an EMPTY DISPOSABLE database; minimal account fixture is NOT the production account schema.
CREATE TABLE app_users(id BIGINT PRIMARY KEY);
INSERT INTO app_users VALUES (1),(2),(3),(4);
-- Execute 01_person1.sql here, then execute the remaining statements in this file.
