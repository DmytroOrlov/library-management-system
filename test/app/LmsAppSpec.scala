package app

import controllers.BookFundControllerSpec.testAddBookPage
import controllers.LmsControllerSpec._
import controllers.VisitorController._
import controllers.VisitorControllerSpec._
import data.VisitorRepo
import models.Visitor
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen
import org.scalamock.scalatest.MockFactory
import org.scalatest.MustMatchers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.PropertyChecks
import org.scalatest.time.Span._
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import util.MockitoSugar

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

class LmsAppSpec extends PlaySpec with MustMatchers with OneAppPerSuite with ScalaFutures with PropertyChecks with MockitoSugar with MockFactory {
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
    "takes registered request" should {
      "return registered visitors" in {
        val fName = Random.nextInt().toString
        val lName = Random.nextInt().toString
        register(fName, lName)
        val res = route(app, FakeRequest(GET, "/registered")).get
        status(res) mustBe OK
        val jsons = s"""[${contentAsString(res).replace("}{", "},{")}]"""
        val vs = Json.fromJson[List[Visitor]](Json.parse(jsons)).get
        vs.exists {
          case Visitor(`fName`, `lName`, _, _, _) => true
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
          'firstName (fName),
          'lastName (lName),
          'middleName (None),
          'extraName (None)
        )
      }
    }
    "register visitor" should {
      "get registered visitor in list" in forAll(visitors) { v =>
        val visitorRepo = inject.instanceOf[VisitorRepo]
        val added = visitorRepo.add(v).futureValue
        added.id mustBe defined
        added.copy(id = None) mustBe v

        val (folded, _) = visitorRepo.list.runFold(false -> Set.empty[Int]) {
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
  }

  def register(fName: String, lName: String): Option[Future[Result]] = {
    route(app, FakeRequest(POST, "/register").withFormUrlEncodedBody(
      firstName -> fName,
      lastName -> lName,
      middleName -> "",
      extraName -> ""
    ))
  }

  val visitors: Gen[Visitor] = for {
    f <- arbitrary[String]
    l <- arbitrary[String]
    m <- Gen.option(arbitrary[String])
    e <- Gen.option(arbitrary[String])
  } yield Visitor(firstName = f, lastName = l, middleName = m, extraName = e)
}
