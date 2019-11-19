package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._

@Singleton
class LmsController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  val home = Action {
    Ok(views.html.home())
  }
}
