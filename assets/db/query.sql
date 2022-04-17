SELECT actors.name, plays.role, movies.title
FROM actors
INNER JOIN plays on actors.id = plays.actor
INNER JOIN movies on movies.id = plays.movie
WHERE actors.birthday > 1972
ORDER BY actors.name ASC;
