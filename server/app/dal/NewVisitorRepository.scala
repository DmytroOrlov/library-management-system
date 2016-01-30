package dal

import java.util.UUID
import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NewVisitorRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import driver.api._

  class NewVisitors(tag: Tag) extends Table[UUID](tag, "new_visitor") {
    def uuid = column[UUID]("uuid")

    def * = uuid
  }

  private val newVisitors = TableQuery[NewVisitors]

  def create(uuid: UUID): Future[UUID] = db.run {
    newVisitors += uuid
  }.map(_ => uuid)

  def list() = db.stream(newVisitors.result)
}
