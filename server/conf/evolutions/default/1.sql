# --- !Ups

CREATE TABLE "library" (
  "uuid" UUID PRIMARY KEY
  , "name" VARCHAR NOT NULL UNIQUE
);

CREATE TABLE "visitor" (
  "uuid" UUID PRIMARY KEY
  , "number" SERIAL
  , "library_uuid" UUID NOT NULL REFERENCES "library"("uuid")
  , "first_name" VARCHAR NOT NULL
  , "last_name" VARCHAR NOT NULL
  , "middle_name" VARCHAR
  , "extra_name" VARCHAR
  , UNIQUE ("number", "library_uuid")
);

CREATE TABLE "user" (
  "uuid" UUID PRIMARY KEY
  , "name" VARCHAR NOT NULL
  , "password" VARCHAR NOT NULL
  , "visitor_uuid" UUID UNIQUE REFERENCES "visitor"("uuid")
);

CREATE TABLE "new_visitor" (
  "id" INTEGER NOT NULL UNIQUE REFERENCES "user"
);


# --- !Downs

DROP TABLE "library" IF EXISTS;

DROP TABLE "visitor" IF EXISTS;

DROP TABLE "user" IF EXISTS;

DROP TABLE "new_visitor" IF EXISTS;
