package com.storagexchange.controllers

import com.storagexchange.views
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import java.util.UUID
import com.storagexchange.mail._ 


case class SignupRequest(
  myname: String,
  surname: String,
  email: String,
  university: String,
  psw1: String,
  psw2: String)

object Application extends Controller {

  val loginForm = Form(
    tuple(
      "email" -> nonEmptyText(minLength = 4),
      "password" -> nonEmptyText(minLength = 6)
    ) verifying ("Invalid email or password", user => user match {
      case userData => true // TODO: put verification logic
    })
  )

  val newUserForm = Form(
    mapping(
      "myname" -> nonEmptyText(minLength = 4),
      "surname" -> nonEmptyText(minLength = 4),
      "email" -> nonEmptyText(minLength = 4),
      "university" -> nonEmptyText,
      "psw1" -> nonEmptyText(minLength = 6),
      "psw2" -> nonEmptyText(minLength = 6)
    )(SignupRequest.apply)(SignupRequest.unapply)
      verifying ("Passwords must match", user => user match {
        case userData => userData.psw1 == userData.psw2
      })
    )

  def index = Action {
    Ok(views.html.index())
  }
  def login = Action {
    Ok(views.html.login(loginForm))
  }
  def signup = Action {
    Ok(views.html.signup(newUserForm))
  }
 
  def registration = Action { request => 
    def emailAddr = request.body.asFormUrlEncoded.get("email")(0)  

    var uuid : String = java.util.UUID.randomUUID.toString
    val mandrill = new MandrillSender
    val m = new MailController(mandrill)
    //TODO: put html in separate file. for some reason wasn't able to read from "verifyEmail.scala.html"
    m.sendVerificationEmail(new Recipient("vignesh", emailAddr),"<html> <head> <meta charset=\"utf-8\" /></head> <a href=" + "\"" + "http://localhost:9000/confirm/" + uuid + "\"" + ">Confirm</a></html>")
    Ok(views.html.login(loginForm))
  }

  def sendAuth(token:String) {
    return 0 
  }
  
  def authenticated(token:String) = Action {
    println(token)
    Ok(views.html.login(loginForm))
  }

  def authorize = Action {
    Ok
  }
}
