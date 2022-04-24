package controllers

import play.api.libs.json._

case class Movie(title: String, year: String)

object Movie {
  implicit val movieReads = Json.reads[Movie]
}
