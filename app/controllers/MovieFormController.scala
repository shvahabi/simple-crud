package controllers

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import models.Movies
import models.forms.{Movie => FormsMovie}

class MovieFormController @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, messagesAction: MessagesActionBuilder, controllerComponents: ControllerComponents)(implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) with HasDatabaseConfigProvider[JdbcProfile] {
  val form: Form[FormsMovie] = Form(
    mapping(
      "title" -> text,
      "year" -> number(min = 0)
    )(FormsMovie.apply)(FormsMovie.unapply)
  )
  val movies = TableQuery[Movies]

  def blankMovieForm(): Action[AnyContent] = messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] => {
    Future {
      Ok(views.html.newMovie(form))
    }
  }
  }

  def newMovie(): Action[AnyContent] = messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] => {
    val successFunction: FormsMovie => Future[Result] = { data => {
      val newMovie = FormsMovie(data.title, data.year)
      db.run {
        movies.map(m => (m.title, m.year)) += (newMovie.title, newMovie.year)
      } map {
        case _: Int => Redirect(routes.MovieReportController.allMovies(Some(1))).flashing("info" -> "اطلاعات فیلم جدید در سیستم ثبت گردید")
      } recoverWith[Result] {
        case t: Throwable => userPrompt(t)
      }
    }
    }
    val errorFunction: Form[FormsMovie] => Future[Result] = { formWithErrors => {
      Future {
        BadRequest(views.html.newMovie(formWithErrors))
      }
    }
    }

    val movieData: Form[FormsMovie] = form.bindFromRequest()
    movieData.fold(errorFunction, successFunction)
  }
  }

  def filledMovieForm(movieId: String): Action[AnyContent] = messagesAction.async { implicit messageRequest: MessagesRequest[AnyContent] => {
    db.run {
      movies.filter(_.id === movieId.asColumnOf[Int]).result.head
    } map {
      v => Map("title" -> v.title, "year" -> v.year.toString)
    } map {
      w => form.bind(w)
    } map {
      case u: Form[FormsMovie] => Ok(views.html.editMovie(u, movieId))
    } recoverWith[Result] {
      case t: Throwable => userPrompt(t)
    }
  }
  }

  def editMovie(movieId: String): Action[AnyContent] = messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] => {
    val successFunction: FormsMovie => Future[Result] = { data => {
      val modifiedMovie = FormsMovie(data.title, data.year)
      db.run {
        movies.filter(_.id === movieId.asColumnOf[Int]).map(a => (a.title, a.year)).update((modifiedMovie.title, modifiedMovie.year))
      } map {
        case _: Int => Redirect(routes.MovieReportController.allMovies(Some(1))).flashing("info" -> "تغییرات اطلاعات فیلم ثبت گردید")
      } recoverWith[Result] {
        case t: Throwable => userPrompt(t)
      }
    }
    }
    val errorFunction: Form[FormsMovie] => Future[Result] = { formWithErrors => {
      Future {
        BadRequest(views.html.editMovie(formWithErrors, movieId))
      }
    }
    }

    val actorData: Form[FormsMovie] = form.bindFromRequest()
    actorData.fold[Future[Result]](errorFunction, successFunction)
  }
  }

  def userPrompt(t: Throwable): Future[Result] = Future {
    t.printStackTrace()
    new Status(503)(views.html.userPrompt("Service unavailable, please try again in a while ..."))
  }

}