# --- !Ups

create table moviePairs (
	movie1_id                 varchar(255) not null,
	movie2_id                 varchar(255) not null,
	constraint pk_moviePairs primary key (movie1_id, movie2_id))
as select * from csvread('data/moviePairs.dat', 'fieldSeparator=,')
;
	
# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists moviePairs;

SET REFERENTIAL_INTEGRITY TRUE;
