package controllers

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc._
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import models.Actor

class ActorReportController @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, controllerComponents: ControllerComponents)(implicit exec: ExecutionContext) extends AbstractController(controllerComponents) with HasDatabaseConfigProvider[JdbcProfile] {
  def allActors(page: Option[Int] = Some(1)): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    val pageLimit: Int = 5

    page match {
      case Some(0) => {
        db.run {
          sql"#${
            s"""
               |SELECT * FROM actors
               |ORDER BY name;
               |""".stripMargin
          }".as[Actor]
        } map {
          unresolvedValue => {
            val actors: Vector[Actor] = unresolvedValue
            Ok(views.html.someActors(actors, 0, 0))
          }
        } recoverWith[Result] {
          case t: Throwable => userPrompt(t)
        }
      }
      case _ => {
        db.run {
          sql"#${
            s"""
               |SELECT * FROM actors
               |ORDER BY name
               |LIMIT $pageLimit
               |OFFSET ${(page.getOrElse(1) - 1) * pageLimit};
               |""".stripMargin
          }".as[Actor]
        } zip {
          db.run {
            sql"#${
              s"""
                 |SELECT COUNT(*) FROM actors;
                 |""".stripMargin
            }".as[Int].head
          }
        } map {
          unresolvedValue => {
            val (actors: Vector[Actor], actorsCount: Int) = unresolvedValue
            Ok(views.html.someActors(actors, page.getOrElse(1), Math.ceil(actorsCount / pageLimit.toFloat).toInt))
          }
        } recoverWith[Result] {
          case t: Throwable => userPrompt(t)
        }
      }
    }
  }
  }

  def singleActor(actorId: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    db.run {
      sql"#${
        s"""
           |SELECT * FROM actors
           |WHERE id = ${actorId};
           |""".stripMargin
      }".as[Actor].head
    } map {
      unresolvedValue => {
        val actor: Actor = unresolvedValue
        Ok(views.html.singleActor(actor))
      }
    } recoverWith[Result] {
      case t: Throwable => userPrompt(t)
    }
  }
  }

  def deleteActor(actorId: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    db.run {
      sqlu"#${
        s"""
           |DELETE FROM actors
           |WHERE id = ${actorId};
           |""".stripMargin
      }"
    } map {
      _ => Redirect(routes.ActorReportController.allActors(Some(1)))
    } recoverWith[Result] {
      case t: Throwable => userPrompt(t)
    }
  }
  }

  def userPrompt(t: Throwable): Future[Result] = Future {
    t.printStackTrace()
    new Status(503)(views.html.userPrompt("Service unavailable, please try again in a while ..."))
  }
}