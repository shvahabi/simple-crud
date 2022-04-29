package controllers

import javax.inject._
import play.api.mvc._
import slick.jdbc.PostgresProfile.api._
import models.Actor

@Singleton
class ActorReportController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  def allActors(page: Option[Int] = Some(1)): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    implicit val db = Database.forConfig("db")
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
    val pageLimit = 5

    page match {
      case Some(0) => {
        {
          query[Actor](
            s"""
               |SELECT * FROM actors
               |ORDER BY name;
               |""".stripMargin
          )
        } map {
          unresolvedValue => {
            val actors: Vector[Actor] = unresolvedValue
            Ok(views.html.someActors(actors, 0, 0))
          }
        }
      }
      case _ => {
        {
          query[Actor](
            s"""
               |SELECT * FROM actors
               |ORDER BY name
               |LIMIT $pageLimit
               |OFFSET ${(page.getOrElse(1) - 1) * pageLimit};
               |""".stripMargin
          )
        } zip {
          count(
            s"""
               |SELECT COUNT(*) FROM actors;
               |""".stripMargin
          )
        } map {
          unresolvedValue => {
            val (actors: Vector[Actor], actorsCount: Int) = unresolvedValue
            Ok(views.html.someActors(actors, page.getOrElse(1), Math.ceil(actorsCount / 5f).toInt))
          }
        }
      }
    }
  }
  }

  def singleActor(actorId: String): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] => {
      implicit val db = Database.forConfig("db")
      report[Actor](
        query[Actor](
          s"""
             |SELECT * FROM actors
             |WHERE id = ${
            actorId
          };
             |""".stripMargin
        ),
        result => Ok(views.html.singleActor(result.head))
      )
    }
  }

  def deleteActor(actorId: String): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] => {
      implicit val db = Database.forConfig("db")

      report[Int](
        delete(
          s"""
             |DELETE FROM actors
             |WHERE id = ${
            actorId
          };
             |""".stripMargin
        ),
        _ => Redirect(routes.ActorReportController.allActors(Some(1)))
      )
    }
  }
}