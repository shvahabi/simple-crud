package controllers

import play.api.libs.json._

case class Movies(title: String, year: Int)

object Movies {
  implicit val moviesReads = Json.reads[Movies]
}
