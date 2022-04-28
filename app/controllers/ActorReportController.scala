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

    query[Actor](
      s"""
         |SELECT * FROM actors
         |ORDER BY id
         |LIMIT 5
         |OFFSET ${(page.getOrElse(1) - 1) * 5};
         |""".stripMargin
    ) zip[Int] {
      count(
        s"""
           |SELECT COUNT(*) FROM actors;
           |""".stripMargin
      )
    } map {
      result => Ok(views.html.someActors(result._1, page.getOrElse(1), result._2))
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