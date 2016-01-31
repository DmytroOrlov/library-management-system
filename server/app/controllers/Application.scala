package controllers

import controllers.Application._
import controllers.UserController._
import play.api.mvc._

class Application extends Controller {
  def index = Action { implicit request =>
    request.session.get(useruuid).fold(Redirect(routes.UserController.getRegister)) { _ =>
      Ok(views.html.index())
    }
  }

  def logout = Action { implicit request =>
    request.session.get(useruuid).fold(Redirect(routes.UserController.getRegister).withNewSession) { _ =>
      Redirect(routes.UserController.getLogin)
        .withNewSession
        .flashing(flashToUser -> logoutDone)
    }
  }
}

object Application {
  val logoutDone = "Logout done"
}
