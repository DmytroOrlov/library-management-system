package controllers

import controllers.Application._
import controllers.RegisterController._
import play.api.mvc._

class Application extends Controller {
  def index = Action { implicit request =>
    request.session.get(username).fold(Redirect(routes.RegisterController.register)) { _ =>
      Ok(views.html.index())
    }
  }

  def logout = Action { implicit request =>
    request.session.get(username).fold(Redirect(routes.RegisterController.register).withNewSession) { _ =>
      Redirect(routes.RegisterController.login)
        .withNewSession
        .flashing(flashToUser -> logoutDone)
    }
  }
}

object Application {
  val logoutDone = "Logout done"
}
