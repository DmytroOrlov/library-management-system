import controllers.LmsApp
import org.scalatest.MustMatchers
import org.scalatestplus.play.PlaySpec
import play.api.test.FakeRequest
import play.api.test.Helpers._

class LmsAppControllerSpec extends PlaySpec with MustMatchers {
  "LmsApp controller" when {
    "takes home page request" should {
      "return it" in {
        val controller = new LmsApp
        val res = controller.index(FakeRequest(GET, "/"))

        status(res) mustBe OK
        contentType(res) mustBe Some("text/plain")
        contentAsString(res) mustBe "1"
      }
    }
  }
}
