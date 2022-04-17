package controllers

import javax.inject._
import play.api._
import play.api.mvc._

@Singleton
class GreetController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  def greet() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.greet())
  }
}
