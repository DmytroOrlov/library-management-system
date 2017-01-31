package controllers

import javax.inject.{Inject, Singleton}

import data.VisitorRepo
import models.Visitor
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class VisitorController @Inject()(visitors: VisitorRepo)(implicit ec: ExecutionContext) extends Controller {
  val register = Action {
    Ok("1")
  }

  def registerDb(name: String) = Action.async {
    visitors.create(Visitor(name, name, None, None)).map { v =>
      Ok(v.toString)
    }
  }

  def registered = Action {
    Ok.chunked(visitors.list())
  }
}
