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

class TransactionStoreSpec extends Specification {
  val transactionStore: TransactionStore = new TransactionDAL
  val transaction1 = Transaction(10, "2014-01-19 03:14:07", "2014-01-19 03:14:07", "2014-01-19 03:14:07",Some(1),1,2,Some(1))
  val transaction2 = Transaction(3, "2014-01-19 03:14:07", "2014-01-19 03:14:07", "2014-01-19 03:14:07",Some(1),1,2,Some(2))
  val transaction1Copy = transaction1.copy()
  val transaction2Copy = transaction2.copy()


  val InsertTransaction = BeforeHook {
    DB.withConnection { implicit conn =>
      transactionStore.insert(transaction1).toInt must beEqualTo(1)
      transactionStore.insert(transaction2).toInt must beEqualTo(2)
    }
  }
  
  "Transaction Store" should {
    "insert a transaction" in RunningApp {
        transactionStore.insert(transaction1).toInt must beEqualTo(1)
    }
    "find post by postID" in InsertTransaction {
      transactionStore.getByPostID(transaction1.postID) must beEqualTo(List(transaction1Copy))
    }
    "not find a non-existent transaction by postID" in InsertTransaction {
      transactionStore.getByPostID(123123) must beEmpty
    }
    "find transaction by id" in InsertTransaction {
      transactionStore.getById(1) must beSome(transaction1Copy)
    }
    "not find a non-existent transaction by id" in InsertTransaction {
      transactionStore.getById(3049) must beNone
    }
  }
}
