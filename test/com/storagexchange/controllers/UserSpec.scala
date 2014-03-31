package com.storagexchange.controllers

import org.mockito.Mockito.{mock, when}
import com.storagexchange.models._
import com.storagexchange.utils._
import play.api.test._
import play.api.test.Helpers._
import play.api.Play.current
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import com.storagexchange.models.UserStore
import java.sql.Timestamp
import play.api.db._
import java.math.BigDecimal

trait UserTest extends Specification {
    
  val today = new Timestamp(600)
  val tomorrow = new Timestamp(100430600)
  val clock = mock(classOf[Clock])
  // mock the class
  when(clock.now).thenReturn(today)
  val pswHasher = new FakePasswordHelper
  val userStore: UserStore = new UserDAL(pswHasher, clock)
  val universityStore: UniversityStore = new UniversityDAL()
  val locationStore: LocationStore = new LocationDAL()

  val SignUp = BeforeHook {
    createUser
  }
  val id = 1
  val now = Some(clock.now)
  val password = "12345678"
  val user = User("michele", "esposito", "m@e.com", password, 1, now, now)
  val userId = user.copy(userId = Some(id))
  val univ = "Stanford University"
  val invalidUniv = "Stanford"
  val y = new BigDecimal(37.000000).setScale(6,BigDecimal.ROUND_HALF_UP)
  val z = new BigDecimal(122.000000).setScale(6,BigDecimal.ROUND_HALF_UP)
  val testLoc = Location("Stanford University", y, z, "Stanford", "California", "450 Serra Mall", "94305", None)
  val testUniv = University(1,"Stanford University", "http://www.stanford.edu", 
                            "http://upload.wikimedia.org/wikipedia/en/b/b7/Stanford_University_seal_2003.svg",
                            "Cardinal, White", None)
  def insertUniversityLocation = {
    locationStore.insert(testLoc)
    universityStore.insert(testUniv)
  }
  val InsertUniversityLocation = BeforeHook {
    DB.withConnection { implicit conn =>
      insertUniversityLocation
    }
  }
  def createUser = {
    insertUniversityLocation
    val Some(create) = route(requestWithSamePasswords(password))
    status(create) must beEqualTo(SEE_OTHER)
  }
  val CreateUser = BeforeHook {
    createUser
  }

  def createUserRequest(user: User) = genericCreateRequest(user.password, user.password, user, univ)
  def genericCreateRequest(psw1: String, psw2: String, user: User, university: String) = FakeRequest(
    POST,routes.Application.signup.url).withFormUrlEncodedBody(
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
  
  //createUser

  "User" should {
    /**
     * Refuse empty form
     */
    "Sign up correctly" in {
      /**
       * Good form!
       */
      "accept valid user" in InsertUniversityLocation {
        val Some(create) = route(requestWithSamePasswords(password))
        status(create) must equalTo(SEE_OTHER)
      }
      /**
       * Check request with 2 different passwords
       */
      "refuse if different passwords" in InsertUniversityLocation {
        val Some(create) = route(requestWithDifferentPasswords(password, password.reverse))
        status(create) must equalTo(BAD_REQUEST)
        contentAsString(create) must contain("Passwords must match")
        contentAsString(create) must contain("Storage Exchange")
      }
      /**
        * Check request with an invalid university (UIUC instead of University of Illinois, Urbana Champaign)
        */
      "refuse if invalid university" in InsertUniversityLocation {
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

      "not accept a signup form with only username " in InsertUniversityLocation {
        val Some(create) = route(FakeRequest(POST, routes.Application.signup.url)
          .withFormUrlEncodedBody("username" -> "michele"))
        status(create) must equalTo(BAD_REQUEST)
      }
      "Sign up should redirect to home page" in CreateUser {
        val Some(home) = route(FakeRequest(GET, routes.Application.index.url).withSession(("email", user.email)))
        contentAsString(home) must contain("Storage Exchange")
      }
      "Sign up and login" in SignUp {
        val Some(login) = route(FakeRequest(POST, routes.Application.login.url).
          withFormUrlEncodedBody("email" -> user.email, "password" -> password))
        status(login) must beEqualTo(SEE_OTHER)
      }
      "Log out should have no session" in SignUp {
        val Some(login) = route(FakeRequest(GET, routes.Application.logout.url).
          withFormUrlEncodedBody("email" -> user.email, "password" -> password))
        status(login) must beEqualTo(SEE_OTHER)
        session(login).isEmpty must beTrue
      }
    }
    "Get pages correctly" in {
      "show error on a wrong login from" in InsertUniversityLocation {
        val Some(login) = route(FakeRequest(POST, routes.Application.login.url).
          withFormUrlEncodedBody("email" -> user.email, "password" -> "iaosnte"))
        status(login) must beEqualTo(BAD_REQUEST)
        contentAsString(login) must contain("Invalid email or password")
      }
      "Get user home after sigin up" in CreateUser {
        val Some(home) = route(FakeRequest(GET, routes.Application.index.url).
          withSession("email" -> user.email))
        status(home) must beEqualTo(OK)
        contentAsString(home) must contain(user.name)
      }
      "Get user profile after sigin up" in CreateUser {
        val Some(profile) = route(FakeRequest(GET, routes.Dynamic.profile.url).
          withSession("email" -> user.email))
        status(profile) must beEqualTo(OK)
        contentAsString(profile) must contain(user.name)
        contentAsString(profile) must contain("Profile")
      }
    }
  }
}
