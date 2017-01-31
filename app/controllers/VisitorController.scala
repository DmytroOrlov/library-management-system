package controllers

import javax.inject.Singleton

import play.api.mvc._

@Singleton
class VisitorController extends Controller {
  val register = Action {
    Ok("1")
  }
}
