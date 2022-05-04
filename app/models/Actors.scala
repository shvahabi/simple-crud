package models

import slick.jdbc.PostgresProfile.api._

class Actors(tag: Tag) extends Table[Actor](tag, "actors") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def birthday = column[Int]("birthday")
  def * = (id, name, birthday).shaped <> ((Actor.apply _).tupled, Actor.unapply)
}