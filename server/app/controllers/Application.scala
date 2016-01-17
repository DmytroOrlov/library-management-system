package controllers

import engine.{BackPressuredWebSocketActor, DataProducer, SimpleWebSocketActor}
import monifu.concurrent.Implicits.globalScheduler
import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.mvc._

import scala.concurrent.duration._

object Application extends Controller with JSONFormats {
  def index = Action {
    Ok(views.html.index())
  }

  def register = Action {
    Ok(views.html.register())
  }
}
