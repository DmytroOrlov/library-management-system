package controllers

import com.google.inject.Inject
import controllers.RegisterController._
import dal.PersonRepository
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class RegisterController @Inject()(repo: PersonRepository, val messagesApi: MessagesApi)(implicit ec: ExecutionContext) extends Controller with I18nSupport {

  val personForm: Form[CreatePersonForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "age" -> number.verifying(min(0), max(maxAge))
    )(CreatePersonForm.apply)(CreatePersonForm.unapply)
  }

  def register = Action { implicit request =>
    request.session.get(username).fold(Ok(views.html.register(personForm))) { _ =>
      Redirect(routes.Application.index)
        .flashing(flashToUser -> already_registered)
    }
  }

  def add() = Action.async { implicit request =>
    personForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(BadRequest(views.html.register(errorForm)))
      },
      person => {
        repo.create(person.name, person.age).map { p =>
          Redirect(routes.Application.index)
            .flashing(flashToUser -> user_registered)
            .withSession(username -> p.name)
        }.recover {
          case _ => Ok(views.html.register(personForm.bindFromRequest
            .withError("name", name_registered)))
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

case class CreatePersonForm(name: String, age: Int)

object RegisterController {
  val maxAge = 140
  val username = "username"
  val flashToUser = "flashToUser"

  val user_registered = "Thank you for your registration"
  val logout_done = "Logout done"
  val already_registered = "You are already registered"
  val name_registered = "Name already registered, Please choose another"
}
