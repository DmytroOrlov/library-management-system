package data

import javax.inject.{Inject, Singleton}

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.google.inject.ImplementedBy
import models.Book
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[BookRepoImpl])
trait BookRepo {
  def add(book: Book): Future[Book]

  def list: Source[Book, NotUsed]
}

@Singleton
class BookRepoImpl @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends BookRepo {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import driver.api._

  class Books(tag: Tag) extends Table[Book](tag, "book") {
    def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def author = column[String]("author")

    def title = column[String]("title")

    def year = column[Int]("year")

    def code = column[String]("code")

    def * = (author, title, year, code, id.?) <>
      ((Book.apply _).tupled, Book.unapply)
  }

  val books = TableQuery[Books]

  def add(book: Book): Future[Book] = db.run {
    (books returning books.map(_.id)
      into ((v, id) => v.copy(id = Some(id)))
      ) += book
  }

  def list: Source[Book, NotUsed] = Source.fromPublisher(
    db.stream(
      books.result.transactionally.withStatementParameters(fetchSize = 1)
    )
  )
}
