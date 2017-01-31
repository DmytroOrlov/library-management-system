package data

import javax.inject.{Inject, Singleton}

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.google.inject.ImplementedBy
import models.Visitor
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[VisitorRepoImpl])
trait VisitorRepo {
  def create(visitor: Visitor): Future[Visitor]

  def list(): Source[Visitor, NotUsed]
}

@Singleton
class VisitorRepoImpl @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends VisitorRepo {
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

  val visitors = TableQuery[Visitors]

  def create(visitor: Visitor): Future[Visitor] = db.run {
    visitors += visitor
  }.collect { case 1 => visitor }

  def list(): Source[Visitor, NotUsed] = Source.fromPublisher(
    db.stream(
      visitors.result.transactionally.withStatementParameters(fetchSize = 1)
    )
  )
}
