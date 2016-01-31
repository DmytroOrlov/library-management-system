package controllers

import play.api.mvc.{Action, Controller}

class VisitorController extends Controller {
  def getNewVisitor = Action(Ok("New Visitor"))
}
