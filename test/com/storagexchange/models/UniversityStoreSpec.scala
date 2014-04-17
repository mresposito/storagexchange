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
import java.math.BigDecimal

trait UniversityTest extends LocationTest {
  val testUniversity = University(1,"University of California, Berkeley", "http://www.berkeley.edu", 
    "http://upload.wikimedia.org/wikipedia/commons/f/fc/The_University_of_California_1868.svg",
    "Yale Blue, California Gold", None)
  val cal_x = new BigDecimal(37.000000).setScale(6,BigDecimal.ROUND_HALF_UP)
  val cal_y = new BigDecimal(122.000000).setScale(6,BigDecimal.ROUND_HALF_UP)
  val testLocation = Location("University of California, Berkeley",
      cal_x, cal_y, "Berkeley", "California",
      "103 Sproul Hall", "94720")
  
  val InsertLocation = BeforeHook {
    DB.withConnection { implicit conn =>
      locationStore.insert(testLocation)
    }
  }
  val InsertUniversity = BeforeHook {
    DB.withConnection { implicit conn =>
      locationStore.insert(testLocation)
      universityStore.insert(testUniversity)
    }
  }
}

class UniversityStoreSpec extends Specification with UniversityTest {
  
  "University Store" should {
    "insert university properly" in InsertLocation {
      universityStore.insert(testUniversity) must beEqualTo(1)
    }
    "catch exception if ref. integrity violated" in RunningApp {
      universityStore.insert(testUniversity) must throwA[Exception]
    }
    "get universities by name" in InsertUniversityLocation {
      universityStore.getUniversityByName("Stanford University") must beSome(testUniv)
    }
    "get universities by city" in InsertUniversityLocation {
      universityStore.getByCity("Stanford") must beSome(testUniv)
    }
    "if no inserted uni, get nothing" in RunningApp {
      universityStore.getAll must haveSize(0)
    }
    "get 1 unis if inserted one" in InsertUniversityLocation {
      universityStore.getAll must haveSize(1)
    }
  }
}
