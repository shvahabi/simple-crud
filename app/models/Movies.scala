package models

import slick.jdbc.PostgresProfile.api._

class Movies(tag: Tag) extends Table[Movie](tag, "movies") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def year = column[Int]("year")
  def * = (id, title, year).shaped <> ((Movie.apply _).tupled, Movie.unapply)
}