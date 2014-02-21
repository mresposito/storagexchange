package com.storagexchange.controllers

import com.storagexchange.views
import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def authorize = Action {
    Ok
  }

}
