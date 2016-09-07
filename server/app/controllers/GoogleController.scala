package controllers

import com.github.scribejava.apis.GoogleApi20
import com.github.scribejava.core.builder.ServiceBuilder
import com.google.inject.Inject
import com.typesafe.config.Config
import play.api.mvc.{Action, Controller}

import scala.util.Random

class GoogleController @Inject()(config: Config) extends Controller {
  val clientId = config.getString("google.clientId")
  val clientSecret = config.getString("google.clientSecret")

  def withGoogle = Action {
    val secretState = "secret" + Random.nextInt(999999)
    val service = new ServiceBuilder()
      .apiKey(clientId)
      .apiSecret(clientSecret)
      .scope("email")
      .state(secretState)
      .callback("http://bibliman.com:9000/oauth2callback")
      .build(GoogleApi20.instance)
    val authorizationUrl = {
      import scala.collection.JavaConversions._
      service.getAuthorizationUrl(Map("access_type" -> "offline", "prompt" -> "consent"))
    }
    SeeOther(authorizationUrl)
      .withSession("secretState" -> secretState)
  }

  def oauth2callback(state: String, code: String) = Action { implicit r =>
    r.session.get("secretState").filter(_ == state).fold(BadRequest("bad")) { _ =>
      Ok("ok").removingFromSession("secretState")
    }
  }
}
