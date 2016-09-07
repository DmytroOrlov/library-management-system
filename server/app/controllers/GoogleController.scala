package controllers

import com.github.scribejava.apis.GoogleApi20
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.{ForceTypeOfHttpRequest, HttpClient, ScribeJavaConfig}
import com.github.scribejava.core.oauth.OAuth20Service
import com.github.scribejava.httpclient.ahc.AhcHttpClientConfig
import com.google.inject.Inject
import com.typesafe.config.Config
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import play.api.mvc.{Action, Controller}

import scala.util.Random

class GoogleController @Inject()(config: Config) extends Controller {
  val clientId = config.getString("google.clientId")
  val clientSecret = config.getString("google.clientSecret")

  val secretState = "secret" + Random.nextInt(999999)
  ScribeJavaConfig.setForceTypeOfHttpRequests(ForceTypeOfHttpRequest.FORCE_ASYNC_ONLY_HTTP_REQUESTS)
  val clientConfig: HttpClient.Config = new AhcHttpClientConfig(new DefaultAsyncHttpClientConfig.Builder()
    .setMaxConnections(5)
    .setRequestTimeout(10000)
    .setPooledConnectionIdleTimeout(1000)
    .setReadTimeout(1000)
    .build)
  val service: OAuth20Service = new ServiceBuilder()
    .apiKey(clientId)
    .apiSecret(clientSecret)
    .scope("email")
    .state(secretState)
    .callback("http://bibliman.com:9000/oauth2callback")
    .httpClientConfig(clientConfig)
    .build(GoogleApi20.instance)
  val authorizationUrl = {
    import scala.collection.JavaConversions._
    service.getAuthorizationUrl(Map("access_type" -> "offline", "prompt" -> "consent"))
  }

  def withGoogle = Action {
    SeeOther(authorizationUrl)
      .withSession("secretState" -> secretState)
  }

  def oauth2callback(state: String, code: String) = Action { implicit r =>
    r.session.get("secretState")
      .filter(_ == state)
      .fold(BadRequest("bad")) { _ =>
        Ok("ok").removingFromSession("secretState")
      }
  }
}
