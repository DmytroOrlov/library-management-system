package controllers

import controllers.BookFundControllerSpec.testAddBookPage
import org.scalatest.MustMatchers
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class BookFundControllerSpec extends PlaySpec {
  "BookFund controller" when {
    "takes add book page request" should {
      "return it" in testAddBookPage((new BookFundController).add(FakeRequest()))
    }
  }
}

object BookFundControllerSpec extends MustMatchers {
  def testAddBookPage(res: Future[Result]): Unit = {
    status(res) mustBe OK
    contentType(res) mustBe Some("text/plain")
    contentAsString(res) mustBe "1"
  }
}
