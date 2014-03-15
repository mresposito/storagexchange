package com.storagexchange.controllers

import com.storagexchange.views
import com.storagexchange.models._
import com.storagexchange.mail._ 
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.db._
import play.api.Play.current
import anorm._
import play.api.db.DB
import java.util.UUID

case class PostRequest(
  description: String)

case class SignupRequest(
  myname: String,
  surname: String,
  email: String,
  university: String,
  psw1: String,
  psw2: String)

object Application extends Controller {
  
  val userStore: UserStore = UserDAL

  val loginForm = Form(
    tuple(
      "email" -> nonEmptyText(minLength = 4),
      "password" -> nonEmptyText(minLength = 6)
    ) verifying ("Invalid email or password", user => user match {
      case userData => userStore.authenticate(user._1, user._2)
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

  val newPostForm = Form(
    mapping(
      "description" -> nonEmptyText(minLength = 4)
      )(PostRequest.apply)(PostRequest.unapply)
    )

  def index = Action {
    Ok(views.html.index())
  }
  /**
   * Get login page
   */
  def login = Action {
    Ok(views.html.login(loginForm))
  }
  /**
   * Authorize a user if cas a good form
   */
  def authorize = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login(formWithErrors)),
      user => Redirect(routes.Application.index).withSession("email" -> user._1))
  }

  /**
   * Serve the signup page
   */
  def signup = Action {
    val u = User("michele", "esposito", "m@e.com", "12", 0)
    userStore.insert(u)
    Ok(views.html.signup(newUserForm))
  }
 
  def authenticated(token:String) = Action {
    println(token)
    Ok(views.html.login(loginForm))
  }

  /**
   * signs up a new user
   */
  def registration = Action { implicit request =>
  	newUserForm.bindFromRequest.fold(
  	  formWithErrors => BadRequest(views.html.signup(formWithErrors)),
  	  newUser => {
  	    // TODO: insert password hasher
  	  	val password = newUser.psw1
  	  	// FIXME: insert proper university id
        val user = User(newUser.myname, newUser.surname,
          newUser.email, password, 0)
        val userId = userStore.insert(user)
        Redirect(routes.Application.index()).
        	withSession("email" -> newUser.email)
  	  }
  	)
  }

  def newPost = Action {
    Ok(views.html.newpost(newPostForm))
  }

  def postReceive = Action{ implicit request =>
    val postData = postingForm.bindFromRequest.get
    println(postData.description);
  }
}
