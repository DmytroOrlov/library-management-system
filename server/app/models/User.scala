package models

import play.api.http.Writeable
import play.api.libs.json._

case class User(name: String, password: String, id: Option[Int] = None)

object User {
  implicit val format = Json.format[User]

  implicit val writes = Json.writes[User]

  implicit def writable(implicit w: Writeable[JsValue]): Writeable[User] = w.map(writes.writes)
}
