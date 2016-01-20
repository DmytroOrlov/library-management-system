package controllers

import java.security.MessageDigest

import com.google.inject.Inject
import com.typesafe.scalalogging.StrictLogging
import controllers.UserController._
import dal.UserRepository
import models.User
import monifu.concurrent.Scheduler
import monifu.reactive.Ack.Continue
import play.api.data.Forms._
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.iteratee.Concurrent
import play.api.mvc._
import sun.misc.BASE64Encoder

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class UserController @Inject()(repo: UserRepository, val messagesApi: MessagesApi)(implicit ec: ExecutionContext) extends Controller with I18nSupport with StrictLogging {

  val registerForm: Form[RegisterForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "password" -> nonEmptyText,
      "verify" -> nonEmptyText
    )(RegisterForm.apply)(RegisterForm.unapply) verifying(passwordsNotMatched, validatePassword _)
  }

  val loginForm: Form[LoginForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginForm.apply)(LoginForm.unapply)
  }

  def getLogin = Action { implicit request =>
    request.session.get(username).fold(Ok(views.html.login(loginForm))) { name =>
      Redirect(routes.Application.index)
        .flashing(flashToUser -> s"$loggedInAs$name")
    }
  }

  def getRegister = Action { implicit request =>
    request.session.get(username).fold(Ok(views.html.register(registerForm))) { _ =>
      Redirect(routes.Application.index)
        .flashing(flashToUser -> alreadyRegistered)
    }
  }

  def postLogin() = Action.async { implicit request =>
    def wrongPassword = BadRequest(
      views.html.login(loginForm.bindFromRequest
        .withError("password", passwordNotMatchTheName)
      )
    )

    loginForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(BadRequest(views.html.login(errorForm)))
      },
      user => {
        repo.passwordFor(user.name).map {
          _.fold(wrongPassword) { hashedAndSalted =>
            val salt = hashedAndSalted.split(",")(1)
            if (hashedAndSalted != passwordHash(user.password, salt)) wrongPassword
            else Redirect(routes.Application.index)
              .flashing(flashToUser -> s"$loggedInAs${user.name}")
              .withSession(username -> user.name)
          }
        }.recover {
          case e =>
            logger.error(e.getMessage, e)
            Ok(views.html.login(loginForm.bindFromRequest
              .withError("password", errorDuringPasswordCheck)))
        }
      }
    )
  }

  def postRegister() = Action.async { implicit request =>
    registerForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(BadRequest(views.html.register(withPasswordMatchError(errorForm))))
      },
      user => {
        val hash = passwordHash(user.password, Random.nextInt().toString)
        repo.createAndGet(User(user.name, hash)).map { r =>
          Redirect(routes.Application.index)
            .withSession(username -> user.name)
            .flashing(flashToUser -> userRegistered)
        }.recover {
          case e => logger.warn(e.getMessage, e)
            Ok(views.html.register(registerForm.bindFromRequest
              .withError("name", nameRegistered)))
        }
      }
    )
  }

  def withPasswordMatchError(errorForm: Form[RegisterForm]) =
    if (errorForm.errors.collectFirst({ case FormError(_, List(`passwordsNotMatched`), _) => true }).nonEmpty)
      errorForm.withError("password", passwordsNotMatched)
    else errorForm

  implicit val scheduler = Scheduler(ec)

  def all = Action { implicit request =>
    request.session.get(username).fold(Redirect(routes.UserController.getRegister)) { _ =>
      Ok.chunked(Concurrent.unicast[User](chan => {
        repo.list().subscribe(next => {
          chan.push(next)
          Continue
        }, error => chan.end(error), () => chan.end())
      }))
    }
  }
}

case class RegisterForm(name: String, password: String, verify: String)

case class LoginForm(name: String, password: String)

object UserController {
  val username = "username"
  val flashToUser = "flashToUser"

  val userRegistered = "Thank you for your registration"
  val alreadyRegistered = "You are already registered"
  val loggedInAs = "Logged in as "
  val nameRegistered = "Name already registered, Please choose another"
  val passwordsNotMatched = "Passwords not matched"
  val passwordNotMatchTheName = "Password not match the name"
  val errorDuringPasswordCheck = "Error during password check"

  def validatePassword(f: RegisterForm) = f.password.equals(f.verify)

  def passwordHash(password: String, salt: String) = {
    val saltedAndHashed: String = password + "," + salt
    val digest: MessageDigest = MessageDigest.getInstance("MD5")
    digest.update(saltedAndHashed.getBytes)
    val encoder: BASE64Encoder = new BASE64Encoder
    val hashedBytes = new String(digest.digest, "UTF-8").getBytes
    encoder.encode(hashedBytes) + "," + salt
  }
}
