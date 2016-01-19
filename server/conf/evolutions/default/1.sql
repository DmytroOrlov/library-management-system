# --- !Ups

create table "people" (
  "id" bigserial primary key,
  "name" varchar not null unique,
  "password" varchar not null
);

# --- !Downs

drop table "people" if exists;
