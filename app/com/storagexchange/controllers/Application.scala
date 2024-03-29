package com.storagexchange.controllers

import com.storagexchange.views
import com.storagexchange.models._
import com.storagexchange.mails.{Message => EmailMessage, _}
import com.storagexchange.utils._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.db._
import play.api.Play.current
import anorm._
import play.api.db.DB
import javax.inject.Singleton
import javax.inject.Inject
import com.typesafe.scalalogging.slf4j.Logging

case class SignupRequest(
  myname: String,
  surname: String,
  email: String,
  university: String,
  psw1: String,
  psw2: String)

@Singleton
class Application @Inject()(userStore: UserStore, mailSender: MailSender,
    passwordHasher: PasswordHelper, idHasher: IdHasher, universityStore: UniversityStore) extends Controller  
    with Logging with Secured {
  
  
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
      verifying ("User already exists", user => user match {
        case userData => ! userStore.getByEmail(user.email).isDefined  
      })
      verifying ("Enter a valid university", user => user match {
        case userData => universityStore.getUniversityByName(user.university).isDefined
      })
    )

  def index = Action { implicit request =>
    request.session.get("email").map { username =>
      userStore.getByEmail(username).map { user =>
        Ok(views.html.user.index(user))
      }.getOrElse(Ok(views.html.index()))
    }.getOrElse {
      // serve home page
      Ok(views.html.index())
    }
  }
  /**
   * Get login page
   */
  def login = Action {
    Ok(views.html.login(loginForm))
  }
  def logout = Action {
    Redirect(routes.Application.index).withNewSession
  }
  /**
   * Authorize a user if has a good form
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
    Ok(views.html.signup(newUserForm))
  }
  /**
   * signs up a new user
   */
  def registration = Action { implicit request =>
  	newUserForm.bindFromRequest.fold(
  	  formWithErrors => BadRequest(views.html.signup(formWithErrors)),
  	  newUser => insert(newUser)
	  )
  }
  
  private def insert(newUser: SignupRequest) = universityStore.
	  getUniversityByName(newUser.university).map { university =>

    val password = passwordHasher.createPassword(newUser.psw1)
    val user = User(newUser.myname, newUser.surname,
      newUser.email, password, university.id.get)
    val userId = userStore.insert(user)
    // verify the user email
    sendVerificationEmail(user.copy(userId = Some(userId)))
    // redirect to login with his session
    Redirect(routes.Application.index()). 
    	withSession("email" -> newUser.email)

  }.getOrElse {
    logger.error("A university could not be found. Check the inputted name again")
    BadRequest("The university you have specified does not exist")
  }

  private def sendVerificationEmail(user: User): Unit = {
    val root = Play.current.configuration.getString("website.root")
    val hashedId = idHasher.encrypt(user.userId.get)
    val url = root.getOrElse("http://localhost:9000/")
    val verificationURL = url + "verify/" + hashedId
    val body = views.html.verifyEmail(user.name, verificationURL)
    val recipient = Recipient(user.name, user.email)
    mailSender.send(EmailMessage(
      body.toString(),
      "Welcome to Storage Exchange",
      List(recipient)))
  }
  /**
   * Verify a user's email adress
   */
  def verifyEmail(token:String) = Action {
    try {
      val id = idHasher.decryptLong(token)
      if(userStore.verify(id)) {
        userStore.getById(id).map { user =>
          Ok(views.html.thankYouForVerifying(user)).
	        	withSession("email" -> user.email)
        }. getOrElse {
          Ok(views.html.index())   
        }
      } else {
        BadRequest(views.html.error404())
      }
    } catch {
      case e:Exception => {
        logger.info(s"Invalid token: ${token}")
        BadRequest(views.html.error404())
      }
    }
  }

}
