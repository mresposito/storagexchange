package com.storagexchange.controllers

import com.storagexchange.commons.Global
import com.storagexchange.models._
import com.storagexchange.utils._
import play.api.test._
import play.api.test.Helpers._
import play.api.Play.current
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class VerifyUserSpec extends Specification with UserTest {
  def createUser = {
    insertUniversityLocation
    val Some(create) = route(requestWithSamePasswords(password))
    status(create) must beEqualTo(SEE_OTHER)
  }
  val CreateUser = BeforeHook {
    createUser
  }
  val idHasher: IdHasher = new FakeIdHasher
  val hashedId = idHasher.encrypt(id)
  val verifyUrl = s"/verify/${hashedId}"
  val VerifyUser = BeforeHook {
    createUser
    val Some(register) = route(FakeRequest(GET, verifyUrl))
    status(register) must beEqualTo(OK)
  }

  "Verify User" should {
    "be able to get verification page" in CreateUser {
      val Some(register) = route(FakeRequest(GET, verifyUrl))
      status(register) must beEqualTo(OK)
    } 
    "verification page should contain name" in CreateUser {
      val Some(register) = route(FakeRequest(GET, verifyUrl))
      contentAsString(register) must contain(user.name)
    }
    "verification page for non extisting user shoud be bad" in RunningApp {
      val Some(register) = route(FakeRequest(GET, verifyUrl))
      status(register) must beEqualTo(BAD_REQUEST)
    }
    "A register user should be in the database" in CreateUser {
      userStore.getById(1) should beSome(userId)
    }
    "A register user should not be verified at beginnig" in CreateUser {
      userStore.verified.getById(1) should beNone
    }
    "Once verified, should be in verified DB" in VerifyUser {
      userStore.verified.getById(1) should beSome(userId)
    }
    "Once verified, should be in normal DB" in VerifyUser {
      userStore.getById(1) should beSome(userId)
    }
  }
}