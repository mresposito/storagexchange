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

class LocationStoreSpec extends Specification  {

  val locationStore: LocationStore = new LocationDAL()
  val universityStore: UniversityStore = new UniversityDAL()
  
  val x = new BigDecimal(15.000000).setScale(6,BigDecimal.ROUND_HALF_UP)

  val location = Location("Home", x, x, "Cerritos", "California", "12640 Misty Place", "90703")

  val y = new BigDecimal(37.000000).setScale(6,BigDecimal.ROUND_HALF_UP)
  val z = new BigDecimal(122.000000).setScale(6,BigDecimal.ROUND_HALF_UP)

  val testLoc = Location("Stanford University", y, z, "Stanford", "California", "450 Serra Mall", "94305", Some(1))
  val testUniv = University(1,"Stanford University", "http://www.stanford.edu", 
                            "http://upload.wikimedia.org/wikipedia/en/b/b7/Stanford_University_seal_2003.svg",
                            "Cardinal, White", None)
    
  val InsertUniversity = BeforeHook {
    DB.withConnection { implicit conn =>
      locationStore.insert(testLoc)
      universityStore.insert(testUniv)
    }
  }

  "Location Store" should {
    "insert a location" in RunningApp {
      //is 6 because there will be 5 other locations inserted on startup
      locationStore.insert(location).toInt must beEqualTo(1)
    }
    "get location from id" in InsertUniversity {
      locationStore.getById(1) must beSome(testLoc)
    }
  }
}
