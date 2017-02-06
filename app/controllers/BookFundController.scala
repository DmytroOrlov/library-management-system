package controllers

import javax.inject.Singleton

import play.api.mvc.{Action, Controller}

@Singleton
class BookFundController extends Controller {
  val add = Action {
    Ok("1")
  }
}
