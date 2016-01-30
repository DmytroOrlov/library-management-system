package controllers

import java.security.MessageDigest
import java.util.UUID.randomUUID
import java.util.concurrent.atomic.AtomicReference

import com.google.inject.Inject
import com.typesafe.scalalogging.StrictLogging
import controllers.UserController._
import dal.UserRepository
import models.User
import monifu.concurrent.Scheduler
import monifu.reactive.Ack.{Cancel, Continue}
import monifu.reactive.{Ack, Observable}
import play.api.data.Forms._
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.iteratee._
import play.api.mvc._
import sun.misc.BASE64Encoder

import scala.concurrent.{ExecutionContext, Future, Promise}
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
      f => {
        repo.usersBy(f.name).map { us =>
          us.map(u => u.uuid -> u.password.split(",")).collectFirst {
            case (u, Array(hash, salt)) if hash == hashSalt(f.password, salt)._1 =>
              Redirect(routes.Application.index)
                .withSession(username -> f.name, UserController.uuid -> u.toString)
                .flashing(flashToUser -> s"$loggedInAs${f.name}")
          }.getOrElse(wrongPassword)
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
      form => hashSalt(form.password, Random.nextInt().toString) match {
        case (hash, salt) => repo.create(User(randomUUID, form.name, s"$hash,$salt")).map { u =>
          Redirect(routes.Application.index)
            .withSession(username -> form.name, UserController.uuid -> u.uuid.toString)
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
      Ok.chunked(unicast[User](chan => {
        Observable.fromReactivePublisher(repo.list()/*db.stream(users.result)*/).subscribe(
          next => { chan.push(next) },
          error => chan.end(error),
          () => chan.end()
        )
      }))
    }
  }

  def unicast[E](onStart: Channel[E] => Unit) = new Enumerator[E] {
    def apply[A](it: Iteratee[E, A]) = {
      val kk = new AtomicReference[Option[Input[E] => Iteratee[E, A]]](None)
      val promise = Promise[Iteratee[E, A]]()
      it.pureFold {
        case Step.Cont(k) => kk.set(Some(k))
        case other => promise.success(other.it)
      }

      val pushee = new Channel[E] {
        def end(e: Throwable) = promise.failure(e)

        def end() = kk.get.foreach(k => promise.success(Cont(k)))

        def push(item: Input[E]) = kk.get match {
          case Some(k) =>
            val next = k(item)
            next.pureFold {
              case Step.Cont(k) =>
                kk.set(Some(k))
                Continue
              case _ =>
                promise.success(next)
                Cancel
            }
          case _ => Cancel
        }
      }
      Future(onStart(pushee)).flatMap(_ => promise.future)
    }
  }
}

trait Channel[E] {
  def push(chunk: Input[E]): Future[Ack]

  def push(item: E): Future[Ack] = {
    push(Input.El(item))
  }

  def end(e: Throwable)

  def end()

  def eofAndEnd() {
    push(Input.EOF)
    end()
  }
}

case class RegisterForm(name: String, password: String, verify: String)

case class LoginForm(name: String, password: String)

object UserController {
  val username = "username"
  val uuid = "uuid"
  val flashToUser = "flashToUser"

  val userRegistered = "Thank you for your registration"
  val alreadyRegistered = "You are already registered"
  val loggedInAs = "Logged in as "
  val nameRegistered = "Name already registered, Please choose another"
  val passwordsNotMatched = "Passwords not matched"
  val passwordNotMatchTheName = "Password not match the name"
  val errorDuringPasswordCheck = "Error during password check"

  def validatePassword(f: RegisterForm) = f.password.equals(f.verify)

  val encoder = new BASE64Encoder

  def hashSalt(password: String, salt: String) = {
    val passwordSalt = password + "," + salt
    val digest = MessageDigest.getInstance("MD5")
    digest.update(passwordSalt.getBytes)
    val hashedBytes = new String(digest.digest, "UTF-8").getBytes
    encoder.encode(hashedBytes) -> salt
  }
}
