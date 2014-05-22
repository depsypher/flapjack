#!/bin/bash

createuser flapjack
createdb flapjack

psql -Uflapjack << EOF
create sequence hibernate_sequence;
create sequence session_id_seq start with 10 increment by 10;
create table person (
	person_id bigint not null primary key,
	email text not null unique,
	name text,
	role varchar(32) not null
);
create table session (
	session_id bigint not null primary key,
	person_id bigint not null references person(person_id),
	value varchar(255) not null,
	updated timestamp with time zone
);
EOF