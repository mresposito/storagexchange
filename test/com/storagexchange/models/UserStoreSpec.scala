package com.storagexchange.models

import com.storagexchange.utils._
import org.specs2.mutable._
import org.specs2.execute.AsResult
import play.api.test._
import play.api.test.Helpers._
import play.api.db._
import play.api.Play.{current => curr}
import java.sql.Timestamp

class UserStoreSpec extends Specification {
  val userStore: UserStore = UserDAL

  val password = "123456"
  val user = User("michele", "esposito", "m@e.com", password, 0)
  val userId = user.copy(userId = Some(1))
  
  val InsertUser = BeforeHook {
    DB.withConnection { implicit conn =>
    	userStore.insert(user).toInt must beEqualTo(1)
    }
  }
  
  "User Store" should {
    "insert a user" in RunningApp {
    	userStore.insert(user).toInt must beEqualTo(1)
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
}
