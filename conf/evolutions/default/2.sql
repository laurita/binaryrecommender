# --- !Ups

create table moviePairs (
	movie1_id                 integer not null,
	movie2_id                 integer not null,
  logpopcorr                float,
	constraint pk_moviePairs primary key (movie1_id, movie2_id))
as select * from csvread('data/data1/moviePairs.dat', null, 'fieldSeparator=, caseSensitiveColumnNames=true')
;

create table ml_ratings (
	user_id										integer not null,
	movie_id									integer not null,
	value											integer not null,
  time											bigint)
as select *
from csvread('data/data1/very_pop_ratings.dat', null, 'fieldSeparator=, caseSensitiveColumnNames=true')
;

create table comparisons (
	user_id										integer not null,
	question									integer not null,
	list_nr										integer not null,
  answer										integer not null,
	constraint pk_comparisons primary key (user_id, question, list_nr))
;

create table similarities (
	user1_id									integer not null,
	user2_id									integer not null,
	similarity								float,
	signSimilarity						float,
constraint pk_similarities primary key (user1_id, user2_id)
);

create table kmatrix (
  user_id                   integer not null,
	movie1_id									integer not null,
	movie2_id									integer not null,
	cValue										float,
	wValue										float,
constraint pk_kmatrix primary key (user_id, movie1_id, movie2_id)
);

create table recommendation_comparisons (
  user_id                   integer not null,
	comparison                integer not null,
constraint pk_recommendation_comparisons primary key (user_id)
);

ALTER SEQUENCE user_seq RESTART WITH 7000;
	
# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists moviePairs;
drop table if exists ml_ratings;
drop table if exists comparisons;
drop table if exists similarities;
drop table if exists kmatrix;
drop table if exists recommendation_comparisons;

SET REFERENTIAL_INTEGRITY TRUE;
