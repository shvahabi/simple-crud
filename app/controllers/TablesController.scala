package controllers

import play.api.mvc._
import play.twirl.api.HtmlFormat
import slick.jdbc.PostgresProfile.api._

import javax.inject._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class TablesController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  def all(tableName: String) = Action { implicit request: Request[AnyContent] => {
    val db = Database.forConfig("db")

    def query(queryString: String): Either[Exception, Vector[(Int, String, Int)]] = {
      Try {
        Await.result(
          db.run {
            sql"#${queryString}".as[(Int, String, Int)]
          }, 2.second)
      } match {
        case Success(s: Vector[(Int, String, Int)]) => Right(s)
        case Failure(e: Exception) => Left(e)
      }
    }

    def queryToResult[T](queryString: String, header: (String, String, String)): Result = {
      query(queryString) match {
        case Right(r: Vector[(Int, String, Int)]) => Ok(views.html.allRows(r, header))
        case Left(e: Exception) => {
          e.printStackTrace()
          new Status(503)(views.html.userPrompt("Service unavailable, please try again in a while ..."))
        }
      }
    }

    tableName match {
      case "movies" => queryToResult("SELECT id, title, year FROM movies", ("ردیف", "عنوان", "سال تولید"))
      case "actors" => queryToResult("SELECT id, name, birthday FROM actors", ("ردیف", "نام", "سال تولد"))
      case _ => NotFound(views.html.notFound())
    }
  }
  }
}
