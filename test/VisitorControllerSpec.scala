import controllers.VisitorController
import data.VisitorRepo
import org.scalatest.MustMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global

class VisitorControllerSpec extends PlaySpec with MustMatchers with MockitoSugar {
  "Visitor controller" when {
    "takes register request" should {
      "return it" in {
        val controller = new VisitorController(mock[VisitorRepo])
        val res = controller.register(FakeRequest())

        status(res) mustBe OK
        contentType(res) mustBe Some("text/plain")
        contentAsString(res) mustBe "1"
      }
    }
  }
}
