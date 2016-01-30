package dal

import java.util.UUID
import javax.inject.{Inject, Singleton}

import models.Library
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LibraryRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import driver.api._

  class Libraries(tag: Tag) extends Table[Library](tag, "library") {
    def uuid = column[UUID]("uuid", O.PrimaryKey)

    def name = column[String]("name")

    def * = (uuid, name) <>
      (Library.tupled, Library.unapply)
  }

  private[dal] val libraries = TableQuery[Libraries]

  def create(library: Library): Future[Library] = db.run {
    libraries += library
  }.map(_ => library)

  def list() = db.stream(libraries.result)
}
