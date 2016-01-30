# --- !Ups

CREATE TABLE "user" (
  "id" SERIAL PRIMARY KEY,
  "name" VARCHAR NOT NULL UNIQUE,
  "password" VARCHAR NOT NULL
);

# --- !Downs

DROP TABLE "user" IF EXISTS;
