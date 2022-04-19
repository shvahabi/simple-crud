package controllers

import play.api.mvc._
import slick.jdbc.GetResult
import slick.jdbc.PostgresProfile.api._

import javax.inject._
import scala.concurrent.Future

@Singleton
class TablesController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  def all(tableName: String) = Action.async { implicit request: Request[AnyContent] => {
    import scala.concurrent.ExecutionContext.Implicits.global
    val db = Database.forConfig("db")
    type myType = (String, String, String)

    def query[T <: (Any, Any, Any)](queryString: String): Future[Vector[T]] = {
      implicit val getResult: GetResult[T] = GetResult(x => (x.nextString(), x.nextString(), x.nextString()).asInstanceOf[T])
      db.run {
        sql"#${queryString}".as[T]
      } andThen {
        case _ => db.close()
      }
    }

    def queryToResult[T <: (Any, Any, Any)](queryString: String, header: List[String]): Future[Result] = {
      val result: Future[Vector[T]] = query(queryString)
      result.failed match {
        case _: Future[NoSuchElementException] => result map { x => Ok(views.html.allRows(x.map(y => y.productIterator.toList.asInstanceOf[List[String]]), header)) }
        case t: Future[Throwable] => {
          t foreach { e => e.printStackTrace() }
        }
          Future { new Status(503)(views.html.userPrompt("Service unavailable, please try again in a while ...")) }
      }
    }

    tableName match {
      case "movies" => queryToResult[myType]("SELECT id, title, year FROM movies", List("ردیف", "عنوان", "سال تولید"))
      case "actors" => queryToResult[myType]("SELECT id, name, birthday FROM actors", List("ردیف", "نام", "سال تولد"))
      case "plays" => queryToResult[myType](
        s"""
           |SELECT actors.name, plays.role, movies.title
           |FROM actors
           |INNER JOIN plays on actors.id = plays.actor
           |INNER JOIN movies on movies.id = plays.movie
           |WHERE actors.birthday > 1971
           |ORDER BY actors.name ASC;
           |""".stripMargin,
        List("بازیگر", "نقش", "فیلم")
      )
      case _ => Future { NotFound(views.html.notFound()) }
    }
  }
  }
}
