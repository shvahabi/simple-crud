CREATE TABLE movies (
    id serial PRIMARY KEY,
    title varchar(255) NOT NULL,
    year integer NOT NULL);

CREATE TABLE actors (
    id serial PRIMARY KEY,
    name varchar(255) NOT NULL,
    birthday integer NOT NULL);

CREATE TABLE plays (
    movie integer NOT NULL REFERENCES movies (id) ON DELETE CASCADE,
    actor integer NOT NULL REFERENCES actors (id) ON DELETE CASCADE,
    role varchar(255));
