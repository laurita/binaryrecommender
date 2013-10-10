# --- !Ups

create table moviePairs (
	movie1_id                 integer not null,
	movie2_id                 integer not null,
	constraint pk_moviePairs primary key (movie1_id, movie2_id))
as select * from csvread('data/moviePairs.dat', 'fieldSeparator=,')
;

create table ml_ratings (
	user_id										integer not null,
	movie_id									integer not null,
	value											integer not null,
  time											bigint)
as select *
from csvread('data/pop_ratings.dat', null, 'fieldSeparator=, caseSensitiveColumnNames=true')
;


	
# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists moviePairs;
drop table if exists ml_ratings;

SET REFERENTIAL_INTEGRITY TRUE;
