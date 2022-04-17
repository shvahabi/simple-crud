package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

@Singleton
class GreetController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  def greet(name: Option[String]) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.greet(s"Hello ${name.getOrElse("Anonymous")}!"))
  }

  def dbGreet() = Action { implicit request: Request[AnyContent] => {
    val db = Database.forConfig("db")
    val query: Future[Vector[String]] = db.run {
      sql"SELECT 'Database is configured properly ...'".as[(String)]
    }
    val timeout: Duration = 2.second
    val result: Either[Exception, String] = Try {
      Await.result(query, timeout)
    } match {
      case Success(s: Vector[String]) => Right(s.toList.head)
      case Failure(e: Exception) => Left(e)
    }
    result match {
      case Right(s: String) => Ok(views.html.greet(s))
      case Left(t: Exception) => {
        t.printStackTrace()
        new Status(424)(views.html.greet("Unknown problem with database configuration ..."))
      }
    }
  }
  }
}
