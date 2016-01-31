package controllers

import java.util.UUID._

import com.google.inject.Inject
import controllers.UserController._
import controllers.VisitorController._
import dal.NewVisitorRepository
import models.NewVisitor
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

import scala.concurrent.{ExecutionContext, Future}

class VisitorController @Inject()(repo: NewVisitorRepository, val messagesApi: MessagesApi)(implicit ec: ExecutionContext) extends Controller with I18nSupport {
  val newVisitorForm: Form[NewVisitorForm] = Form {
    mapping(
      firstName -> nonEmptyText,
      lastName -> nonEmptyText,
      middleName -> text,
      extraName -> text
    )(NewVisitorForm.apply)(NewVisitorForm.unapply)
  }

  def getNewVisitor = Action { implicit request =>
    request.session.get(visitoruuid).fold(Ok(views.html.newVisitor(newVisitorForm))) { _ =>
      Redirect(routes.Application.index)
    }
  }

  def postNewVisitor() = Action.async { implicit request =>
    newVisitorForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(BadRequest(views.html.newVisitor(errorForm)))
      },
      f => repo.create(NewVisitor(randomUUID, f.firstName, f.lastName, Some(f.middleName), Some(f.extraName))).map { _ =>
        Redirect(routes.Application.index)
          .flashing(flashToUser -> messagesApi(newVisitorApplied))
      }.recover {
        case _ => BadRequest(views.html.newVisitor(newVisitorForm.bindFromRequest
          .withError(firstName, messagesApi(cantCreateNewVisitor))))
      }
    )
  }
}

case class NewVisitorForm(firstName: String, lastName: String, middleName: String, extraName: String)

object VisitorController {
  val firstName = "firstName"
  val lastName = "lastName"
  val middleName = "middleName"
  val extraName = "extraName"

  val newVisitorApplied = "newVisitorApplied"
  val cantCreateNewVisitor = "cantCreateNewVisitor"
}
