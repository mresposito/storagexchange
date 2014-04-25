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

trait LocationTest {
  // conversions for BigDecimal
  implicit def convert(u: Double): BigDecimal = new BigDecimal(u).
    setScale(6,BigDecimal.ROUND_HALF_UP)
  implicit def toDouble(b: java.math.BigDecimal): Double = b.doubleValue()
  
  val locationStore: LocationStore = new LocationDAL()
  val universityStore: UniversityStore = new UniversityDAL()
  
  val x = 15.000000
  val location = Location("Home", x, x, "Cerritos", "California", "12640 Misty Place", "90703")

  val testLoc = Location("Stanford University", 37.429167, -122.138056,
    "Stanford", "California",
    "450 Serra Mall", "94305", Some(1))
  val stanford = testLoc
  val berkley = Location("Berkeley", 37.871667, -122.272778, "does",
    "really", "not", "matter", Some(3))

  val testLoc2 = Location("University of Illinois, Urbana Champaign", 40.110500, 
    -88.228400, "Champaign", "Illinois", 
    "Admissions and Records, 901 West Illinois Street","61801", Some(2)) 

  val testUniv = University(1,"Stanford University", "http://www.stanford.edu", 
    "http://upload.wikimedia.org/wikipedia/en/b/b7/Stanford_University_seal_2003.svg",
    "Cardinal, White", Some(1))

  def insertUniversityLocation = {
    locationStore.insert(testLoc)
    universityStore.insert(testUniv)
  }
  val InsertUniversityLocation = BeforeHook {
    DB.withConnection { implicit conn =>
      insertUniversityLocation
    }
  }
}

class LocationStoreSpec extends Specification with LocationTest  {      
  "Location Store" should {
    "insert a location" in RunningApp {
      //equals 1 because data isn't inserted on startup in Test mode
      locationStore.insert(location).toInt must beEqualTo(1)
    }
    "get location from id" in InsertUniversityLocation {
      locationStore.getById(1) must beSome(testLoc)
    }
  }
}