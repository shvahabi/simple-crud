package controllers

import models.forms.Actor
import play.api.data._

import javax.inject._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

class ActorFormController @Inject()(messagesAction: MessagesActionBuilder, controllerComponents: ControllerComponents) extends AbstractController(controllerComponents) {

  val actorForm: Form[Actor] = Form(
    mapping(
      "name" -> text,
      "birthday" -> number(min = 0)
    )(Actor.apply)(Actor.unapply)
  )

  val postUrl = routes.ActorFormController.newActor()

  def newActor() = messagesAction { implicit messagesRequest: MessagesRequest[AnyContent] => {
//    val actorData = actorForm.bindFromRequest.get
    Ok(views.html.newActor(actorForm, postUrl))
  }
  }

  def editActor() = Action { implicit request: Request[AnyContent] => {
    val anyData  = Map("name" -> "bob", "age" -> "21")
    val userData = actorForm.bind(anyData).get
    Ok
  }
  }

}
