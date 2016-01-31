package dal

import java.util.UUID
import javax.inject.{Inject, Singleton}

import models.NewVisitor
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NewVisitorRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, val userRepo: UserRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import driver.api._
  import userRepo._

  class NewVisitors(tag: Tag) extends Table[NewVisitor](tag, "new_visitor") {
    def uuid = column[UUID]("uuid", O.PrimaryKey)

    def firstName = column[String]("first_name")

    def lastName = column[String]("last_name")

    def middleName = column[String]("middle_name")

    def extraName = column[String]("extra_name")

    def * = (uuid, firstName, lastName, middleName.?, extraName.?) <>
      (NewVisitor.tupled, NewVisitor.unapply)

    def user = foreignKey("user_fk", uuid, users)(_.uuid)
  }

  private val newVisitors = TableQuery[NewVisitors]

  def create(newVisitor: NewVisitor): Future[NewVisitor] = db.run {
    newVisitors += newVisitor
  }.map(_ => newVisitor)

  def list() = db.stream(newVisitors.result)
}
