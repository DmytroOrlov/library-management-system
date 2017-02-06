package controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import controllers.BookFundController._
import controllers.BookFundControllerSpec.testAddBookPage
import data.BookRepo
import models.Book
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

class BookFundControllerSpec extends PlaySpec with MustMatchers with MockitoSugar with MockFactory {
  implicit val mat = ActorMaterializer()(ActorSystem())

  "BookFund controller" when {
    "takes add book page request" should {
      "return it" in testAddBookPage(new BookFundController(mockito[BookRepo], mockito[MessagesApi]).add(FakeRequest()))
    }
    "takes posted book" should {
      "add one to db" in {
        val bookRepo = mock[BookRepo]
        val a = "a"
        val t = "t"
        val y = 2017
        val c = "a1"
        val book = Book(a, t, y, c)
        bookRepo.add _ expects book returns Future.successful(book)
        val controller = new BookFundController(bookRepo, mockito[MessagesApi])
        val res = controller.postBook(
          FakeRequest(POST, "/book").withFormUrlEncodedBody(
            author -> a,
            title -> t,
            year -> y.toString,
            code -> c
          )
        )
        status(res) mustBe OK
        contentAsString(res) mustBe Json.toJson(book).toString
      }
      "reject string year" in {
        val controller = new BookFundController(mock[BookRepo], MockMessagesApi)
        val res = controller.postBook(
          FakeRequest(POST, "/book").withFormUrlEncodedBody(
            author -> "a",
            title -> "t",
            year -> "y",
            code -> "c"
          )
        )
        status(res) mustBe BAD_REQUEST
      }
    }
    "takes books request" should {
      "return added bookds" in {
        def toJson = (v: Book) => Json.toJson(v).toString()
        val bookRepo = mock[BookRepo]
        val vs = immutable.Seq(Book("a1", "t1", 2016, "c1"), Book("a2", "t2", 2017, "c2"))
        bookRepo.list _ expects() returns Source(vs)
        val controller = new BookFundController(bookRepo, mockito[MessagesApi])
        val res = controller.books(FakeRequest())
        status(res) mustBe OK
        contentAsString(res) mustBe vs.map(toJson).mkString
      }
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
