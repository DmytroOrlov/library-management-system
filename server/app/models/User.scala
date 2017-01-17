package models

import java.util.UUID

case class User(uuid: UUID, email: String, password: String, visitorUuid: Option[UUID] = None)

object User {
  import play.api.http.Writeable
  import play.api.libs.json._
  implicit val format = Json.format[User]
  implicit def writable(implicit w: Writeable[JsValue]): Writeable[User] = w.map(format.writes)
}

case class Library(uuid: UUID, name: String)

case class Visitor(uuid: UUID, number: Int, libraryUuid: UUID, firstName: String, lastName: String, middleName: Option[String], extraName: Option[String])

case class NewVisitor(uuid: UUID, firstName: String, lastName: String, middleName: Option[String], extraName: Option[String])
