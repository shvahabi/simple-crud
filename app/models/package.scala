import slick.jdbc.GetResult

package object models {
  implicit val getActors = GetResult[Actor](r => Actor(r.nextInt, r.nextString, r.nextInt))
}
