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

  val testUniversity = University(5,"University of California, Berkeley", "http://www.berkeley.edu", 
                                  "http://upload.wikimedia.org/wikipedia/commons/f/fc/The_University_of_California_1868.svg",
                                  "Yale Blue, California Gold", Option(5)) 

  "University Store" should {
    "contain universities on start" in RunningApp {
      val universityList: List[University] = universityStore.getAll()
      universityList(4) must beEqualTo(testUniversity)
    }
  }
}
