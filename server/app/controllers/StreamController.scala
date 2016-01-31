package controllers

import com.google.inject.Inject
import controllers.UserController._
import engine.{BackPressuredWebSocketActor, DataProducer, SimpleWebSocketActor}
import monifu.concurrent.Implicits.globalScheduler
import play.api.Play.current
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc._

import scala.concurrent.duration._

class StreamController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport with JSONFormats {
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
