package controllers

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import models.{Movie => ModelMovie}
import models.forms.{Movie => FormsMovie}

class MovieFormController @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, messagesAction: MessagesActionBuilder, controlComponents: ControlComponents)(implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) with HasDatabaseConfigProvider[JdbcProfile] {
  case class Movie(title: String, year: Int)

  val form: Form[Movie] = Form(
    mapping(
      "title" -> text,
      "year" -> number(min = 0)
    )(Movie.apply)(Movie.unapply)
  )

  def blankMovieForm(): Action[AnyContent] = MessagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] => {
    Future {
      Ok(views.html.newMovie(form))
    }
  }
  }

  def newMovie(): Action[AnyContent] = MessagesAction.async { implicit messagesResuest: MessagesResuest[AnyContent] => {

  }
  }

  def filledMovieForm(movieId: String): Action[AnyContent] = MessagesAction.async { implicit messageRequest: MessagesResuest[AnyContent] => {
    db.run {
      sql"#${
        s"""
           |SELECT title, year FROM
           |WHERE id = ${movieId}
           |""".stripMargin
      }".as[ModelMovie]
    } map {
      v => Map("title" -> v.head.title, "year" -> v.head.year.toString)
    } map {
      w => form.bind(w)
    }
  }
  }
}