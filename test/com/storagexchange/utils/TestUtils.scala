package com.storagexchange.utils

import org.specs2.execute.AsResult
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.db._
import play.api.Play.{current => curr}
import java.sql.Timestamp

// provides a way to mix in empty classes
abstract class DbFixture extends org.specs2.mutable.Around {
  private def app = new FakeApplication(additionalConfiguration = inMemoryDatabase())
  def before: Unit 
  def around[T: AsResult](t: => T) = {
    running(app) {
      before
      AsResult(t)
    }
  }
}

// implementation with empty loader
object RunningApp extends DbFixture {
  def before: Unit = {}
}

object BeforeHook {
  trait B
  def apply(t: => Unit) = new DbFixture with B {
    def before: Unit = t
  }
}
