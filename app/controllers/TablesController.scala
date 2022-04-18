package controllers

import play.api.mvc._
import play.twirl.api.HtmlFormat
import slick.jdbc.GetResult
import slick.jdbc.PostgresProfile.api._

import javax.inject._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class TablesController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  def all(tableName: String) = Action { implicit request: Request[AnyContent] => {
    val db = Database.forConfig("db")

    def query[T](queryString: String)(implicit getTResult: GetResult[T]): Either[Exception, Vector[T]] = {
      val tryBlock: Try[Vector[T]] = Try {
        Await.result(
          db.run {
            sql"#${queryString}".as[T]
          }, 2.second)
      }
      db.close()
      tryBlock match {
        case Success(s: Vector[T]) => Right(s)
        case Failure(e: Exception) => Left(e)
      }
    }

    def queryToResult[T](queryString: String, header: (String, String, String))(implicit getTResult: GetResult[T]): Result = {
      query[T](queryString) match {
        case Right(r: Vector[T]) => Ok(views.html.allRows(r.asInstanceOf[Vector[(Any, Any, Any)]], header))
        case Left(e: Exception) => {
          e.printStackTrace()
          new Status(503)(views.html.userPrompt("Service unavailable, please try again in a while ..."))
        }
      }
    }

    tableName match {
      case "movies" => queryToResult[(Int, String, Int)]("SELECT id, title, year FROM movies", ("ردیف", "عنوان", "سال تولید"))
      case "actors" => queryToResult[(Int, String, Int)]("SELECT id, name, birthday FROM actors", ("ردیف", "نام", "سال تولد"))
      case _ => NotFound(views.html.notFound())
    }
  }
  }
}
