package models

import play.api.libs.json._

case class User(name: String, password: String, id: Option[Int] = None)

object User {
  implicit val userFormat = Json.format[User]
}
