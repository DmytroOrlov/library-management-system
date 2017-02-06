package controllers

import controllers.BookFundControllerSpec.testAddBookPage
import data.BookRepo
import org.scalatest.MustMatchers
import org.scalatestplus.play.PlaySpec
import play.api.i18n.MessagesApi
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import util.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BookFundControllerSpec extends PlaySpec with MockitoSugar {
  "BookFund controller" when {
    "takes add book page request" should {
      "return it" in testAddBookPage(new BookFundController(mockito[BookRepo], mockito[MessagesApi]).add(FakeRequest()))
    }
  }
}

object BookFundControllerSpec extends MustMatchers {
  def testAddBookPage(res: Future[Result]): Unit = {
    status(res) mustBe OK
    contentType(res) mustBe Some("text/html")
    contentAsString(res) must include("Добавление новой книги")
    contentAsString(res) must include("form")
    contentAsString(res) must include("author")
    contentAsString(res) must include("title")
    contentAsString(res) must include("year")
    contentAsString(res) must include("code")
    contentAsString(res) must include("Добавить в фонд")
  }
}
