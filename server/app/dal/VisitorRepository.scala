package dal

import java.util.UUID
import javax.inject.{Inject, Singleton}

import models.Visitor
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VisitorRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, val libRepo: LibraryRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import driver.api._

  import libRepo._

  class Visitors(tag: Tag) extends Table[Visitor](tag, "visitor") {
    def uuid = column[UUID]("uuid", O.PrimaryKey)

    def number = column[Int]("number")

    def libraryUuid = column[UUID]("library_uuid")

    def firstName = column[String]("first_name")

    def lastName = column[String]("last_name")

    def middleName = column[String]("middle_name")

    def extraName = column[String]("extra_name")

    def * = (uuid, number, libraryUuid, firstName, lastName, middleName.?, extraName.?) <>
      (Visitor.tupled, Visitor.unapply)

    def library = foreignKey("library_fk", libraryUuid, libraries)(_.uuid)
  }

  private[dal] val visitors = TableQuery[Visitors]

  def create(visitor: Visitor): Future[Visitor] = db.run {
    visitors += visitor
  }.map(_ => visitor)

  def list() = db.stream(
    visitors.result.transactionally.withStatementParameters(fetchSize = 1)
  )
}
