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

    def query[T](queryString: String)(implicit getResult: GetResult[T]): Future[Vector[T]] = {
      db.run {
        sql"#${queryString}".as[T]
      } andThen {
        case _ => db.close()
      }
    }

    def queryToResult[T](queryString: String, header: List[String])(implicit getResult: GetResult[T]): Future[Result] = {
      val result: Future[Vector[T]] = query[T](queryString)
      result.failed match {
        case _: Future[NoSuchElementException] => result map { x => Ok(views.html.allRows(x.asInstanceOf[Vector[List[String]]], header)) }
        case t: Future[Throwable] => {
          t foreach { e => e.printStackTrace() }
        }
          Future { new Status(503)(views.html.userPrompt("Service unavailable, please try again in a while ...")) }
      }
    }

    tableName match {
      case "movies" => queryToResult[(Int, String, Int)]("SELECT id, title, year FROM movies", List("ردیف", "عنوان", "سال تولید"))
      case "actors" => queryToResult[(Int, String, Int)]("SELECT id, name, birthday FROM actors", List("ردیف", "نام", "سال تولد"))
      case "plays" => queryToResult[(String, String, String)](
        s"""
           |SELECT actors.name, plays.role, movies.title
           |FROM actors
           |INNER JOIN plays on actors.id = plays.actor
           |INNER JOIN movies on movies.id = plays.movie
           |WHERE actors.birthday > 1971
           |ORDER BY actors.name ASC;
           |""".stripMargin,
        List("بازیگر", "نقش", "فیلم")
      )
      case _ => Future { NotFound(views.html.notFound()) }
    }
  }
  }
}
