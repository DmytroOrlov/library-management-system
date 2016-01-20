package dal

import javax.inject.{Inject, Singleton}

import models.Person
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
 * A repository for people.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class PersonRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.

  import dbConfig._
  import driver.api._

  /**
   * Here we define the table. It will have a name of people
   */
  private class PeopleTable(tag: Tag) extends Table[Person](tag, "people") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    /** The name column */
    def name = column[String]("name")

    /** The password column */
    def password = column[String]("password")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Person object.
     *
     * In this case, we are simply passing the id, name and page parameters to the Person case classes
     * apply and unapply methods.
     */
    def * = (id, name, password) <>((Person.apply _).tupled, Person.unapply)
  }

  /**
   * The starting point for all queries on the people table.
   */
  private val people = TableQuery[PeopleTable]

  /**
   * Create a person with the given name and password.
   *
   * This is an asynchronous operation, it will return a future of the created person, which can be used to obtain the
   * id for that person.
   */
  def create(name: String, password: String): Future[Person] = db.run {
    // We create a projection of just the name and password columns, since we're not inserting a value for the id column
    (people.map(p => (p.name, p.password))
      // Now define it to return the id, because we want to know what id was generated for the person
      returning people.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((namePassword, id) => Person(id, namePassword._1, namePassword._2))
      // And finally, insert the person into the database
      ) +=(name, password)
  }

  def password(name: String): Future[Option[String]] = db.run(
    people.filter(_.name === name).map(_.password).result
  ).map(_.headOption)

  /**
   * List all the people in the database.
   */
  def list(): Future[Seq[Person]] = db.run {
    people.result
  }
}
