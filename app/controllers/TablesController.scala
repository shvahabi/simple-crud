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

    def query(queryString: String): Future[Vector[(String, String, String)]] = {
//      implicit val getResult: GetResult[List[String]] = GetResult(x => x.nextString()::x.nextString()::x.nextString()::Nil)
      db.run {
        sql"#${queryString}".as[(String, String, String)]
      } andThen {
        case _ => db.close()
      }
    }

    def queryToResult(queryString: String, header: List[String]): Future[Result] = {
      val result: Future[Vector[(String, String, String)]] = query(queryString)
      result.failed match {
        case _: Future[NoSuchElementException] => result map { x => Ok(views.html.allRows(x.map(y => y._1::y._2::y._3::Nil), header)) }
        case t: Future[Throwable] => {
          t foreach { e => e.printStackTrace() }
        }
          Future { new Status(503)(views.html.userPrompt("Service unavailable, please try again in a while ...")) }
      }
    }

    tableName match {
      case "movies" => queryToResult("SELECT id, title, year FROM movies", List("ردیف", "عنوان", "سال تولید"))
      case "actors" => queryToResult("SELECT id, name, birthday FROM actors", List("ردیف", "نام", "سال تولد"))
      case "plays" => queryToResult(
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
