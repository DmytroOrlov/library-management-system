package controllers

import com.google.inject.Inject
import controllers.Application._
import controllers.UserController._
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc._

class Application @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {
  def index = Action { implicit request =>
    Logger.info(request.acceptLanguages.map(_.code).mkString(", "))
    request.session.get(useruuid).fold(Redirect(routes.UserController.getRegister)) { _ =>
      Ok(views.html.index())
    }
  }

  def logout = Action { implicit request =>
    request.session.get(useruuid).fold(Redirect(routes.UserController.getRegister).withNewSession) { _ =>
      Redirect(routes.UserController.getLogin)
        .withNewSession
        .flashing(flashToUser -> Messages(logoutDone))
    }
  }

  def oauth2callback(state: String, code: String) = Action {
    Logger.info(s"$state $code")
    Ok("")
  }
}

object Application {
  val logoutDone = "logoutDone"
}
