package controllers

import javax.inject.{Inject, Singleton}

import controllers.BookFundController._
import data.BookRepo
import models.Book
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BookFundController @Inject()(bookRepo: BookRepo, components: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(components) with I18nSupport {
  val addBookForm: Form[AddBookForm] = Form {
    mapping(
      author -> nonEmptyText,
      title -> nonEmptyText,
      year -> number,
      code -> nonEmptyText
    )(AddBookForm.apply)(AddBookForm.unapply)
  }

  val add = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.addBook(addBookForm))
  }

  val books = Action {
    Ok.chunked(bookRepo.list)
  }

  val postBook = Action.async { implicit request: Request[AnyContent] =>
    addBookForm.bindFromRequest.fold(
    errorForm => {
      Future.successful(BadRequest(views.html.addBook(errorForm)))
    }, {
      case AddBookForm(a, t, y, c) =>
        bookRepo.add(Book(a, t, y, c)).map { b =>
          Ok(b)
        }
    })
  }
}

case class AddBookForm(author: String, title: String, year: Int, code: String)

object BookFundController {
  val author = "author"
  val title = "title"
  val year = "year"
  val code = "code"
}
