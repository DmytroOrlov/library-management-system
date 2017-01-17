package controllers

import java.util.UUID._

import com.github.scribejava.apis.GoogleApi20
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model._
import com.github.scribejava.httpclient.ahc.AhcHttpClientConfig
import com.google.inject.Inject
import com.typesafe.config.Config
import dal.UserRepository
import models.User
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.inject.ApplicationLifecycle
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent._
import scala.util.Random

class GoogleController @Inject()(repo: UserRepository, val messagesApi: MessagesApi, config: Config, lifecycle: ApplicationLifecycle)
                                (implicit ec: ExecutionContext) extends Controller with I18nSupport {
  val clientId = config.getString("google.clientId")
  val clientSecret = config.getString("google.clientSecret")

  ScribeJavaConfig.setForceTypeOfHttpRequests(ForceTypeOfHttpRequest.FORCE_ASYNC_ONLY_HTTP_REQUESTS)
  val secretState = "secret" + Random.nextInt(999999)
  val clientConfig = new AhcHttpClientConfig(new DefaultAsyncHttpClientConfig.Builder()
    .setMaxConnections(5)
    .setRequestTimeout(10000)
    .setPooledConnectionIdleTimeout(1000)
    .setReadTimeout(1000)
    .build)
  val service = {
    val s = new ServiceBuilder()
      .apiKey(clientId)
      .apiSecret(clientSecret)
      .scope("email")
      .state(secretState)
      .callback("http://bibliman.com:9000/oauth2callback")
      .httpClientConfig(clientConfig)
      .build(GoogleApi20.instance)
    lifecycle.addStopHook(() => Future.successful(s.closeAsyncClient()))
    s
  }

  val authorizationUrl = {
    import scala.collection.JavaConversions._
    service.getAuthorizationUrl(Map("access_type" -> "offline", "prompt" -> "consent"))
  }
  val protectedResourceUrl = "https://www.googleapis.com/plus/v1/people/me"

  def withGoogle = Action(SeeOther(authorizationUrl))

  def oauth2callback(state: String, code: String) = Action.async { implicit r =>
    if (state != secretState) Future.successful {
      Redirect(routes.UserController.getRegister)
        .flashing(flashToUser -> "Error login with Google")
    } else {
      val promise = Promise[Result]()
      service.getAccessTokenAsync(code, new OAuthAsyncRequestCallback[OAuth2AccessToken] {
        def onCompleted(accessToken: OAuth2AccessToken) = {
          val request = new OAuthRequestAsync(Verb.GET, protectedResourceUrl, service)
          service.signRequest(accessToken, request)
          request.sendAsync(new OAuthAsyncRequestCallback[Response] {
            def onCompleted(profileResp: Response) = {
              val profile = Json.parse(profileResp.getBody)
              val email = ((profile \ "emails")(0) \ "value").as[String]
              val name = ((profile \ "urls")(0) \ "label").asOpt[String]

              promise.completeWith(repo.createOrUpdate(User(randomUUID, email, accessToken.getRefreshToken)).map { user =>
                val res = Redirect(routes.LmsApp.index)
                  .withSession(useruuid -> user.uuid.toString)
                  .flashing(flashToUser -> Messages(youAreLoggedinAs, name.getOrElse(email)))
                user.visitorUuid.fold(res) { v =>
                  res.addingToSession(visitoruuid -> v.toString)
                }
              })
            }

            def onThrowable(t: Throwable) = promise.failure(t)
          })
        }

        def onThrowable(t: Throwable) = promise.failure(t)
      })
      promise.future
    }
  }
}
