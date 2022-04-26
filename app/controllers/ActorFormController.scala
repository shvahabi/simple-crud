package controllers

import models.forms.Actor
import play.api.data._

import javax.inject._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

class ActorFormController @Inject()(messagesAction: MessagesActionBuilder, controllerComponents: ControllerComponents) extends AbstractController(controllerComponents) {

  val actor: Form[Actor] = Form(
    mapping(
      "name" -> text,
      "birthday" -> number(min = 0)
    )(Actor.apply)(Actor.unapply)
  )


  def actorForm() = messagesAction { implicit messagesRequest: MessagesRequest[AnyContent] => {
    //    val actorData = actorForm.bindFromRequest.get
    Ok(views.html.newActor(actor))
  }
  }

  def newActor() = Action { implicit messagesRequest: Request[AnyContent] => {
    val actorData: Actor = actor.bindFromRequest.get
    Redirect(routes.ActorReportController.allActors()).flashing("info" -> "Blog post added (trust me)")
//    val successFunction: Actor => Result = { data: Actor => {
    //      val newActor = models.forms.Actor(data.name, data.birthday)
    //      println(newActor)
    //      Redirect(routes.ActorReportController.allActors()).flashing("info" -> "Blog post added (trust me)")
    //    }
    //    }
    //    val errorFunction: Form[Actor] => Result = { formWithErrors: Form[Actor] => {
    //      BadRequest(views.html.newActor(formWithErrors))
    //    }
    //    }
    //    actorData.fold(errorFunction, successFunction)
  }
  }

  def editActor(actorId: String) = messagesAction { implicit messagesRequest: MessagesRequest[AnyContent] => {
    val anyData = Map("name" -> "bob", "birthday" -> "21")
    val actorData = actor.bind(anyData).get
    Ok(views.html.newActor(actor))
  }
  }

}
