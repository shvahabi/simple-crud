package controllers

import play.api.libs.json._

case class Actor(name: String, birthday: String)

object Actor {
  implicit val actorReads = Json.reads[Actor]
}
