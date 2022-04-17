package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import slick.driver.PostgresDriver.api._
// import slick.jdbc.JdbcBackend._

@Singleton
class GreetController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  def greet(name: Option[String]) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.greet(s"Hello ${name.getOrElse("Anonymous")}!"))
  }
  def dbGreet() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.greet("Database is configured properly ..."))
  }
}
