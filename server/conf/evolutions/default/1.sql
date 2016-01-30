# --- !Ups

CREATE TABLE "visitor" (
  "id" SERIAL PRIMARY KEY
);

CREATE TABLE "user" (
  "id" SERIAL PRIMARY KEY,
  "name" VARCHAR NOT NULL UNIQUE,
  "password" VARCHAR NOT NULL,
  "visitor_id" INTEGER UNIQUE REFERENCES "visitor"("id")
);

CREATE TABLE "new_visitor" (
  "id" INTEGER NOT NULL UNIQUE REFERENCES "user"
);


# --- !Downs

DROP TABLE "user" IF EXISTS;

DROP TABLE "visitor" IF EXISTS;

DROP TABLE "new_visitor" IF EXISTS;
