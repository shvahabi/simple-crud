package controllers

import play.api.mvc._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject()(controllerComponents: ControllerComponents)(implicit exec: ExecutionContext) extends AbstractController(controllerComponents) {
  def landing(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    Future {
      Ok(views.html.navigation())
    }
  }
  }

  def notFound(name: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    Future {
      new Status(404)(views.html.notFound())
    }
  }
  }
}