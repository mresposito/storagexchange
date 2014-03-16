package com.storagexchange.models

import com.storagexchange.utils._
import org.specs2.mutable._
import org.specs2.execute.AsResult
import play.api.test._
import play.api.test.Helpers._
import play.api.db._
import play.api.Play.{current => curr}
import java.sql.Timestamp
import org.h2.jdbc.JdbcSQLException
import com.storagexchange.controllers.UserTest

class UserStoreSpec extends Specification with UserTest {
  
  val InsertUser = BeforeHook {
    DB.withConnection { implicit conn =>
    	userStore.insert(user).toInt must beEqualTo(1)
    }
  }
  val InsertVerifiedUser = BeforeHook {
    DB.withConnection { implicit conn =>
    	userStore.insert(user).toInt must beEqualTo(1)
    	userStore.verify(1) must beTrue
    }
  }
  
  "User Store" should {
    "insert a user" in RunningApp {
    	userStore.insert(user).toInt must beEqualTo(1)
    }
    "should trow an exception if inserting user twice" in InsertUser {
    	userStore.insert(user).toInt must throwA[JdbcSQLException]
    }
    "authorize user" in InsertUser {
      userStore.authenticate(user.email, user.password) must beTrue
    }
    "refuse unexisting user" in InsertUser {
      userStore.authenticate("hello", user.password) must beFalse
    }
    "find user by email" in InsertUser {
      userStore.getByEmail(user.email) must beSome(userId)
    }
    "not find an inexistig user" in InsertUser {
      userStore.getByEmail("hello@me") must beNone
    }
    "find user by id" in InsertUser {
      userStore.getById(1) must beSome(userId)
    }
    "not find an inexisting user by id should be none" in InsertUser {
      userStore.getById(3049) must beNone
    }
  }
  
  "Verified User store" should {
    "not retrieve a not verified user by email" in InsertUser {
      userStore.verified.getByEmail(user.email) must beNone
    }
    "not retrieve a not verified user by id" in InsertUser {
      userStore.verified.getById(1) must beNone
    }
    "should verify a user" in InsertUser {
      userStore.verify(1) must beTrue
    }
    "if user does not exist, don't verify him" in RunningApp {
      userStore.verify(0) must beFalse
    }
    "retrieve verified user by email" in InsertVerifiedUser {
      userStore.verified.getByEmail(user.email) must beSome(userId)
    }
    "retrieve verified user by id" in InsertVerifiedUser {
      userStore.verified.getById(1) must beSome(userId)
    }
  }
}
