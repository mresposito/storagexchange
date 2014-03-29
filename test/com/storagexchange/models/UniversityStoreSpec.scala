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
import com.storagexchange.controllers.UniversityTest
import java.math.BigDecimal

class UniversityStoreSpec extends Specification with UniversityTest {
  
  "University Store" should {
    "contain universities on start" in RunningApp {
      val universityList: List[University] = universityStore.getAll()
      universityList(4) must beEqualTo(testUniversity)
    }
  }
}
