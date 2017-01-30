import org.scalatest.{MustMatchers, FlatSpec}
import org.scalatestplus.play.{PlaySpec, OneAppPerSuite}
import play.api.test.FakeRequest
import controllers.LmsApp
import org.scalatestplus.play._
import play.api.test.Helpers._
import play.api.test._

class LmsAppSpec extends PlaySpec with MustMatchers with OneAppPerSuite {

  "LmsApp" when {
    "takes home page request" should {
      "return it" in {
        val home = route(app, FakeRequest(GET, "/")).get

        status(home) mustBe OK
        contentType(home) mustBe Some("text/plain")
        contentAsString(home) mustBe "1"
      }
    }
  }
}
