# --- !Ups

CREATE TABLE "users" (
  "id" SERIAL PRIMARY KEY,
  "name" VARCHAR NOT NULL UNIQUE,
  "password" VARCHAR NOT NULL
);

# --- !Downs

DROP TABLE "users" IF EXISTS;
