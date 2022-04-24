package controllers

import play.api.mvc._
import play.api.libs.json._
import slick.jdbc.PostgresProfile.api._

import javax.inject._
import scala.concurrent.Future
import scala.util.Success

@Singleton
class RecordsController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  def form(tableName: String) = Action { implicit request: Request[AnyContent] => {
    tableName match {
      case "movie" => Ok(views.html.allRecords(List[(String, String, String)](("عنوان", "title", "text"), ("سال ساخت", "year", "number")), "new/movie"))
      case "actor" => Ok(views.html.allRecords(List[(String, String, String)](("نام", "name", "text"), ("سال تولد", "birthday", "number")), "new/actor"))
    }
  }
  }

  def all(tableName: String) = Action.async { implicit request: Request[AnyContent] => {
    import scala.concurrent.ExecutionContext.Implicits.global
    val db = Database.forConfig("db")

    def insertInto(queryString: String): Future[Result] = {
      val insertStatement: Future[Int] = db.run {
        sqlu"#${queryString}"
      } andThen {
        case _ => db.close()
      }
      insertStatement.failed match {
        case x: Future[NoSuchElementException] => Future {
          Ok(views.html.userPrompt("One row successfully added to database ..."))
        }
        case t: Future[Throwable] => {
          t foreach { e => e.printStackTrace() }
          Future {
            new Status(503)(views.html.userPrompt("Service unavailable, please try again in a while ..."))
          }
        }
      }
    }

    tableName match {
      case "movie" => {
        println(request.body.asJson.get)
        val movie: Movie = Json.fromJson[Movie](request.body.asJson.get).get
        insertInto(
          s"""
             |INSERT INTO movies (title, year)
             |VALUES ('${movie.title}', '${movie.year.toInt}');
             |""".stripMargin)
      }
      case "actor" => {
//        println(Json.fromJson[Actor](request.body.asJson.get).get)
        val actor: Actor = Json.fromJson[Actor](request.body.asJson.get).get
        insertInto(
          s"""
             |INSERT INTO actors (name, birthday)
             |VALUES ('${actor.name}', '${actor.birthday.toInt}');
             |""".stripMargin)
      }
      case _ => Future {
        NotFound(views.html.notFound())
      }
    }
  }
  }
}
