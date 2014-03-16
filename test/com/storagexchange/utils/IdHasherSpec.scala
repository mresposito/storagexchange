package com.storagexchange.utils

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.Crypto
import org.specs2.mutable._
import org.specs2.ScalaCheck
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll

/**
 * These tests take a long time to run
 * So they are commented out
 */
class IdHasherSpec extends Specification with ScalaCheck {
   args(skipAll = true)
  
  val message = "hello this is my message"
  val hasher = new Base64AES

  // FIXME!
  // should be able to encode any message
  "A Base 64 encoder" should {
    "Encode and decode message" in {
      running(FakeApplication()) {
        val encoded = hasher.encrypt(message)
        hasher.decrypt(encoded) == message
      }
    }
  }
  "A Base 64 Long Encoder" should {
    "Encode and decode any non negative long" in {
      forAll { n : Long =>
        n >= 0 ==>
        running(FakeApplication()) {
          val encoded = hasher.encrypt(n)
          hasher.decryptLong(encoded) == n
        }
      }
    }
    "Return null if not valid" in {
      running(FakeApplication()) {
        val encoded = hasher.encrypt("hello8")
        hasher.decryptLong(encoded) must throwA[Exception]
      }
    }
  }
}
