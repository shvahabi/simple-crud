package controllers

import play.api.mvc._
import play.twirl.api.HtmlFormat
import slick.jdbc.GetResult
import slick.jdbc.PostgresProfile.api._

import javax.inject._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TablesController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  def all(tableName: String) = Action { implicit request: Request[AnyContent] => {
    Result {
      val db = Database.forConfig("db")

      def newQuery[T](queryString: String, header: (String, String, String))(implicit getResult: GetResult[T]): Future[Vector[T]] =
        db.run {
          sql"#${queryString}".as[T]
        } andThen {
          case _ => db.close()
        }

      def queryToResult[T](queryString: String, header: (String, String, String))(implicit getTResult: GetResult[T]): Result = Result ({
        val result: Future[Vector[T]] = newQuery[T](queryString, header)

        val a: Int = try {
          result.failed
          
        } catch {
          case e: NoSuchElementException => Ok(views.html.allRows(result, header))
        }

        andThen {
          case Success(v: Vector[T]) =>
          case Failure(e: Exception) => {
            e.printStackTrace()
            new Status(503)(views.html.userPrompt("Service unavailable, please try again in a while ..."))
          }
        }
      })

      tableName match {
        case "movies" => queryToResult[(Int, String, Int)]("SELECT id, title, year FROM movies", ("ردیف", "عنوان", "سال تولید"))
        case "actors" => queryToResult[(Int, String, Int)]("SELECT id, name, birthday FROM actors", ("ردیف", "نام", "سال تولد"))
        case "plays" => queryToResult[(String, String, String)](
          s"""
             |SELECT actors.name, plays.role, movies.title
             |FROM actors
             |INNER JOIN plays on actors.id = plays.actor
             |INNER JOIN movies on movies.id = plays.movie
             |WHERE actors.birthday > 1971
             |ORDER BY actors.name ASC;
             |""".stripMargin,
          ("بازیگر", "نقش", "فیلم")
        )
        case _ => NotFound(views.html.notFound())
      }
    }
  }
  }
}
