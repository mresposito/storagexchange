package com.storagexchange.controllers

import com.storagexchange.views
import com.storagexchange.models._
import com.storagexchange.mails._
import com.storagexchange.utils._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.db._
import play.api.Play.current
import anorm._
import play.api.db.DB
import java.util.UUID
import javax.inject.Singleton
import javax.inject.Inject
import com.typesafe.scalalogging.slf4j.Logging

case class PostRequest(
  description: String,
  storageSize: Int)

case class PostIdRequest(
  postID: Long)

case class PostModifyRequest(
  description: String,
  storageSize: Int,
  postID: Long)

case class SignupRequest(
  myname: String,
  surname: String,
  email: String,
  university: String,
  psw1: String,
  psw2: String)

@Singleton
class Application @Inject()(userStore: UserStore, postStore: PostStore, mailSender: MailSender,
    passwordHasher: PasswordHelper, idHasher: IdHasher) extends Controller 
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
    )

  val newPostForm = Form(
    mapping(
      "description" -> nonEmptyText(minLength = 4),
      "storageSize" -> number(min=0)
      )(PostRequest.apply)(PostRequest.unapply)
    )

  val postModifyInitialForm = Form(
    mapping(
      "postID" -> longNumber(min=0)
      )(PostIdRequest.apply)(PostIdRequest.unapply)
    )

  val postModifyForm = Form(
    mapping(
      "description" -> nonEmptyText(minLength = 4),
      "storageSize" -> number(min=0),
      "postID" -> longNumber(min=0)
      )(PostModifyRequest.apply)(PostModifyRequest.unapply)
    )
  val postDeleteForm = Form(
    mapping(
      "postID" -> longNumber(min=0)
      )(PostIdRequest.apply)(PostIdRequest.unapply)
    )

  def index = Action { implicit request =>
    request.session.get("email").map { username =>
      userStore.getByEmail(username).map { user =>
        Ok(views.html.userIndex(user))
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
  	  newUser => {
  	  	val password = passwordHasher.createPassword(newUser.psw1)
  	  	// FIXME: insert proper university id
        val user = User(newUser.myname, newUser.surname,
          newUser.email, password, 0)
        val userId = userStore.insert(user)
        sendVerificationEmail(user.copy(userId = Some(userId)))
        Redirect(routes.Application.index()).
        	withSession("email" -> newUser.email)
  	  }
  	)
  }

  def newPost = IsAuthenticated { username => implicit request =>
    Ok(views.html.newpost(newPostForm))
  }

  def postReceive = Action { implicit request =>
    val postData = newPostForm.bindFromRequest.get
    request.session.get("email").map { username =>
      postStore.insert(Post(username, postData.description, 
        postData.storageSize))
      Ok(views.html.newpost(newPostForm))
    }.getOrElse { 
      Ok(views.html.index()) 
    }
  }

  def postMyRetreive = IsAuthenticated { username => implicit request =>
    request.session.get("email").map { username =>
      val postList = postStore.getByEmail(username)
      Ok(views.html.myposts(postList))
    }.getOrElse { 
      Ok(views.html.index()) 
    }
  }

  def postViewAll = Action { request =>
    val postList = postStore.getAll()
    Ok(views.html.postboard(postList))
  }

  def postModifyInitial = Action { implicit request =>
    postModifyInitialForm.bindFromRequest.fold(
      // Fix: should go to proper page with errors.
      // Having issue with that however.
      formWithErrors => BadRequest(views.html.error404()),
      modifyRequest => {
        postStore.getById(modifyRequest.postID).map { oldPost =>
          Ok(views.html.modifypost(newPostForm, oldPost))  
        }.getOrElse(Ok(views.html.index()))
      }
    )
  }

  def postModify = Action{ implicit request =>
    postModifyForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.error404()),
      updatedPost => {
        postStore.updateById(updatedPost.postID, updatedPost.description, updatedPost.storageSize)
        Redirect("myposts")
      }
    )
  }

  def postDelete = Action{ implicit request =>
    postDeleteForm.bindFromRequest.fold(
        formWithErrors => Redirect("/myposts"),
        deletedPost => {
          postStore.removeById(deletedPost.postID)
          Redirect("myposts")
        }
      )
  }

  private def sendVerificationEmail(user: User): Unit = {
    val root = Play.current.configuration.getString("website.root")
    val hashedId = idHasher.encrypt(user.userId.get)
    val url = root.getOrElse("http://localhost:9000/")
    val verificationURL = url + "verify/" + hashedId
    val body = views.html.verifyEmail(user.name, verificationURL)
    val recipient = Recipient(user.name, user.email)
    mailSender.send(Message(
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
