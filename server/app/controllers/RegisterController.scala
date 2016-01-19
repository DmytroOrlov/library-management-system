package controllers

import java.security.MessageDigest

import com.google.inject.Inject
import com.typesafe.scalalogging.StrictLogging
import controllers.RegisterController._
import dal.PersonRepository
import play.api.data.Forms._
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import sun.misc.BASE64Encoder

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class RegisterController @Inject()(repo: PersonRepository, val messagesApi: MessagesApi)(implicit ec: ExecutionContext) extends Controller with I18nSupport with StrictLogging {

  val personForm: Form[CreatePersonForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "password" -> nonEmptyText,
      "verify" -> nonEmptyText
    )(CreatePersonForm.apply)(CreatePersonForm.unapply) verifying(passwordNotMatched, validatePassword _)
  }

  def register = Action { implicit request =>
    request.session.get(username).fold(Ok(views.html.register(personForm))) { _ =>
      Redirect(routes.Application.index)
        .flashing(flashToUser -> alreadyRegistered)
    }
  }

  def add() = Action.async { implicit request =>
    personForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(BadRequest(views.html.register(
          if (errorForm.errors.collectFirst({ case FormError(_, List(RegisterController.passwordNotMatched), _) => true }).nonEmpty)
            errorForm.withError("password", passwordNotMatched)
          else errorForm
        )))
      },
      person => {
        val hash = passwordHash(person.password, Random.nextInt().toString)
        repo.create(person.name, hash).map { p =>
          Redirect(routes.Application.index)
            .flashing(flashToUser -> userRegistered)
            .withSession(username -> p.name)
        }.recover {
          case e =>
            logger.error(e.getMessage, e)
            Ok(views.html.register(personForm.bindFromRequest
              .withError("name", nameRegistered)))
        }
      }
    )
  }

  def get = Action.async {
    repo.list().map { people =>
      Ok(Json.toJson(people))
    }
  }
}

case class CreatePersonForm(name: String, password: String, verify: String)

object RegisterController {
  val username = "username"
  val flashToUser = "flashToUser"

  val userRegistered = "Thank you for your registration"
  val alreadyRegistered = "You are already registered"
  val nameRegistered = "Name already registered, Please choose another"
  val passwordNotMatched = "Passwords not matched"

  def validatePassword(f: CreatePersonForm) = f.password.equals(f.verify)

  def passwordHash(password: String, salt: String) = {
    val saltedAndHashed: String = password + "," + salt
    val digest: MessageDigest = MessageDigest.getInstance("MD5")
    digest.update(saltedAndHashed.getBytes)
    val encoder: BASE64Encoder = new BASE64Encoder
    val hashedBytes = new String(digest.digest, "UTF-8").getBytes
    encoder.encode(hashedBytes) + "," + salt
  }
}
