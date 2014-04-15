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

class TransactionStoreSpec extends Specification with UserTest {
  val transactionStore: TransactionStore = new TransactionDAL
  val transaction1 = Transaction(10, "2014-01-19 03:14:07.0", "2014-01-19 03:14:07.0",1,2,1,Some(1))
  val transaction2 = Transaction(3, "2014-01-19 03:14:07.0", "2014-01-19 03:14:07.0",1,2,1,Some(2))
  val transaction1Copy = transaction1.copy()
  val transaction2Copy = transaction2.copy()

  val transactionByEmail1 = TransactionByEmail(10, "2014-01-19 03:14:07.0", "2014-01-19 03:14:07.0","user1@user.com","user2@user.com",1,Some(1))
  val transactionByEmail2 = TransactionByEmail(10, "2014-01-19 03:14:07.0", "2014-01-19 03:14:07.0","user1@user.com","user2@user.com",1,Some(2))
  val transactionByEmail1Copy = transaction1.copy()
  val transactionByEmail2Copy = transaction2.copy()

  //val userStore: UserStore = new UserDAL
  val user1 = User("user1","user1","user1@user.com","abcd",1);
  val user2 = User("user2","user2","user2@user.com","abcd",1);


  val InsertTransaction = BeforeHook {
    DB.withConnection { implicit conn =>
      userStore.insert(user1).toInt must beEqualTo(1)
      userStore.insert(user2).toInt must beEqualTo(2)
      transactionStore.insert(transaction1).toInt must beEqualTo(1)
      transactionStore.insert(transaction2).toInt must beEqualTo(2)
    }
  }

  val InsertUser = BeforeHook {
    DB.withConnection { implicit conn =>
      userStore.insert(user1).toInt must beEqualTo(1)
      userStore.insert(user2).toInt must beEqualTo(2)
    }
  }
  
  "Transaction Store" should {
    "insert a transaction" in RunningApp {
        transactionStore.insert(transaction1).toInt must beEqualTo(1)
    }
    "insert a transaction by Email" in InsertUser {
        transactionStore.insertByEmail(transactionByEmail1).toInt must beEqualTo(1)
    }
    "find transaction by id" in InsertTransaction {
      transactionStore.getByID(1) must beSome(transaction1Copy)
    }
    "not find a non-existent transaction by id" in InsertTransaction {
      transactionStore.getByID(3049) must beNone
    }
    "find transaction by postID" in InsertTransaction {
      transactionStore.getByPostID(transaction1.postID) must beEqualTo(List(transaction1Copy,transaction2Copy))
    }
    "not find a non-existent transaction by postID" in InsertTransaction {
      transactionStore.getByPostID(123123) must beEmpty
    }
    "find transaction by buyerID" in InsertTransaction {
      transactionStore.getByBuyerID(transaction1.buyerID) must beEqualTo(List(transaction1Copy,transaction2Copy))
    }
    "not find a non-existent transaction by buyerID" in InsertTransaction {
      transactionStore.getByBuyerID(123123) must beEmpty
    }
    "find transaction by sellerID" in InsertTransaction {
      transactionStore.getBySellerID(transaction1.sellerID) must beEqualTo(List(transaction1Copy,transaction2Copy))
    }
    "not find a non-existent transaction by sellerID" in InsertTransaction {
      transactionStore.getBySellerID(123123) must beEmpty
    }
  }
}
