package controllers

import play.api.libs.json._

case class Movie(title: String, year: Int)

object Movie {
  implicit val movieReads = Json.reads[Movie]
}
