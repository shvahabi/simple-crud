package controllers

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc._
import slick.jdbc.{GetResult, JdbcProfile}
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import models.Movie

class MovieReportController @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, controllerComponents: ControllerComponents)(implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) with HasDatabaseConfigProvider[JdbcProfile] {

  def allMovies(page: Option[Int] = Some(1)): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    val pageLimit: Int = 5

    page match {
      case Some(0) => {
        db.run {
          sql"#${
            s"""
               |SELECT * FROM movies
               |ORDER BY title;
               |""".stripMargin
          }".as[Movie]
        } map {
          v => {
            val movies = v
            Ok(views.html.someMovies(movies, 0, 0))
          }
        } recoverWith[Result] {
          case t: Throwable => userPrompt(t)
        }
      }
      case _ => {
        db.run {
          sql"#${
            s"""
               |SELECT * FROM movies
               |ORDER BY title
               |LIMIT ${pageLimit}
               |OFFSET ${(page.getOrElse(1) - 1) * pageLimit};
               |""".stripMargin
          }".as[Movie]
        } zip {
          db.run {
            sql"#${
              s"""
                 |SELECT COUNT(*) FROM movies;
                 |""".stripMargin
            }".as[Int].head
          }
        } map {
          v => {
            val (movies: Vector[Movie], total: Int) = v
            Ok(views.html.someMovies(movies, page.getOrElse(1), Math.ceil(total / pageLimit.toFloat).toInt))
          }
        }
      }
    }
  }
  }

  def singleMovie(movieId: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    db.run {
      sql"#${
        s"""
           |SELECT * FROM movies
           |WHERE id = {$movieId};
           |""".stripMargin
      }".as[Movie].head
    } map {
      v => {
        val movie: Movie = v
        Ok(views.html.singleMovie(movie))
      }
    } recoverWith[Result] {
      case t: Throwable => userPrompt(t)
    }
  }
  }

  def deleteMovie(movieId: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    db.run {
      sqlu"#${
        s"""
           |DELETE FROM movies
           |WHERE id = ${movieId}
           |""".stripMargin
      }"
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