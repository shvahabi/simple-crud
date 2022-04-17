CREATE TABLE movies (
    id serial PRIMARY KEY,
    title varchar(255) NOT NULL,
    year integer NOT NULL);

CREATE TABLE actors (
    id serial PRIMARY KEY,
    name varchar(255) NOT NULL,
    birthday integer NOT NULL);

CREATE TABLE plays (
    movie integer NOT NULL REFERENCES movies (id),
    actor integer NOT NULL REFERENCES actors (id),
    role varchar(255));
