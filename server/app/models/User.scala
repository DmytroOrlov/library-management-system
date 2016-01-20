package models

import play.api.libs.json._

case class User(id: Long, name: String, password: String)

object User {
  implicit val personFormat = Json.format[User]
}