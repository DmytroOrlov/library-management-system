package controllers

import javax.inject.{Inject, Singleton}

import controllers.VisitorController._
import data.VisitorRepo
import models.Visitor
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

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

  val registered = Action {
    Ok.chunked(visitors.list)
  }

  val postRegister = Action.async { implicit request =>
    registerForm.bindFromRequest.fold(
    errorForm => {
      Future.successful(BadRequest(views.html.register(errorForm)))
    }, {
      case RegisterForm(f, l, m, e) =>
        def strToOption(s: String) = if (s.isEmpty) None else Some(s)
        visitors.add(Visitor(f, l, strToOption(m), strToOption(e))).map { _ =>
          SeeOther(routes.VisitorController.registered().url)
        }
    })
  }
}

case class RegisterForm(firstName: String, lastName: String, middleName: String, extraName: String)

object VisitorController {
  val firstName = "firstName"
  val lastName = "lastName"
  val middleName = "middleName"
  val extraName = "extraName"
}
