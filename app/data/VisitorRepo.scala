package data

import javax.inject.{Inject, Singleton}

import models.Visitor
import play.api.db.slick.DatabaseConfigProvider
import slick.backend.DatabasePublisher
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VisitorRepo @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import driver.api._

  class Visitors(tag: Tag) extends Table[Visitor](tag, "visitor") {
    def id = column[Int]("id", O.PrimaryKey)

    def firstName = column[String]("first_name")

    def lastName = column[String]("last_name")

    def middleName = column[String]("middle_name")

    def extraName = column[String]("extra_name")

    def * = (firstName, lastName, middleName.?, extraName.?, id.?) <>
      ((Visitor.apply _).tupled, Visitor.unapply)
  }

  private[data] val visitors = TableQuery[Visitors]

  def create(visitor: Visitor): Future[Visitor] = db.run {
    visitors += visitor
  }.collect { case 1 => visitor }

  def list(): DatabasePublisher[Visitor] = db.stream(
    visitors.result.transactionally.withStatementParameters(fetchSize = 1)
  )
}
