# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table movie (
  movie_id                  varchar(255) not null,
  title                     varchar(255),
  description               varchar(255),
  constraint pk_movie primary key (movie_id))
;

create table preference (
  pref_id                   bigint not null,
  value                     integer,
  user_user_id              bigint,
  movie1_movie_id           varchar(255),
  movie2_movie_id           varchar(255),
  constraint pk_preference primary key (pref_id))
;

create table rating (
  rating_id                 bigint not null,
  value                     integer,
  user_user_id              bigint,
  movie_movie_id            varchar(255),
  constraint pk_rating primary key (rating_id))
;

create table user (
  user_id                   bigint not null,
  email                     varchar(255),
  created_at                timestamp,
  experiment_group          integer,
  stage1done                boolean,
  question1                 varchar(255),
  question2                 varchar(255),
  question3                 varchar(255),
  question4                 varchar(255),
  constraint pk_user primary key (user_id))
;

create sequence movie_seq;

create sequence preference_seq;

create sequence rating_seq;

create sequence user_seq;

alter table preference add constraint fk_preference_user_1 foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;
create index ix_preference_user_1 on preference (user_user_id);
alter table preference add constraint fk_preference_movie1_2 foreign key (movie1_movie_id) references movie (movie_id) on delete restrict on update restrict;
create index ix_preference_movie1_2 on preference (movie1_movie_id);
alter table preference add constraint fk_preference_movie2_3 foreign key (movie2_movie_id) references movie (movie_id) on delete restrict on update restrict;
create index ix_preference_movie2_3 on preference (movie2_movie_id);
alter table rating add constraint fk_rating_user_4 foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;
create index ix_rating_user_4 on rating (user_user_id);
alter table rating add constraint fk_rating_movie_5 foreign key (movie_movie_id) references movie (movie_id) on delete restrict on update restrict;
create index ix_rating_movie_5 on rating (movie_movie_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists movie;

drop table if exists preference;

drop table if exists rating;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists movie_seq;

drop sequence if exists preference_seq;

drop sequence if exists rating_seq;

drop sequence if exists user_seq;

