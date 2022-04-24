package controllers

import play.api.mvc._
import slick.jdbc.GetResult
import slick.jdbc.PostgresProfile.api._

import javax.inject._
import scala.concurrent.Future

@Singleton
class TablesController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  def all(tableName: String) = Action.async { implicit request: Request[AnyContent] => {
    import scala.concurrent.ExecutionContext.Implicits.global
    val db = Database.forConfig("db")

    def queryReport[T <: Product](queryString: String, heading: List[String], name: String)(implicit getResult: GetResult[T]): Future[Result] = {
      val queryResult: Future[Vector[T]] = db.run {
        sql"#${queryString}".as[T]
      } andThen {
        case _ => db.close()
      }
      queryResult.failed match {
        case _: Future[NoSuchElementException] => queryResult map { x => Ok(views.html.allRows(x.map(y => y.productIterator.toList), heading, name)) }
        case t: Future[Throwable] => {
          t foreach { e => e.printStackTrace() }
        }
          Future {
            new Status(503)(views.html.userPrompt("Service unavailable, please try again in a while ..."))
          }
      }
    }

    tableName match {
      case "movies" => {
        case class Movies(id: Int, title: String, year: Int)
        implicit val getMovies = GetResult[Movies](r => Movies(r.nextInt, r.nextString, r.nextInt))
        queryReport[Movies](
          s"""
             |SELECT id, title, year FROM movies
             |""".stripMargin,
          List("ردیف", "عنوان", "سال تولید", "عملیات"),
          "movie")
      }
      case "actors" => {
        case class Actors(id: Int, name: String, birthday: Int)
        implicit val getActors = GetResult[Actors](r => Actors(r.nextInt, r.nextString, r.nextInt))
        queryReport[Actors](
          s"""
             |SELECT id, name, birthday FROM actors
             |""".stripMargin,
          List("ردیف", "نام", "سال تولد", "عملیات"),
          "actor")
      }
      case "plays" => {
        case class Plays(name: String, role: String, title: String)
        implicit val getPlays = GetResult[Plays](r => Plays(r.nextString, r.nextString, r.nextString))
        queryReport[Plays](
          s"""
             |SELECT actors.name, plays.role, movies.title
             |FROM actors
             |INNER JOIN plays on actors.id = plays.actor
             |INNER JOIN movies on movies.id = plays.movie
             |WHERE actors.birthday > 1971
             |ORDER BY actors.name ASC;
             |""".stripMargin,
          List("بازیگر", "نقش", "فیلم"),
          "")
      }
      case _ => Future {
        NotFound(views.html.notFound())
      }
    }
  }
  }
}
