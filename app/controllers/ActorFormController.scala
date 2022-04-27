package controllers

import play.api.data._

import javax.inject._
import play.api.mvc._
import play.api.data.Forms._
import slick.jdbc.PostgresProfile.api._
import models.forms.Actor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class ActorFormController @Inject()(messagesAction: MessagesActionBuilder, val controllerComponents: ControllerComponents) extends BaseController {
  implicit val db = Database.forConfig("db")

  val form: Form[Actor] = Form(
    mapping(
      "name" -> text,
      "birthday" -> number(min = 0)
    )(Actor.apply)(Actor.unapply)
  )

  def blankActorForm(): Action[AnyContent] = messagesAction { implicit messagesRequest: MessagesRequest[AnyContent] => {
    Ok(views.html.newActor(form))
  }
  }

  def newActor(): Action[AnyContent] = messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] => {
    implicit val db = Database.forConfig("db")

    val successFunction: Actor => Future[Result] = { data => {
      val newActor = models.forms.Actor(data.name, data.birthday)
      val queryString =
        s"""
           |INSERT INTO actors
           |(name, birthday)
           |VALUES
           |('${newActor.name}', ${newActor.birthday});
           |""".stripMargin

      db.run {
        sqlu"#$queryString"
      } andThen {
        case _ => db.close()
      } map {
        case _: Int => Redirect(routes.ActorReportController.allActors()).flashing("info" -> "اطلاعات بازیگر جدید در سیستم ثبت گردید")
      } recover {
        t: Throwable => {
          t.printStackTrace()
          new Status(503)(views.html.userPrompt("Service unavailable, please try again in a while ..."))
        }
      }
    }
    }
    val errorFunction: Form[Actor] => Future[Result] = { formWithErrors => {
      Future {
        BadRequest(views.html.newActor(formWithErrors))
      }
    }
    }

    val actorData: Form[Actor] = form.bindFromRequest()
    actorData.fold(errorFunction, successFunction)

  }
  }

  def filledActorForm(actorId: String): Action[AnyContent] = messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] => {
    query[models.Actor](
      s"""
         |SELECT * FROM actors
         |WHERE id = ${actorId};
         |""".stripMargin
    ) map {
      resolvedValue => Map("name" -> resolvedValue.head.name, "birthday" -> resolvedValue.head.birthday.toString)
    } map {
      resolvedValue => form.bind(resolvedValue)
    } map {
      case value: Form[Actor] => Ok(views.html.editActor(value, actorId))
    } recover {
      case t: Throwable => {
        t.printStackTrace()
        new Status(503)(views.html.userPrompt("Service unavailable, please try again in a while ..."))
      }
    }
  }
  }

  def editActor(actorId: String): Action[AnyContent] = messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] => {

    val successFunction: Actor => Future[Result] = { data => {
      val modifiedActor = models.forms.Actor(data.name, data.birthday)
      implicit val db = Database.forConfig("db")

      val queryString =
        s"""
           |UPDATE actors
           |SET name = '${modifiedActor.name}', birthday = ${modifiedActor.birthday}
           |WHERE id = $actorId;
           |""".stripMargin

      db.run {
        sqlu"#${queryString}"
      } andThen {
        case _ => db.close()
      } map {
        case _: Int => Redirect(routes.ActorReportController.allActors()).flashing("info" -> "تغییرات اطلاعات بازیگر ثبت گردید")
      } recover {
        case t: Throwable => {
          t.printStackTrace()
          new Status(503)(views.html.userPrompt("Service unavailable, please try again in a while ..."))
        }
      }
    }
    }
    val errorFunction: Form[Actor] => Future[Result] = { formWithErrors => {
      Future {
        BadRequest(views.html.editActor(formWithErrors, actorId))
      }
    }
    }

    val actorData: Form[Actor] = form.bindFromRequest()
    actorData.fold[Future[Result]](errorFunction, successFunction)

  }
  }

}
