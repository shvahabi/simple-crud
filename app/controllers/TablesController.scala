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

    case class Movies(id: Int, title: String, year: Int)
    case class Actors(id: Int, name: String, birthday: Int)
    case class Plays(name: String, role: String, title: String)

    def queryToResult(implicit getResult: GetResult[Movies]): Future[Result] = {
      val result: Future[Vector[Movies]] = db.run {
        sql"SELECT * FROM movies".as[Movies]
      } andThen {
        case _ => db.close()
      }
      result.failed match {
        case _: Future[NoSuchElementException] => result map { x => Ok(views.html.allRows(x.map(y => y.productIterator.toList), List("ردیف", "عنوان", "سال تولید"))) }
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
        implicit val getResult = GetResult[Movies](r => Movies(r.nextInt, r.nextString, r.nextInt))
        queryToResult(getResult)
      }

//      case "actors" => queryToResult("SELECT id, name, birthday FROM actors", List("ردیف", "نام", "سال تولد"))(implicit val getResult = GetResult[Actors](r => Actors(r.nextInt, r.nextString, r.nextInt)))
//      case "plays" => queryToResult[Plays](
//        s"""
//           |SELECT actors.name, plays.role, movies.title
//           |FROM actors
//           |INNER JOIN plays on actors.id = plays.actor
//           |INNER JOIN movies on movies.id = plays.movie
//           |WHERE actors.birthday > 1971
//           |ORDER BY actors.name ASC;
//           |""".stripMargin,
//        List("بازیگر", "نقش", "فیلم")
//      )(implicit val getResult = GetResult(r => Actors(r.nextInt, r.nextString, r.nextInt)))
      case _ => Future { NotFound(views.html.notFound()) }
    }
  }
  }
}
