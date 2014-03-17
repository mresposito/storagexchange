package com.storagexchange.controllers

import play.api.test._
import play.api.test.Helpers._
import play.api.db._
import play.api.Play.current
import org.specs2.ScalaCheck
import org.specs2.mutable._
import org.specs2.mutable._
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._
import com.storagexchange.utils.RunningApp

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class SecuredSpec extends Specification with UserTest {
  
  "Secured" should {
    
    "Non authorized user should not get profile page" in RunningApp {
      val Some(page) = route(FakeRequest(GET, "/profile"))
      status(page) must beEqualTo(SEE_OTHER)
    }
    "Non existing user with session should get profile page" in RunningApp {
      val Some(page) = route(FakeRequest(GET, "/profile").
          withSession("email"-> "mah"))
      status(page) must beEqualTo(OK)
    }
    "Existing user should get profile page" in CreateUser {
      val Some(page) = route(FakeRequest(GET, "/profile").
          withSession("email"-> user.email))
      status(page) must beEqualTo(OK)
    }
  }
}
