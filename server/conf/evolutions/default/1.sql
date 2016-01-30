# --- !Ups

CREATE TABLE "library" (
  "uuid" UUID PRIMARY KEY
  , "name" VARCHAR NOT NULL UNIQUE
);

CREATE TABLE "visitor" (
  "id" INTEGER NOT NULL,
  "library_uuid" UUID NOT NULL REFERENCES "library"("uuid"),
  PRIMARY KEY ("id", "library_uuid")
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

DROP TABLE "library" IF EXISTS;

DROP TABLE "visitor" IF EXISTS;

DROP TABLE "user" IF EXISTS;

DROP TABLE "new_visitor" IF EXISTS;
