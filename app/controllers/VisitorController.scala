package controllers

import javax.inject.{Inject, Singleton}

import controllers.VisitorController._
import data.VisitorRepo
import models.Visitor
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class VisitorController @Inject()(visitors: VisitorRepo, val messagesApi: MessagesApi)(implicit ec: ExecutionContext) extends Controller with I18nSupport {
  val registerForm: Form[RegisterForm] = Form {
    mapping(
      firstName -> nonEmptyText,
      lastName -> nonEmptyText,
      middleName -> text,
      extraName -> text
    )(RegisterForm.apply)(RegisterForm.unapply)
  }

  val register = Action {
    Ok(views.html.register(registerForm))
  }

  val postRegister = Action { implicit request =>
    registerForm.bindFromRequest.fold(
      errorForm => BadRequest(views.html.register(errorForm)),
      f => Ok("1")
    )
  }

  def registerDb(name: String) = Action.async {
    visitors.add(Visitor(firstName = name, lastName = name, middleName = None, extraName = None)).map { v =>
      Ok(v.toString)
    }
  }

  def registered = Action {
    Ok.chunked(visitors.list)
  }
}

case class RegisterForm(firstName: String, lastName: String, middleName: String, extraName: String)

object VisitorController {
  val firstName = "firstName"
  val lastName = "lastName"
  val middleName = "middleName"
  val extraName = "extraName"
}
