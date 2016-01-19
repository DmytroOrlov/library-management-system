# --- !Ups

create table "people" (
  "id" bigserial primary key,
  "name" varchar not null unique,
  "age" int not null
);

# --- !Downs

drop table "people" if exists;
