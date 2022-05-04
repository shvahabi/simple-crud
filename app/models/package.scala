import slick.jdbc.GetResult
import slick.jdbc.PostgresProfile.api._

package object models {
  implicit val getActors = GetResult[Actor](r => Actor(r.nextInt, r.nextString, r.nextInt))

  class Actors(tag: Tag) extends Table[Actor](tag, "actors") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def birthday = column[Int]("birthday")
    def * = (id, name, birthday).shaped <> ((Actor.apply _).tupled, Actor.unapply)
  }
}
