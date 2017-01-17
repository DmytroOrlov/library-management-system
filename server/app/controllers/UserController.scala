package controllers

import java.security.MessageDigest
import java.util.UUID.randomUUID

import akka.stream.scaladsl.Source
import com.google.inject.Inject
import com.typesafe.config.Config
import controllers.UserController._
import dal.UserRepository
import models.User
import play.api.Logger
import play.api.data.Forms.{email => _, _}
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc._
import sun.misc.BASE64Encoder

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class UserController @Inject()(repo: UserRepository, val messagesApi: MessagesApi, config: Config)(implicit ec: ExecutionContext) extends Controller with I18nSupport {

  val registerForm: Form[RegisterForm] = Form {
    mapping(
      email -> nonEmptyText,
      password -> nonEmptyText,
      verify -> nonEmptyText
    )(RegisterForm.apply)(RegisterForm.unapply) verifying(passwordsNotMatched, validatePassword _)
  }

  val loginForm: Form[LoginForm] = Form {
    mapping(
      email -> nonEmptyText,
      password -> nonEmptyText
    )(LoginForm.apply)(LoginForm.unapply)
  }

  def getLogin = Action { implicit request =>
    request.session.get(useruuid).fold(Ok(views.html.login(loginForm))) { _ =>
      Redirect(routes.LmsApp.index)
        .flashing(flashToUser -> Messages(youAreLoggedinAs, "<>"))
    }
  }

  def getRegister = Action { implicit request =>
    request.session.get(useruuid).fold(Ok(views.html.register(registerForm))) { _ =>
      Redirect(routes.LmsApp.index)
        .flashing(flashToUser -> Messages(youAreRegistered))
    }
  }

  def postLogin() = Action.async { implicit request =>
    def wrongPassword = BadRequest(
      views.html.login(loginForm.bindFromRequest
        .withError(password, Messages(passwordNotMatchEmail))
      )
    )

    loginForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(BadRequest(views.html.login(errorForm)))
      },
      f => {
        repo.usersBy(f.email).map { us =>
          us.map(u => u -> span(u.password)).collectFirst {
            case (user, (hash, salt)) if hash == toHashSalt(f.password, salt)._1 =>
              redirectWithSession(user)
                .flashing(flashToUser -> Messages(youAreLoggedinAs, user.email))
          }.getOrElse(wrongPassword)
        }.recover {
          case e => Logger.error(e.getMessage, e)
            Ok(views.html.login(loginForm.bindFromRequest
              .withError(password, Messages(errorDuringPasswordCheck))))
        }
      }
    )
  }

  def postRegister() = Action.async { implicit request =>
    registerForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(BadRequest(views.html.register(withPasswordMatchError(errorForm))))
      },
      form => toHashSalt(form.password, Random.nextInt().toString) match {
        case (hash, salt) => repo.create(User(randomUUID, form.email, combine(hash, salt))).map { user =>
          redirectWithSession(user)
            .flashing(flashToUser -> Messages(youAreRegistered))
        }.recover {
          case e => Logger.error(e.getMessage, e)
            BadRequest(views.html.register(registerForm.bindFromRequest
              .withError(email, Messages(emailRegistered))))
        }
      }
    )
  }

  def redirectWithSession(u: User)(implicit request: Request[AnyContent]) = {
    val res = Redirect(routes.LmsApp.index).withSession(useruuid -> u.uuid.toString)
    u.visitorUuid.fold(res) { u =>
      res.addingToSession(visitoruuid -> u.toString)
    }
  }

  def withPasswordMatchError(errorForm: Form[RegisterForm]) =
    if (errorForm.errors.collectFirst({ case FormError(_, List(`passwordsNotMatched`), _) => true }).nonEmpty)
      errorForm.withError(password, Messages(passwordsNotMatched))
    else errorForm

  def all = Action { implicit request =>
    request.session.get(useruuid).fold(Redirect(routes.UserController.getRegister)) { _ =>
      Ok.chunked(Source.fromPublisher(repo.list()))
    }
  }
}

case class RegisterForm(email: String, password: String, verify: String)

case class LoginForm(email: String, password: String)

object UserController {
  val useruuid = "uuid"
  val visitoruuid = "visitoruuid"
  val flashToUser = "flashToUser"

  val email = "email"
  val password = "password"
  val verify = "verify"

  val youAreRegistered = "youAreRegistered"
  val youAreLoggedinAs = "youAreLoggedinAs"
  val emailRegistered = "emailRegistered"
  val passwordsNotMatched = "passwordsNotMatched"
  val passwordNotMatchEmail = "passwordNotMatchEmail"
  val errorDuringPasswordCheck = "errorDuringPasswordCheck"

  def validatePassword(f: RegisterForm) = f.password.equals(f.verify)

  val encoder = new BASE64Encoder

  def toHashSalt(password: String, salt: String) = {
    val withSalt = combine(password, salt)
    val digest = MessageDigest.getInstance("MD5")
    digest.update(withSalt.getBytes)
    val hashedBytes = new String(digest.digest, "UTF-8").getBytes
    encoder.encode(hashedBytes) -> salt
  }

  def combine(password: String, salt: String) = password + "," + salt

  def span(hashSalt: String): (String, String) = hashSalt.span(_ != ',') match {
    case (p, s) => p -> s.tail
  }
}
