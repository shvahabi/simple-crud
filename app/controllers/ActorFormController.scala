package controllers

import javax.inject._
import play.api.mvc._

class ActorFormController @Inject() (val controllerComponents: ControllerComponents) extends BaseController {

  def newActor(name: String) = Action { implicit request: Request[AnyContent] =>
    Ok("Hello " + name)
  }

  def editActor() = Action { implicit request: Request[AnyContent] =>
    Ok("Hello " + name)
  }

}
