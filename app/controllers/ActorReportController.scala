package controllers

import javax.inject._
import play.api.mvc._
import slick.jdbc.PostgresProfile.api._
import models.Actor

@Singleton
class ActorReportController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  def all() = Action.async { implicit request: Request[AnyContent] => {
    implicit val db = Database.forConfig("db")
    report[Actor](
      query[Actor](
        s"""
           |SELECT * FROM actors;
           |""".stripMargin
      ),
      result => Ok(views.html.someActors(result))
    )
  }
  }

  def single(actorId: String) = Action.async { implicit request: Request[AnyContent] => {
    implicit val db = Database.forConfig("db")
    report[Actor](
      query[Actor](
        s"""
           |SELECT * FROM actors
           WHERE id = ${actorId};
           |""".stripMargin
      ),
      result => Ok(views.html.someActors(result))
    )
  }
  }
}