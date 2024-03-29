# --- !Ups

create table moviePairs (
	movie1_id                 integer not null,
	movie2_id                 integer not null,
  logpopcorr                float,
	constraint pk_moviePairs primary key (movie1_id, movie2_id))
  ;
  
COPY moviePairs FROM '/Users/laura/Sandbox/play/movies/data/data1/moviePairs.dat' DELIMITER ',' CSV;

create table ml_ratings (
	user_id										integer not null,
	movie_id									integer not null,
	value											integer not null,
  time											bigint)
  ;
  
COPY ml_ratings FROM '/Users/laura/Sandbox/play/movies/data/data1/very_pop_ratings.dat' DELIMITER ',' CSV;

create table comparisons (
	user_id										integer not null,
	question									integer not null,
  answer										integer not null,
	constraint pk_comparisons primary key (user_id, question))
;

create table recommendation_comparisons (
  user_id                   integer not null,
	comparison                integer not null,
constraint pk_recommendation_comparisons primary key (user_id)
);

ALTER SEQUENCE users_seq RESTART WITH 7000;

ALTER TABLE preference ALTER COLUMN id SET DEFAULT nextval('preference_seq');
ALTER TABLE preference ALTER COLUMN id SET NOT NULL;
ALTER SEQUENCE preference_seq OWNED BY preference.id;
	
# --- !Downs


drop table if exists moviePairs;
drop table if exists ml_ratings;
drop table if exists comparisons;
drop table if exists recommendation_comparisons;