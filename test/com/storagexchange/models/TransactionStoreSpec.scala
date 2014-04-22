package com.storagexchange.models

import org.mockito.Mockito.{mock, when}
import com.storagexchange.utils._
import org.specs2.mutable._
import org.specs2.execute.AsResult
import play.api.test._
import play.api.test.Helpers._
import play.api.db._
import play.api.Play.{current => curr}
import java.sql.Timestamp
import org.h2.jdbc.JdbcSQLException
import com.storagexchange.controllers.UserTest
import com.storagexchange.controllers.PostTest

class TransactionStoreSpec extends Specification with PostTest {
  val postStore: PostStore = new PostDAL

  val transactionStore: TransactionStore = new TransactionDAL
  val transaction1 = Transaction(10,new Timestamp(1397857973), new Timestamp(1397857973),
    1, "buyer@user.com", Some(post1.email), Some(1))
  val transaction2 = Transaction(3, new Timestamp(1397857973), new Timestamp(1397857973), 
    1,  "buyer@user.com", Some(post1.email), Some(2))

  val transaction1Check = Transaction(transaction1.storageTaken, 
    transaction1.startDate, transaction1.endDate, transaction1.postID,
     "buyer@user.com", Some(post1.email), Some(1), false, false)
  val transaction2Check = Transaction(transaction2.storageTaken,
   transaction2.startDate, transaction2.endDate, transaction2.postID,
    "buyer@user.com", Some(post1.email), Some(2), false, false)

   val transaction1ApprovedCheck = Transaction(transaction1.storageTaken,
    transaction1.startDate, transaction1.endDate, transaction1.postID,
     "buyer@user.com", Some(post1.email), Some(1), true, false)

  val InsertTransaction = BeforeHook {
    DB.withConnection { implicit conn =>
      locationStore.insert(testLoc)
      postStore.insert(post1).toInt must beEqualTo(1)
      postStore.insert(post2).toInt must beEqualTo(2)
      transactionStore.insert(transaction1).toInt must beEqualTo(1)
      transactionStore.insert(transaction2).toInt must beEqualTo(2)
    }
  }

  val InsertUser = BeforeHook {
    DB.withConnection { implicit conn =>
      locationStore.insert(testLoc)
      postStore.insert(post1).toInt must beEqualTo(1)
      postStore.insert(post2).toInt must beEqualTo(2)
    }
  }
  
  "Transaction Store" should {
    "insert a transaction" in InsertUser {
        transactionStore.insert(transaction1).toInt must beEqualTo(1)
    }
    "find transaction by id" in InsertTransaction {
      transactionStore.getByID(1) must beSome(transaction1Check)
    }
    "not find a non-existent transaction by id" in InsertTransaction {
      transactionStore.getByID(3049) must beNone
    }
    "find transaction by postID" in InsertTransaction {
      transactionStore.getByPostID(transaction1.postID) must beEqualTo(List(transaction1Check, transaction2Check))
    }
    "not find a non-existent transaction by postID" in InsertTransaction {
      transactionStore.getByPostID(123123) must beEmpty
    }
    "find transaction by buyerEmail" in InsertTransaction {
      transactionStore.getByBuyerEmail(transaction1.buyerEmail) must beEqualTo(List(transaction1Check, transaction2Check))
    }
    "not find a non-existent transaction by buyerEmail" in InsertTransaction {
      transactionStore.getByBuyerEmail("asdfasd@user.com") must beEmpty
    }
    "find transaction by sellerEmail" in InsertTransaction {
      transactionStore.getBySellerEmail(post1.email) must beEqualTo(List(transaction1Check, transaction2Check))
    }
    "not find a non-existent transaction by sellerEmail" in InsertTransaction {
      transactionStore.getBySellerEmail("asdfasd@user.com") must beEmpty
    }
    "approved a transaction" in InsertTransaction {
      transactionStore.approve(1,post1.email)
      transactionStore.getByID(1) must beSome(transaction1ApprovedCheck)
    }
    "can't approved a transaction as non-seller" in InsertTransaction {
      transactionStore.approve(1,"buyer@user.com")
      transactionStore.getByID(1) must beSome(transaction1Check)
    }
  }
}
