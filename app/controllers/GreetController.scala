package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import slick.jdbc.PostgresProfile.api._
// import slick.jdbc.JdbcBackend._

import scala.concurrent.Await
import scala.concurrent.duration._
//import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

@Singleton
class GreetController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  def greet(name: Option[String]) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.greet(s"Hello ${name.getOrElse("Anonymous")}!"))
  }

  def dbGreet() = Action { implicit request: Request[AnyContent] => {
    val db = Database.forConfig("db")
    val query = sql"SELECT 'Database is configured properly ...'".as[(String)]
    val result: Either[Exception, String] = {
      Try {
        Await.result(db.run(query), 10.second).toList.head
      } match {
        case Success(value: String) => Right(value)
        case Failure(e: Exception) => Left(e)
      }
    }
    result match {
      case Right(value: String) =>
        Ok(views.html.greet(value))
      case Left(exception) =>
        exception.printStackTrace()
        new Status(424)("Strange error")
    }
  }
  }
}
