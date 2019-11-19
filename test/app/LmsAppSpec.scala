package app

import controllers.BookFundController._
import controllers.BookFundControllerSpec.testAddBookPage
import controllers.LmsControllerSpec._
import controllers.VisitorController._
import controllers.VisitorControllerSpec._
import data.{BookRepo, VisitorRepo}
import models.{Book, Visitor}
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen
import org.scalamock.scalatest.MockFactory
import org.scalatest.MustMatchers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.PropertyChecks
import org.scalatest.time.Span._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import util.MockitoSugar

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

class LmsAppSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with PropertyChecks with MockitoSugar with MockFactory {
  implicit val patience = PatienceConfig(1.second, 20.millis)
  implicit val mat = app.materializer
  val inject = app.injector

  "Lms App" when {
    "takes home page request" should {
      "return it" in testHomePage(route(app, FakeRequest(GET, "/")).get)
    }
    "takes register visitor request" should {
      "return it" in testRegisterVisitorPage(route(app, FakeRequest(GET, "/register")).get)
    }
    "takes add book request" should {
      "return it" in testAddBookPage(route(app, FakeRequest(GET, "/book")).get)
    }
    "takes visitors request" should {
      "return registered visitors" in {
        val fName = Random.nextInt().toString
        val lName = Random.nextInt().toString
        register(fName, lName).get.futureValue
        val res = route(app, FakeRequest(GET, "/visitors")).get
        status(res) mustBe OK
        val jsons = s"""[${contentAsString(res).replace("}{", "},{")}]"""
        val vs = Json.fromJson[List[Visitor]](Json.parse(jsons)).get
        vs.exists {
          case Visitor(`fName`, `lName`, _, _, Some(_)) => true
          case _ => false
        } mustBe true
      }
    }
    "takes books request" should {
      "return added books" in {
        val a = Random.nextInt().toString
        val t = Random.nextInt().toString
        val y = Random.nextInt()
        val c = Random.nextInt().toString
        addBook(a, t, y.toString, c).get.futureValue
        val res = route(app, FakeRequest(GET, "/books")).get
        status(res) mustBe OK
        val jsons = s"""[${contentAsString(res).replace("}{", "},{")}]"""
        val vs = Json.fromJson[List[Book]](Json.parse(jsons)).get
        vs.exists {
          case Book(`a`, `t`, `y`, `c`, Some(_)) => true
          case _ => false
        } mustBe true
      }
    }
    "takes posted visitor" should {
      "add one to db" in {
        val fName = Random.nextInt().toString
        val lName = Random.nextInt().toString
        val res = register(fName, lName).get
        status(res) mustBe OK
        Json.fromJson[Visitor](Json.parse(contentAsString(res))).get must have(
          'firstName(fName),
          'lastName(lName),
          'middleName(None),
          'extraName(None)
        )
      }
    }
    "takes posted book" should {
      "add one to db" in {
        val a = Random.nextInt().toString
        val t = Random.nextInt().toString
        val y = Random.nextInt()
        val c = Random.nextInt().toString
        val res = addBook(a, t, y.toString, c).get
        status(res) mustBe OK
        Json.fromJson[Book](Json.parse(contentAsString(res))).get must have(
          'author(a),
          'title(t),
          'year(y),
          'code(c)
        )
      }
    }
    "register visitor" should {
      "get registered visitor in list" in forAll(visitors) { v =>
        val visitorRepo = inject.instanceOf[VisitorRepo]
        val added = visitorRepo.add(v).futureValue
        added.id mustBe defined
        added.copy(id = None) mustBe v

        val (folded, _) = visitorRepo.list.runFold(false -> Set.empty[Long]) {
          case ((_, ids), Visitor(v.firstName, v.lastName, v.middleName, v.extraName, Some(id))) =>
            ids.contains(id) mustBe false
            true -> (ids + id)
          case ((res, ids), Visitor(_, _, _, _, Some(id))) =>
            ids.contains(id) mustBe false
            res -> (ids + id)
        }.futureValue
        folded mustBe true
      }
    }
    "add book" should {
      "get added book in list" in forAll(books) { b =>
        val bookRepo = inject.instanceOf[BookRepo]
        val added = bookRepo.add(b).futureValue
        added.id mustBe defined
        added.copy(id = None) mustBe b

        val (folded, _) = bookRepo.list.runFold(false -> Set.empty[Long]) {
          case ((_, ids), Book(b.author, b.title, b.year, b.code, Some(id))) =>
            ids.contains(id) mustBe false
            true -> (ids + id)
          case ((res, ids), Book(_, _, _, _, Some(id))) =>
            ids.contains(id) mustBe false
            res -> (ids + id)
        }.futureValue
        folded mustBe true
      }
    }
  }

  def register(fName: String, lName: String): Option[Future[Result]] = {
    route(app, FakeRequest(POST, "/register").withFormUrlEncodedBody(
      firstName -> fName,
      lastName -> lName,
      middleName -> "",
      extraName -> ""
    ))
  }

  def addBook(a: String, t: String, y: String, c: String): Option[Future[Result]] = {
    route(app, FakeRequest(POST, "/book").withFormUrlEncodedBody(
      author -> a,
      title -> t,
      year -> y,
      code -> c
    ))
  }

  val visitors: Gen[Visitor] = for {
    firstName <- arbitrary[String]
    lastName <- arbitrary[String]
    optMiddleName <- Gen.option(arbitrary[String])
    optExtraName <- Gen.option(arbitrary[String])
  } yield Visitor(firstName, lastName, optMiddleName, optExtraName)

  val books: Gen[Book] = for {
    author <- arbitrary[String]
    title <- arbitrary[String]
    year <- Gen.choose(1000, 2100)
    code <- arbitrary[String]
  } yield Book(author, title, year, code)
}
