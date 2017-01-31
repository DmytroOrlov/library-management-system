package models

import play.api.http.Writeable
import play.api.libs.json._

case class Visitor(firstName: String, lastName: String, middleName: Option[String], extraName: Option[String], id: Option[Int] = None)

object Visitor {
  implicit val format = Json.format[Visitor]

  implicit def writable(implicit w: Writeable[JsValue]): Writeable[Visitor] = w.map(format.writes)
}
