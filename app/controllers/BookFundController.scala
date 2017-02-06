package controllers

import javax.inject.{Inject, Singleton}

import data.BookRepo
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

@Singleton
class BookFundController @Inject()(bookRepo: BookRepo, val messagesApi: MessagesApi) extends Controller with I18nSupport {
  val add = Action {
    Ok("1")
  }
}
