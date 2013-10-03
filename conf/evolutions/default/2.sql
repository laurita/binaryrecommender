# --- !Ups

create table moviePairs (
	movie1_id                 varchar(255) not null,
	movie2_id                 varchar(255) not null,
	constraint pk_moviePairs primary key (movie1_id, movie2_id))
;

insert into moviePairs values('2710', '231');
insert into moviePairs values('2700', '2712');
insert into moviePairs values('2427', '1721');
insert into moviePairs values('1676', '288');
	
# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists moviePairs;

SET REFERENTIAL_INTEGRITY TRUE;
