import controllers.VisitorController
import controllers.VisitorController._
import data.VisitorRepo
import models.Visitor
import org.scalamock.scalatest.MockFactory
import org.scalatest.MustMatchers
import org.scalatestplus.play.PlaySpec
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import util.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VisitorControllerSpec extends PlaySpec with MustMatchers with MockitoSugar with MockFactory {
  "Visitor controller" when {
    "takes register request" should {
      "return it" in {
        val controller = new VisitorController(mockito[VisitorRepo], mockito[MessagesApi])
        val res = controller.register(FakeRequest())

        status(res) mustBe OK
        contentType(res) mustBe Some("text/html")
        contentAsString(res) must include("Регистрация")
        contentAsString(res) must include("form")
        contentAsString(res) must include("firstName")
        contentAsString(res) must include("lastName")
        contentAsString(res) must include("middleName")
        contentAsString(res) must include("extraName")
        contentAsString(res) must include("Зарегистрировать")
      }
    }
    "takes posted visitor" should {
      "add one to db" in {
        val vsRepo = mock[VisitorRepo]
        val fName = "1"
        val lName = "2"
        val visitor = Visitor(fName, lName, None, None)
        vsRepo.add _ expects visitor returns Future.successful(visitor)
        val controller = new VisitorController(vsRepo, mockito[MessagesApi])
        val res = controller.postRegister(
          FakeRequest(POST, "/register").withFormUrlEncodedBody(
            firstName -> fName,
            lastName -> lName,
            middleName -> "",
            extraName -> ""
          )
        )
        status(res) mustBe OK
        contentAsString(res) mustBe Json.toJson(visitor).toString
      }
    }
  }
}
