import controllers.VisitorController
import org.scalatest.MustMatchers
import org.scalatestplus.play.PlaySpec
import play.api.test.FakeRequest
import play.api.test.Helpers._

class VisitorControllerSpec extends PlaySpec with MustMatchers {
  "Visitor controller" when {
    "takes register request" should {
      "return it" in {
        val controller = new VisitorController
        val res = controller.register(FakeRequest())

        status(res) mustBe OK
        contentType(res) mustBe Some("text/plain")
        contentAsString(res) mustBe "1"
      }
    }
  }
}
