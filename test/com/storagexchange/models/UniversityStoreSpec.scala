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

class UniversityStoreSpec extends Specification {
  
  val universityStore: UniversityStore = new UniversityDAL()
  val locationStore: LocationStore = new LocationDAL()
  val testUniversity = University(1,"University of California, Berkeley", "http://www.berkeley.edu", 
                                  "http://upload.wikimedia.org/wikipedia/commons/f/fc/The_University_of_California_1868.svg",
                                  "Yale Blue, California Gold", None)
  val x = new BigDecimal(37.000000).setScale(6,BigDecimal.ROUND_HALF_UP)
  val y = new BigDecimal(122.000000).setScale(6,BigDecimal.ROUND_HALF_UP)
  val testLocation = Location("University of California, Berkeley", x, y, "Berkeley", "California", "103 Sproul Hall", "94720")
  
  def insertLocation = {
    locationStore.insert(testLocation)
  }
  
  val InsertLocation = BeforeHook {
    DB.withConnection { implicit conn =>
      insertLocation
    }
  }
  
  "University Store" should {
    "insert university properly" in InsertLocation {
      universityStore.insert(testUniversity) must beEqualTo(1)
    }
    "catch exception if ref. integrity violated" in RunningApp {
      universityStore.insert(testUniversity) must throwA[Exception]
    }
  }
}
