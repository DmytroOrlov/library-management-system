package controllers

import controllers.RegisterController._
import play.api.mvc._

class Application extends Controller {
  def index = Action { implicit request =>
    request.session.get(username).fold(Redirect(routes.RegisterController.register)) { _ =>
      Ok(views.html.index())
    }
  }

  def logout = Action { implicit request =>
    request.session.get(username).fold(newSession) { _ =>
      newSession.flashing(flashToUser -> logout_done)
    }
  }

  def newSession =
    Redirect(routes.RegisterController.register)
      .withNewSession
}
