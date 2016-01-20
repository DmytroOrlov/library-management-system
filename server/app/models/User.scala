package models

import play.api.http.Writeable
import play.api.libs.json._

case class User(name: String, password: String, id: Option[Int] = None)

object User {
  implicit val userFormat = Json.format[User]

  implicit def userWriteable(implicit w: Writeable[JsValue]): Writeable[User] = w.map(Json.writes[User].writes)
}
