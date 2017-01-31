import controllers.LmsController
import org.scalatest.MustMatchers
import org.scalatestplus.play.PlaySpec
import play.api.test.FakeRequest
import play.api.test.Helpers._

class LmsControllerSpec extends PlaySpec with MustMatchers {
  "Lms controller" when {
    "takes home page request" should {
      "return it" in {
        val controller = new LmsController
        val res = controller.home(FakeRequest())

        status(res) mustBe OK
        contentType(res) mustBe Some("text/html")
        contentAsString(res) must include("Регистрация пользователя")
      }
    }
  }
}
