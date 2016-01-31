package controllers

import controllers.UserController._
import monifu.concurrent.Implicits.globalScheduler
import engine.{SimpleWebSocketActor, BackPressuredWebSocketActor, DataProducer}
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.Play.current
import concurrent.duration._

class StreamController extends Controller with JSONFormats {
  def streams = Action { implicit request =>
    request.session.get(useruuid).fold(Redirect(routes.UserController.getRegister)) { _ =>
      Ok(views.html.streams())
    }
  }

  def backPressuredStream(periodMillis: Int, seed: Long) =
    WebSocket.acceptWithActor[String, JsValue] { req => out =>
      val obs = new DataProducer(periodMillis.millis, seed)
      BackPressuredWebSocketActor.props(obs, out)
    }

  def simpleStream(periodMillis: Int, seed: Long) =
    WebSocket.acceptWithActor[String, JsValue] { req => out =>
      val obs = new DataProducer(periodMillis.millis, seed)
      SimpleWebSocketActor.props(obs, out)
    }
}
