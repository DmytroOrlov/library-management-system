package controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import controllers.VisitorController._
import controllers.VisitorControllerSpec.testRegisterVisitorPage
import data.VisitorRepo
import models.Visitor
import org.scalamock.scalatest.MockFactory
import org.scalatest.MustMatchers
import org.scalatestplus.play.PlaySpec
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import util.{MockMessagesApi, MockitoSugar}

import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VisitorControllerSpec extends PlaySpec with MustMatchers with MockitoSugar with MockFactory {
  implicit val mat = ActorMaterializer()(ActorSystem())

  "Visitor controller" when {
    "takes register request" should {
      "return it" in
        testRegisterVisitorPage(
          new VisitorController(mockito[VisitorRepo], mockito[MessagesApi])
            .register(FakeRequest()))
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
      "reject without firstName" in {
        val controller = new VisitorController(mock[VisitorRepo], MockMessagesApi)
        val res = controller.postRegister(
          FakeRequest(POST, "/register").withFormUrlEncodedBody(
            firstName -> "",
            lastName -> "2",
            middleName -> "",
            extraName -> ""
          )
        )
        status(res) mustBe BAD_REQUEST
      }
      "reject without lastName" in {
        val controller = new VisitorController(mock[VisitorRepo], MockMessagesApi)
        val res = controller.postRegister(
          FakeRequest(POST, "/register").withFormUrlEncodedBody(
            firstName -> "1",
            lastName -> "",
            middleName -> "",
            extraName -> ""
          )
        )
        status(res) mustBe BAD_REQUEST
      }
    }
    "takes registered request" should {
      "return registered visitors" in {
        def toJson = (v: Visitor) => Json.toJson(v).toString()
        val vsRepo = mock[VisitorRepo]
        val vs = immutable.Seq(Visitor("1", "2", None, None), Visitor("3", "4", None, None))
        vsRepo.list _ expects() returns Source(vs)
        val controller = new VisitorController(vsRepo, mockito[MessagesApi])
        val res = controller.registered(FakeRequest())
        status(res) mustBe OK
        contentAsString(res) mustBe vs.map(toJson).mkString
      }
    }
  }
}

object VisitorControllerSpec extends MustMatchers {
  def testRegisterVisitorPage(res: Future[Result]): Unit = {
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
