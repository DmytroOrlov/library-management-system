package controllers

import javax.inject.Singleton

import play.api.mvc._

@Singleton
class LmsController extends Controller {
  val home = Action {
    Ok(views.html.home())
  }
}
