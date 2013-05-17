#!/bin/bash

createuser flapjack
createdb flapjack

psql -Uflapjack << EOF
create sequence hibernate_sequence;
create sequence session_id_seq start with 50 increment by 50;
create table person (
	id bigint not null primary key,
	email text not null unique,
	name text,
	admin boolean not null default false
);
create table session (
	id bigint not null primary key,
	person_id bigint not null references person(id),
	value varchar(255) not null,
	updated timestamp with time zone
);
EOF