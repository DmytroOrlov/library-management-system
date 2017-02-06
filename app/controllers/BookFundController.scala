package controllers

import javax.inject.{Inject, Singleton}

import controllers.BookFundController._
import data.BookRepo
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

@Singleton
class BookFundController @Inject()(bookRepo: BookRepo, val messagesApi: MessagesApi) extends Controller with I18nSupport {
  val addBookForm: Form[AddBookForm] = Form {
    mapping(
      author -> nonEmptyText,
      title -> nonEmptyText,
      year -> number,
      code -> nonEmptyText
    )(AddBookForm.apply)(AddBookForm.unapply)
  }

  val add = Action {
    Ok("1")
  }
}

case class AddBookForm(author: String, title: String, year: Int, code: String)

object BookFundController {
  val author = "author"
  val title = "title"
  val year = "year"
  val code = "code"
}
