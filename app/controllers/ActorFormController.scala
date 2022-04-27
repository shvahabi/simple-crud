package controllers

import play.api.data._

import javax.inject._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import slick.jdbc.PostgresProfile.api._
import models.forms.Actor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class ActorFormController @Inject()(messagesAction: MessagesActionBuilder, controllerComponents: ControllerComponents) extends AbstractController(controllerComponents) {
  implicit val db = Database.forConfig("db")

  val actor: Form[Actor] = Form(
    mapping(
      "name" -> text,
      "birthday" -> number(min = 0)
    )(Actor.apply)(Actor.unapply)
  )


  def blankActorForm() = messagesAction { implicit messagesRequest: MessagesRequest[AnyContent] => {
    //    val actorData = actorForm.bindFromRequest.get
    Ok(views.html.newActor(actor))
  }
  }

  def newActor() = messagesAction { implicit messagesRequest: MessagesRequest[AnyContent] => {

    val successFunction: Actor => Result = { data: Actor => {
      val newActor = models.forms.Actor(data.name, data.birthday)
      println(newActor)
      Redirect(routes.ActorReportController.allActors()).flashing("info" -> "اطلاعات بازیگر در سیستم ثبت گردید")
    }
    }
    val errorFunction: Form[Actor] => Result = { formWithErrors: Form[Actor] => {
      BadRequest(views.html.newActor(formWithErrors))
    }
    }

    val actorData: Form[Actor] = actor.bindFromRequest
    actorData.fold(errorFunction, successFunction)

  }
  }

  def filledActorForm(actorId: String) = messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] => {
    query[models.Actor](
      s"""
         |SELECT * FROM actors
         |WHERE id = ${actorId};
         |""".stripMargin
    ) map {
      resolvedValue => Map("name" -> resolvedValue.head.name, "birthday" -> resolvedValue.head.birthday.toString)
    } map {
      resolvedValue => actor.bind(resolvedValue)
    } map {
      resolvedValue =>
        resolvedValue match {
          case value: Form[Actor] => Ok(views.html.newActor(value))
          case t: Throwable => {
            t.printStackTrace()
            new Status(503)(views.html.userPrompt("Service unavailable, please try again in a while ..."))
          }
        }
    }
  }
  }

  def editActor(actorId: String) = messagesAction { implicit messagesRequest: MessagesRequest[AnyContent] => {
    val anyData = Map("name" -> "bob", "birthday" -> "21")
    val actorData = actor.bind(anyData).get
    Ok(views.html.newActor(actor))
  }
  }

}
