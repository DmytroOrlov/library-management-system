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
   * Here we define the table. It will have a name of users
   */
  class Users(tag: Tag) extends Table[User](tag, "user") {

    /** The ID column, which is the primary key, and auto incremented */
    def uuid = column[UUID]("uuid", O.PrimaryKey)

    /** The name column */
    def name = column[String]("name")

    /** The password column */
    def password = column[String]("password")

    def visitorUuid = column[UUID]("visitor_uuid")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the User object.
     *
     * In this case, we are simply passing the id, name and page parameters to the User case classes
     * apply and unapply methods.
     */
    def * = (uuid, name, password, visitorUuid.?) <>
      ((User.apply _).tupled, User.unapply)

    def visitor = foreignKey("visitor_fk", visitorUuid, visitors)(_.uuid)
  }

  /**
   * The starting point for all queries on the users table.
   */
  private[dal] val users = TableQuery[Users]

  def createUniqueName(user: User): Future[User] = db.run {
    users.filter(_.name === user.name).result.headOption.flatMap {
      case None => users += user
    }.transactionally
  }.map(_ => user)

  def passwordFor(name: String): Future[Seq[String]] = db.run(
    (for (u <- users if u.name === name) yield u.password).result
  )

  def usersBy(name: String): Future[Seq[User]] = db.run(
    users.filter(_.name === name).result
  )

  /**
   * List all the users in the database.
   */
  def list() = db.stream(users.result)
}
