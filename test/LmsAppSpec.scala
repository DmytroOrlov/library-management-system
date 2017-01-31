import data.VisitorRepo
import models.Visitor
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen
import org.scalatest.MustMatchers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.PropertyChecks
import org.scalatest.time.Span._
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.duration._

class LmsAppSpec extends PlaySpec with MustMatchers with OneAppPerSuite with ScalaFutures with PropertyChecks {
  implicit val patience = PatienceConfig(1.second, 10.millis)
  implicit val mat = app.materializer
  val inject = app.injector

  "Lms App" when {
    "takes home page request" should {
      "return it" in {
        val home = route(app, FakeRequest(GET, "/")).get

        status(home) mustBe OK
        contentType(home) mustBe Some("text/html")
        contentAsString(home) must include("Регистрация пользователя")
      }
    }
    "takes register visitor request" should {
      "return it" in {
        val home = route(app, FakeRequest(GET, "/register")).get

        status(home) mustBe OK
        contentType(home) mustBe Some("text/plain")
        contentAsString(home) mustBe "1"
      }
    }
    "register visitor" should {
      "get registered visitor in list" in forAll(visitors) { v =>
        val vsRepo = inject.instanceOf[VisitorRepo]
        val added = vsRepo.add(v).futureValue
        added.id mustBe defined
        added.copy(id = None) mustBe v

        val (folded, _) = vsRepo.list.runFold(false -> Set.empty[Int]) {
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

  val visitors: Gen[Visitor] = for {
    f <- arbitrary[String]
    l <- arbitrary[String]
    m <- Gen.option(arbitrary[String])
    e <- Gen.option(arbitrary[String])
  } yield Visitor(firstName = f, lastName = l, middleName = m, extraName = e)
}
