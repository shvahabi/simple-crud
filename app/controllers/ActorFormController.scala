package controllers

import models.forms.Actor

import javax.inject._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

class ActorFormController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  val userForm = Form(
    mapping(
      "name" -> text,
      "age" -> number
    )(Actor.apply)(Actor.unapply)
  )

  def newActor(name: String) = Action { implicit request: Request[AnyContent] => {
    val userData = userForm.bindFromRequest.get
    Ok("Hello " + name)
  }
  }

  def editActor() = Action { implicit request: Request[AnyContent] => {
    val anyData  = Map("name" -> "bob", "age" -> "21")
    val userData = userForm.bind(anyData).get
    Ok
  }
  }

}
