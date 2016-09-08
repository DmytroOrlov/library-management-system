# --- !Ups

CREATE TABLE "library" (
  "uuid"           UUID        PRIMARY KEY
  , "name"         VARCHAR     NOT NULL    UNIQUE
  , "created"      TIMESTAMP   NOT NULL           DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "visitor" (
  "uuid"           UUID        PRIMARY KEY
  , "number"       SERIAL
  , "library_uuid" UUID        NOT NULL    REFERENCES "library"("uuid")
  , "first_name"   VARCHAR     NOT NULL
  , "last_name"    VARCHAR     NOT NULL
  , "middle_name"  VARCHAR
  , "extra_name"   VARCHAR
  , "created"      TIMESTAMP   NOT NULL    DEFAULT CURRENT_TIMESTAMP
  , UNIQUE ("number", "library_uuid")
);

CREATE TABLE "lib_user" (
  "uuid"           UUID        PRIMARY KEY
  , "email"         VARCHAR     NOT NULL
  , "password"     VARCHAR     NOT NULL
  , "visitor_uuid" UUID                    UNIQUE REFERENCES "visitor"("uuid")
  , "created"      TIMESTAMP   NOT NULL           DEFAULT CURRENT_TIMESTAMP
  , UNIQUE ("email")
);

CREATE TABLE "new_visitor" (
  "uuid"           UUID        PRIMARY KEY REFERENCES "lib_user"
  , "first_name"   VARCHAR     NOT NULL
  , "last_name"    VARCHAR     NOT NULL
  , "middle_name"  VARCHAR
  , "extra_name"   VARCHAR
  , "created"      TIMESTAMP   NOT NULL    DEFAULT CURRENT_TIMESTAMP
);


# --- !Downs

DROP TABLE "library" IF EXISTS;

DROP TABLE "visitor" IF EXISTS;

DROP TABLE "lib_user" IF EXISTS;

DROP TABLE "new_visitor" IF EXISTS;
