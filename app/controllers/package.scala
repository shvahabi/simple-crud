import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc.Result
import play.api.mvc.Results.Status
import slick.jdbc.{GetResult, PostgresProfile}
import slick.jdbc.PostgresProfile.api._
import models.Actor

package object controllers {
  implicit val getActors = GetResult[Actor](r => Actor(r.nextInt, r.nextString, r.nextInt))
  implicit def ActorConverter[T](from: T): Vector[Actor] = from.asInstanceOf[Vector[Actor]]

  def query[T <: Product](queryString: String)(implicit getResult: GetResult[T], db: PostgresProfile.backend.Database): Future[Vector[T]] = {
    val queryResult: Future[Vector[T]] = db.run {
      sql"#${queryString}".as[T]
    } andThen {
      case _ => db.close()
    }
    queryResult
  }

  def report[T <: Product](queryResult: Future[Vector[T]], result: Vector[T] => Result)(implicit db: PostgresProfile.backend.Database): Future[Result] = {
    queryResult.failed match {
      case _: Future[NoSuchElementException] => queryResult map { resolvedValue => result(resolvedValue) }
      case throwable: Future[Throwable] => {
        throwable foreach {
          _.printStackTrace()
        }
      }
        Future {
          new Status(503)(views.html.userPrompt("Service unavailable, please try again in a while ..."))
        }
    }
  }

  def insert[T <: Product](queryString: String)(implicit getResult: GetResult[T], db: PostgresProfile.backend.Database): Future[Int] = {
    val queryResult: Future[Int] = db.run {
      sqlu"#${queryString}"
    } andThen {
      case _ => db.close()
    }
    queryResult
  }

}
