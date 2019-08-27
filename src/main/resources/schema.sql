DROP TABLE IF EXISTS person;
DROP TABLE IF EXISTS email;
DROP TABLE IF EXISTS age;

CREATE TABLE person  (
    person_id BIGINT auto_increment NOT NULL PRIMARY KEY,
    first_name VARCHAR(40),
    last_name VARCHAR(40),
    email VARCHAR(100),
    age INT
);
CREATE TABLE email  (
    person_id BIGINT auto_increment NOT NULL PRIMARY KEY,
    email VARCHAR(100)
);
CREATE TABLE age  (
    person_id BIGINT auto_increment NOT NULL PRIMARY KEY,
    age INT
);