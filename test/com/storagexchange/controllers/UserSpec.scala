package com.storagexchange.controllers

import com.storagexchange.models._
import com.storagexchange.utils._
import play.api.test._
import play.api.test.Helpers._
import play.api.Play.current
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import com.storagexchange.models.UserStore

trait UserTest extends Specification {
    
  val pswHasher = new FakePasswordHelper
  val userStore: UserStore = new UserDAL(pswHasher)

  val SignUp = BeforeHook {
    createUser
  }
  val id = 1
  val password = "12345678"
  val user = User("michele", "esposito", "m@e.com", password, 2)
  val userId = user.copy(userId = Some(id))
  val univ = "Stanford University"
  val invalidUniv = "UIUC"
  def createUser = {
    val Some(create) = route(requestWithSamePasswords(password))
    status(create) must equalTo(SEE_OTHER)
  }
  val CreateUser = BeforeHook {
    createUser
  }
  def createUserRequest(user: User) = genericCreateRequest(user.password, user.password, user, univ)
  def genericCreateRequest(psw1: String, psw2: String, user: User, university: String) = FakeRequest(
    POST,"/signup").withFormUrlEncodedBody(
      "myname" -> user.name,
      "surname" -> user.surname,
      "email" -> user.email,
      "university" -> university,
      "psw1" -> psw1,
      "psw2" -> psw2) 
  def requestWithSamePasswords(psw1: String) = requestWithDifferentPasswords(psw1, psw1)
  def requestWithDifferentPasswords(psw1: String, psw2: String) = genericCreateRequest(psw1, psw2, user, univ)
  def requestWithInvalidUniversity(university: String) = genericCreateRequest(password, password, user, university)
  def requestWithSession(route: String) = FakeRequest(GET, route).withSession(("email", user.email))
}

class UserSpec extends Specification with UserTest {

  "User" should {
    /**
     * Refuse empty form
     */
    "Sign up correctly" in {
      /**
       * Good form!
       */
      "accept valid user" in RunningApp {
        val Some(create) = route(requestWithSamePasswords(password))
        status(create) must equalTo(SEE_OTHER)
      }
      /**
       * Check request with 2 different passwords
       */
      "refuse if different passwords" in RunningApp {
        val Some(create) = route(requestWithDifferentPasswords(password, password.reverse))
        status(create) must equalTo(BAD_REQUEST)
        contentAsString(create) must contain("Passwords must match")
        contentAsString(create) must contain("Storage Exchange")
      }
      /**
        * Check request with an invalid university (UIUC instead of University of Illinois, Urbana Champaign)
        */
      "refuse if invalid university" in RunningApp {
        val Some(create) = route(requestWithInvalidUniversity(invalidUniv))
        status(create) must equalTo(BAD_REQUEST)
        contentAsString(create) must contain("Enter a valid university")
      }
      /**
       * Avoid double sigup
       */
      "refuse if user already exists" in CreateUser {
        val Some(createAgain) = route(requestWithSamePasswords(password))
        status(createAgain) must equalTo(BAD_REQUEST)
        contentAsString(createAgain) must contain("Storage Exchange")
      }
      "refuse should have all information" in CreateUser {
        val Some(createAgain) = route(requestWithSamePasswords(password))
        contentAsString(createAgain) must contain(user.name)
        contentAsString(createAgain) must contain(user.email)
      }

      "not accept a signup form with only username " in RunningApp {
        val Some(create) = route(FakeRequest(POST, "/signup")
          .withFormUrlEncodedBody("username" -> "michele"))
        status(create) must equalTo(BAD_REQUEST)
      }
      "Sign up should redirect to home page" in CreateUser {
        val Some(home) = route(FakeRequest(GET, "/").withSession(("email", user.email)))
        contentAsString(home) must contain("Storage Exchange")
      }
      "Sign up and login" in SignUp {
        val Some(login) = route(FakeRequest(POST, "/login").
          withFormUrlEncodedBody("email" -> user.email, "password" -> password))
        status(login) must beEqualTo(SEE_OTHER)
      }
    }
    "Get pages correctly" in {
      "show error on a wrong login from" in RunningApp {
        val Some(login) = route(FakeRequest(POST, "/login").
          withFormUrlEncodedBody("email" -> user.email, "password" -> "iaosnte"))
        status(login) must beEqualTo(BAD_REQUEST)
        contentAsString(login) must contain("Invalid email or password")
      }
    }
  }
}
