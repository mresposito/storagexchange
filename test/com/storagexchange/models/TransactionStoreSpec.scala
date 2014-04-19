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
  val user1 = User("user1","user1","user1@user.com","abcd",1)
  val user2 = User("user2","user2","user2@user.com","abcd",1)

  val postStore: PostStore = new PostDAL
  val post1 = Post(user2.email, "My post", 95, 1, Some(1))
  val post2 = Post(user2.email, "Some other post", 42, 1, Some(2))
  val post1Copy = post1.copy()
  val post2Copy = post2.copy()

  val transactionStore: TransactionStore = new TransactionDAL
  val transaction1 = Transaction(10,new Timestamp(1397857973), new Timestamp(1397857973),
    1, user1.email, Some(user2.email), Some(1))
  val transaction2 = Transaction(3, new Timestamp(1397857973), new Timestamp(1397857973), 
    1, user1.email, Some(user2.email), Some(2))

  val transaction1Check = Transaction(transaction1.storageTaken, 
    transaction1.startDate, transaction1.endDate, transaction1.postID,
     user1.email, Some(user2.email), Some(1), Some(false),Some(0))
  val transaction2Check = Transaction(transaction2.storageTaken,
   transaction2.startDate, transaction2.endDate, transaction2.postID,
    user1.email, Some(user2.email), Some(2), Some(false),Some(0))

   val transaction1ApprovedCheck = Transaction(transaction1.storageTaken,
    transaction1.startDate, transaction1.endDate, transaction1.postID,
     user1.email, Some(user2.email), Some(1), Some(true),Some(0))

  val InsertTransaction = BeforeHook {
    DB.withConnection { implicit conn =>
      locationStore.insert(testLoc)
      userStore.insert(user1).toInt must beEqualTo(1)
      userStore.insert(user2).toInt must beEqualTo(2)
      postStore.insert(post1).toInt must beEqualTo(1)
      postStore.insert(post2).toInt must beEqualTo(2)
      transactionStore.insert(transaction1).toInt must beEqualTo(1)
      transactionStore.insert(transaction2).toInt must beEqualTo(2)
    }
  }

  val InsertUser = BeforeHook {
    DB.withConnection { implicit conn =>
      locationStore.insert(testLoc)
      userStore.insert(user1).toInt must beEqualTo(1)
      userStore.insert(user2).toInt must beEqualTo(2)
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
      transactionStore.getByPostID(transaction1.postID) must beEqualTo(List(transaction1Check,transaction2Check))
    }
    "not find a non-existent transaction by postID" in InsertTransaction {
      transactionStore.getByPostID(123123) must beEmpty
    }
    "find transaction by buyerEmail" in InsertTransaction {
      transactionStore.getByBuyerEmail(transaction1.buyerEmail) must beEqualTo(List(transaction1Check,transaction2Check))
    }
    "not find a non-existent transaction by buyerEmail" in InsertTransaction {
      transactionStore.getByBuyerEmail("asdfasd@user.com") must beEmpty
    }
    "find transaction by sellerEmail" in InsertTransaction {
      transactionStore.getBySellerEmail(user2.email) must beEqualTo(List(transaction1Check,transaction2Check))
    }
    "not find a non-existent transaction by sellerEmail" in InsertTransaction {
      transactionStore.getBySellerEmail("asdfasd@user.com") must beEmpty
    }
    "approved a transaction" in InsertTransaction {
      transactionStore.approve(1,"user2@user.com")
      transactionStore.getByID(1) must beSome(transaction1ApprovedCheck)

    }
    "can't approved a transaction as non-seller" in InsertTransaction {
      transactionStore.approve(1,"user1@user.com")
      transactionStore.getByID(1) must beSome(transaction1Check)
    }
  }
}
