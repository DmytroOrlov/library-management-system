package models

import java.util.UUID

import play.api.http.Writeable
import play.api.libs.json._

case class User(uuid: UUID, name: String, password: String, visitorUuid: Option[UUID] = None)

object User {
  implicit val format = Json.format[User]

  implicit val writes = Json.writes[User]

  implicit def writable(implicit w: Writeable[JsValue]): Writeable[User] = w.map(writes.writes)
}
