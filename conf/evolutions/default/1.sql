# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table movie (
  id                        varchar(255) not null,
  title                     varchar(255),
  description               varchar(255),
  constraint pk_movie primary key (id))
;

create table user (
  email                     varchar(255) not null,
  constraint pk_user primary key (email))
;

create sequence movie_seq;

create sequence user_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists movie;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists movie_seq;

drop sequence if exists user_seq;

