package models

import play.api.http.Writeable
import play.api.libs.json.{JsValue, Json}

case class Book(author: String, title: String, year: Int, code: String, id: Option[Int] = None)

object Book {
  implicit val format = Json.format[Book]

  implicit def writable(implicit w: Writeable[JsValue]): Writeable[Book] = w.map(format.writes)
}
