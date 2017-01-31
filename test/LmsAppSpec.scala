import org.scalatest.MustMatchers
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.test.FakeRequest
import play.api.test.Helpers._

class LmsAppSpec extends PlaySpec with MustMatchers with OneAppPerSuite {
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
  }
}
