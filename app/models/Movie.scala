package models

import slick.jdbc.GetResult

case class Movie(id: Int, title: String, year: Int)

case object Movie {
  implicit val getMovies = GetResult[Movie](r => Movie(r.nextInt, r.nextString, r.nextInt))
}
