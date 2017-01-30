# --- !Ups

CREATE TABLE "visitor" (
  "id"          SERIAL    PRIMARY KEY,
  "first_name"  VARCHAR   NOT NULL,
  "last_name"   VARCHAR   NOT NULL,
  "middle_name" VARCHAR,
  "extra_name"  VARCHAR,
  "created"     TIMESTAMP NOT NULL    DEFAULT CURRENT_TIMESTAMP
);

# --- !Downs

DROP TABLE "visitor" IF EXISTS;
