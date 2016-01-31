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
      name -> nonEmptyText,
      password -> nonEmptyText,
      verify -> nonEmptyText
    )(RegisterForm.apply)(RegisterForm.unapply) verifying(passwordsNotMatched, validatePassword _)
  }

  val loginForm: Form[LoginForm] = Form {
    mapping(
      name -> nonEmptyText,
      password -> nonEmptyText
    )(LoginForm.apply)(LoginForm.unapply)
  }

  def getLogin = Action { implicit request =>
    request.session.get(useruuid).fold(Ok(views.html.login(loginForm))) { _ =>
      Redirect(routes.Application.index)
        .flashing(flashToUser -> messagesApi(youAreLoggedin))
    }
  }

  def getRegister = Action { implicit request =>
    request.session.get(useruuid).fold(Ok(views.html.register(registerForm))) { _ =>
      Redirect(routes.Application.index)
        .flashing(flashToUser -> messagesApi(youAreRegistered))
    }
  }

  def postLogin() = Action.async { implicit request =>
    def wrongPassword = BadRequest(
      views.html.login(loginForm.bindFromRequest
        .withError(password, messagesApi(passwordNotMatchTheName))
      )
    )

    loginForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(BadRequest(views.html.login(errorForm)))
      },
      f => {
        repo.usersBy(f.name).map { us =>
          us.map(u => u -> u.password.split(",")).collectFirst {
            case (user, Array(hash, salt)) if hash == hashSalt(f.password, salt)._1 =>
              redirectWithSession(user)
                .flashing(flashToUser -> messagesApi(youAreLoggedin))
          }.getOrElse(wrongPassword)
        }.recover {
          case e => logger.error(e.getMessage, e)
            Ok(views.html.login(loginForm.bindFromRequest
              .withError(password, messagesApi(errorDuringPasswordCheck))))
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
        case (hash, salt) => repo.createUniqueName(User(randomUUID, form.name, s"$hash,$salt")).map { user =>
          redirectWithSession(user)
            .flashing(flashToUser -> messagesApi(youAreRegistered))
        }.recover {
          case e => logger.error(e.getMessage, e)
            BadRequest(views.html.register(registerForm.bindFromRequest
              .withError(name, messagesApi(nameRegistered))))
        }
      }
    )
  }

  def redirectWithSession(u: User)(implicit request: Request[AnyContent]) = {
    val res = Redirect(routes.Application.index).withSession(useruuid -> u.uuid.toString)
    u.visitorUuid.fold(res) { u =>
      res.addingToSession(visitoruuid -> u.toString)
    }
  }

  def withPasswordMatchError(errorForm: Form[RegisterForm]) =
    if (errorForm.errors.collectFirst({ case FormError(_, List(`passwordsNotMatched`), _) => true }).nonEmpty)
      errorForm.withError(password, messagesApi(passwordsNotMatched))
    else errorForm

  implicit val scheduler = Scheduler(ec)

  def all = Action { implicit request =>
    request.session.get(useruuid).fold(Redirect(routes.UserController.getRegister)) { _ =>
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
  val useruuid = "uuid"
  val visitoruuid = "visitoruuid"
  val flashToUser = "flashToUser"

  val name = "name"
  val password = "password"
  val verify = "verify"

  val youAreRegistered = "youAreRegistered"
  val youAreLoggedin = "youAreLoggedin"
  val nameRegistered = "nameRegistered"
  val passwordsNotMatched = "passwordsNotMatched"
  val passwordNotMatchTheName = "passwordNotMatchTheName"
  val errorDuringPasswordCheck = "errorDuringPasswordCheck"

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
