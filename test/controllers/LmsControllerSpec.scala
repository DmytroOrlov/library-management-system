package controllers

import controllers.LmsControllerSpec.testHomePage
import org.scalatest.MustMatchers
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class LmsControllerSpec extends PlaySpec with MustMatchers {
  "Lms controller" when {
    "takes home page request" should {
      "return it" in testHomePage((new LmsController(stubControllerComponents())).home(FakeRequest()))
    }
  }
}

object LmsControllerSpec extends MustMatchers {
  def testHomePage(res: Future[Result]): Unit = {
    status(res) mustBe OK
    contentType(res) mustBe Some("text/html")
    contentAsString(res) must include("Home")
    contentAsString(res) must include("Зарегистрировать читателя")
    contentAsString(res) must include("Читатели")
    contentAsString(res) must include("Добавить книгу в фонд")
    contentAsString(res) must include("Книги")
  }
}
