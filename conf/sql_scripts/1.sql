# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table movie (
  id                        integer not null,
  title                     varchar(255),
  description               varchar(255),
  logpopvar                 float,
  trailer_link              varchar(255),
  length                    integer,
  imdb_link                 varchar(255),
  genres                    varchar(255),
  constraint pk_movie primary key (id))
;

create table preference (
  id                        bigint not null,
  value                     integer,
  additional                boolean,
  user_id                   integer,
  movie1_id                 integer,
  movie2_id                 integer,
  constraint pk_preference primary key (id))
;

create table rating (
  id                        integer not null,
  value                     integer,
  additional                boolean,
  user_id                   integer,
  movie_id                  integer,
  constraint pk_rating primary key (id))
;

create table recommendation (
  id                        integer not null,
  rank                      integer,
  user_id                   integer,
  movie_id                  integer,
  good                      boolean,
  seen                      boolean,
  updated                   boolean,
  constraint pk_recommendation primary key (id))
;

create table users (
  id                        integer not null,
  email                     varchar(255),
  created_at                timestamp,
  state                     varchar(255),
  experiment_group          integer,
  stage1done                boolean,
  stage2done                boolean,
  after_update              boolean,
  question1                 integer,
  question2                 integer,
  question3                 integer,
  question4                 integer,
  constraint pk_users primary key (id))
;

create sequence movie_seq;

create sequence preference_seq;

create sequence rating_seq;

create sequence recommendation_seq;

create sequence users_seq;

alter table preference add constraint fk_preference_user_1 foreign key (user_id) references users (id);
create index ix_preference_user_1 on preference (user_id);
alter table preference add constraint fk_preference_movie1_2 foreign key (movie1_id) references movie (id);
create index ix_preference_movie1_2 on preference (movie1_id);
alter table preference add constraint fk_preference_movie2_3 foreign key (movie2_id) references movie (id);
create index ix_preference_movie2_3 on preference (movie2_id);
alter table rating add constraint fk_rating_user_4 foreign key (user_id) references users (id);
create index ix_rating_user_4 on rating (user_id);
alter table rating add constraint fk_rating_movie_5 foreign key (movie_id) references movie (id);
create index ix_rating_movie_5 on rating (movie_id);
alter table recommendation add constraint fk_recommendation_user_6 foreign key (user_id) references users (id);
create index ix_recommendation_user_6 on recommendation (user_id);
alter table recommendation add constraint fk_recommendation_movie_7 foreign key (movie_id) references movie (id);
create index ix_recommendation_movie_7 on recommendation (movie_id);



# --- !Downs

drop table if exists movie cascade;

drop table if exists preference cascade;

drop table if exists rating cascade;

drop table if exists recommendation cascade;

drop table if exists users cascade;

drop sequence if exists movie_seq;

drop sequence if exists preference_seq;

drop sequence if exists rating_seq;

drop sequence if exists recommendation_seq;

drop sequence if exists users_seq;

