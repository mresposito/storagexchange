package com.storagexchange.controllers

import com.storagexchange.views
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.db._
import play.api.Play.current
import anorm._
import play.api.db.DB


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
    val conn = DB.getConnection()
      val sqlquery = SQL("""
                        INSERT INTO User(userID,name,email,verifiedEmail,universityID,thumbnail,creationTime,lastLogin) 
                        VALUES (1, "john", "tmp@gmail.com", "true", 1,"hello",3,4); 
                      """)
      
      DB.withConnection { implicit conn =>
        val result: Boolean = sqlquery.execute()
    }
    Ok(views.html.signup(newUserForm))
  }

  def registration = Action { request =>
     
    Ok(views.html.login(loginForm))
  }

  def authorize = Action {
    Ok
  }
}
