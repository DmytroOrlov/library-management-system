package controllers

import javax.inject.Singleton

import play.api.mvc._

@Singleton
class LmsApp extends Controller {
  def index = Action {
    Ok("1")
  }
}
