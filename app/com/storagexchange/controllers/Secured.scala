package com.storagexchange.controllers

import play.api._
import play.api.mvc._
import play.api.Play.current

trait Secured extends Controller {

  /**
   * Retrieve the connected user email.
   */
  private def username(request: RequestHeader) = request.session.get("email")
  
  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = {
    Results.Redirect(routes.Application.login)
  }
  
  def IsAuthenticated(f: => String => Request[_] => SimpleResult) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(implicit rs => f(user)(rs))
    }
  }
}