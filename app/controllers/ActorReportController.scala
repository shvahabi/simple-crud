package controllers

import javax.inject._
import play.api.mvc._
import slick.jdbc.PostgresProfile.api._
import models.Actor

@Singleton
class ActorReportController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  def allActors(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    implicit val db = Database.forConfig("db")
    report[Actor](
      query[Actor](
        s"""
           |SELECT * FROM actors
           |ORDER BY id;
           |""".stripMargin
      ),
      result => Ok(views.html.someActors(result))
    )
  }
  }

  def singleActor(actorId: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    implicit val db = Database.forConfig("db")
    report[Actor](
      query[Actor](
        s"""
           |SELECT * FROM actors
           |WHERE id = ${actorId};
           |""".stripMargin
      ),
      result => Ok(views.html.someActors(result))
    )
  }
  }

  def deleteActor(actorId: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    implicit val db = Database.forConfig("db")

    report[Int](
      delete(
        s"""
           |DELETE FROM actors
           |WHERE id = ${actorId};
           |""".stripMargin
      ),
      _ => Redirect(routes.ActorReportController.allActors())
    )
  }
  }
}