package controllers

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc._
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import models.{Movie, Movies}

class MovieReportController @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, controllerComponents: ControllerComponents)(implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) with HasDatabaseConfigProvider[JdbcProfile] {
  val movies = TableQuery[Movies]
  def allMovies(page: Option[Int] = Some(1)): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    val pageLimit: Int = 5

    page match {
      case Some(0) => {
        db.run {
          movies.sortBy(_.title.asc).result
        } map {
          unresolvedValue => {
            val movies: Vector[Movie] = unresolvedValue.toVector
            Ok(views.html.someMovies(movies, 0, 0))
          }
        } recoverWith[Result] {
          case t: Throwable => userPrompt(t)
        }
      }
      case _ => {
        db.run {
          movies.sortBy(_.title.asc).drop((page.getOrElse(1) - 1) * pageLimit).take(pageLimit).result zip movies.length.result
        } map {
          unresolvedValue => {
            val moviesRecords: Vector[Movie] = unresolvedValue._1.toVector
            val moviesCount: Int = unresolvedValue._2
            Ok(views.html.someMovies(moviesRecords, page.getOrElse(1), Math.ceil(moviesCount / pageLimit.toFloat).toInt))
          }
        } recoverWith[Result] {
          case t: Throwable => userPrompt(t)
        }
      }
    }
  }
  }

  def singleMovie(movieId: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    db.run {
      movies.filter(_.id ===movieId.asColumnOf[Int]).result.head
    } map {
      unresolvedValue => {
        val movie: Movie = unresolvedValue
        Ok(views.html.singleMovie(movie))
      }
    } recoverWith[Result] {
      case t: Throwable => userPrompt(t)
    }
  }
  }

  def deleteMovie(movieId: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    db.run {
      movies.filter(_.id === movieId.asColumnOf[Int]).delete
    } map {
      _ => Redirect(routes.MovieReportController.allMovies(Some(1)))
    } recoverWith[Result] {
      case t: Throwable => userPrompt(t)
    }
  }
  }

  def userPrompt(t: Throwable): Future[Result] = Future {
    t.printStackTrace()
    new Status(503)(views.html.userPrompt("Service unavailable, please try again in a while ..."))
  }
}