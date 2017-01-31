package util

import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.{RequestHeader, Result}
import play.mvc.Http

object MockMessagesApi extends MessagesApi {
  val m = Messages(Lang("en"), this)

  def messages: Map[String, Map[String, String]] = ???

  def preferred(candidates: Seq[Lang]): Messages = ???

  def preferred(request: RequestHeader): Messages = m

  def preferred(request: Http.RequestHeader): Messages = ???

  def langCookieHttpOnly: Boolean = ???

  def clearLang(result: Result): Result = ???

  def langCookieSecure: Boolean = ???

  def langCookieName: String = ???

  def setLang(result: Result, lang: Lang): Result = ???

  def apply(key: String, args: Any*)(implicit lang: Lang): String = key

  def apply(keys: Seq[String], args: Any*)(implicit lang: Lang): String = ???

  def isDefinedAt(key: String)(implicit lang: Lang): Boolean = ???

  def translate(key: String, args: Seq[Any])(implicit lang: Lang): Option[String] = ???
}
