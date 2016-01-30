package dal

import java.util.UUID
import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NewVisitorRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, val userRepo: UserRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import driver.api._

  import userRepo._

  class NewVisitors(tag: Tag) extends Table[UUID](tag, "new_visitor") {
    def uuid = column[UUID]("uuid", O.PrimaryKey)

    def * = uuid

    def user = foreignKey("user_fk", uuid, users)(_.uuid)
  }

  private val newVisitors = TableQuery[NewVisitors]

  def create(uuid: UUID): Future[UUID] = db.run {
    newVisitors += uuid
  }.map(_ => uuid)

  def list() = db.stream(newVisitors.result)
}
