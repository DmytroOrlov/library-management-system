package dal

import java.util.UUID
import javax.inject.{Inject, Singleton}

import models.User
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
 * A repository for users.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class UserRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, val visitorRepo: VisitorRepository)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.

  import dbConfig._
  import driver.api._
  import visitorRepo._

  /**
   * Here we define the table. It will have a email of users
   */
  class Users(tag: Tag) extends Table[User](tag, "lib_user") {

    /** The ID column, which is the primary key, and auto incremented */
    def uuid = column[UUID]("uuid", O.PrimaryKey)

    /** The email column */
    def email = column[String]("email")

    /** The password column */
    def password = column[String]("password")

    def visitorUuid = column[UUID]("visitor_uuid")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the User object.
     *
     * In this case, we are simply passing the id, email and page parameters to the User case classes
     * apply and unapply methods.
     */
    def * = (uuid, email, password, visitorUuid.?) <>
      ((User.apply _).tupled, User.unapply)

    def visitor = foreignKey("visitor_fk", visitorUuid, visitors)(_.uuid)
  }

  /**
   * The starting point for all queries on the users table.
   */
  private[dal] val users = TableQuery[Users]

  def create(user: User): Future[User] = db.run {
    users += user
  }.map(_ => user)

  def passwordFor(email: String): Future[Seq[String]] = db.run(
    filterBy(email).map(_.password).result
  )

  def usersBy(email: String): Future[Seq[User]] = db.run(
    filterBy(email).result
  )

  private def filterBy(email: String) = users.filter(_.email === email)

  /**
   * List all the users in the database.
   */
  def list() = db.stream(
    users.result.transactionally.withStatementParameters(fetchSize = 1)
  )
}
