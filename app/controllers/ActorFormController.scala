package controllers

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import models.Actors
import models.forms.{Actor => FormsActor}

class ActorFormController @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, messagesAction: MessagesActionBuilder, controllerComponents: ControllerComponents)(implicit exec: ExecutionContext) extends AbstractController(controllerComponents) with HasDatabaseConfigProvider[JdbcProfile] {
  val form: Form[FormsActor] = Form(
    mapping(
      "name" -> text,
      "birthday" -> number(min = 0)
    )(FormsActor.apply)(FormsActor.unapply)
  )
  val actors = TableQuery[Actors]

  def blankActorForm(): Action[AnyContent] = messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] => {
    Future {
      Ok(views.html.newActor(form))
    }
  }
  }

  def newActor(): Action[AnyContent] = messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] => {
    val successFunction: FormsActor => Future[Result] = { data => {
      val newActor = FormsActor(data.name, data.birthday)
      db.run {
        actors.map(a => (a.name, a.birthday)) += (newActor.name, newActor.birthday)
      } map {
        case _: Int => Redirect(routes.ActorReportController.allActors(Some(1))).flashing("info" -> "اطلاعات بازیگر جدید در سیستم ثبت گردید")
      } recoverWith[Result] {
        case t: Throwable => userPrompt(t)
      }
    }
    }
    val errorFunction: Form[FormsActor] => Future[Result] = { formWithErrors => {
      Future {
        BadRequest(views.html.newActor(formWithErrors))
      }
    }
    }

    val actorData: Form[FormsActor] = form.bindFromRequest()
    actorData.fold(errorFunction, successFunction)
  }
  }

  def filledActorForm(actorId: String): Action[AnyContent] = messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] => {
    db.run {
      actors.filter(_.id === actorId.asColumnOf[Int]).result.head
    } map {
      resolvedValue => Map("name" -> resolvedValue.name, "birthday" -> resolvedValue.birthday.toString)
    } map {
      resolvedValue => form.bind(resolvedValue)
    } map {
      case value: Form[models.forms.Actor] => Ok(views.html.editActor(value, actorId))
    } recoverWith[Result] {
      case t: Throwable => userPrompt(t)
    }
  }
  }

  def editActor(actorId: String): Action[AnyContent] = messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] => {
    val successFunction: FormsActor => Future[Result] = { data => {
      val modifiedActor = FormsActor(data.name, data.birthday)
      db.run {
        actors.filter(_.id === actorId.asColumnOf[Int]).map(a => (a.name, a.birthday)).update((modifiedActor.name, modifiedActor.birthday))
      } map {
        case _: Int => Redirect(routes.ActorReportController.allActors(Some(1))).flashing("info" -> "تغییرات اطلاعات بازیگر ثبت گردید")
      } recoverWith[Result] {
        case t: Throwable => userPrompt(t)
      }
    }
    }
    val errorFunction: Form[FormsActor] => Future[Result] = { formWithErrors => {
      Future {
        BadRequest(views.html.editActor(formWithErrors, actorId))
      }
    }
    }

    val actorData: Form[FormsActor] = form.bindFromRequest()
    actorData.fold[Future[Result]](errorFunction, successFunction)
  }
  }

  def userPrompt(t: Throwable): Future[Result] = Future {
    t.printStackTrace()
    new Status(503)(views.html.userPrompt("Service unavailable, please try again in a while ..."))
  }

}
