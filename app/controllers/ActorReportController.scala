package controllers

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc._
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import models.{Actor, Actors}

class ActorReportController @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, controllerComponents: ControllerComponents)(implicit exec: ExecutionContext) extends AbstractController(controllerComponents) with HasDatabaseConfigProvider[JdbcProfile] {
  val actors = TableQuery[Actors]
  def allActors(page: Option[Int] = Some(1)): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    val pageLimit: Int = 5

    page match {
      case Some(0) => {
        db.run {
          actors.sortBy(_.name.asc).result
        } map {
          unresolvedValue => {
            val actors: Vector[Actor] = unresolvedValue.toVector
            Ok(views.html.someActors(actors, 0, 0))
          }
        } recoverWith[Result] {
          case t: Throwable => userPrompt(t)
        }
      }
      case _ => {
        db.run {
          actors.sortBy(_.name.asc).drop((page.getOrElse(1) - 1) * pageLimit).take(pageLimit).result zip actors.distinct.length.result
        } map {
          unresolvedValue => {
            val actorsRecords: Vector[Actor] = unresolvedValue._1.toVector
            val actorsCount: Int = unresolvedValue._2
            Ok(views.html.someActors(actorsRecords, page.getOrElse(1), Math.ceil(actorsCount / pageLimit.toFloat).toInt))
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
      actors.filter(_.id === actorId.asColumnOf[Int]).result.head
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
      actors.filter(_.id === actorId.asColumnOf[Int]).delete
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