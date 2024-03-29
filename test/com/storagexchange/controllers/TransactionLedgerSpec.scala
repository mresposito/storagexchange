package com.storagexchange.controllers

import org.mockito.Mockito.{mock, when}
import java.sql.Timestamp
import com.storagexchange.models._
import com.storagexchange.utils._
import play.api.test._
import play.api.test.Helpers._
import play.api.Play.current
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

trait TransactionTest extends Specification with PostTest {
  // mock the class
  when(clock.now).thenReturn(today)
  val time = clock.now

  val postStore: PostStore = new PostDAL
  val transaction1 = Transaction(10, time, time,
    1, "buyer@user.com", Some(post1.email), Some(1))

  val CreateTransactions = BeforeHook {
    locationStore.insert(testLoc)
    postStore.insert(post1)
    val Some(create1) = route(createTransactionRequest(transaction1, 1))
    status(create1) must beEqualTo(SEE_OTHER)
  }

  val CreateTransactionRatings = BeforeHook {
    locationStore.insert(stanford)
    userStore.insert(user)
    route(createRequest(post1))
    route(createTransactionRequest(transaction1, 1))
    route(requestWithSessionTransaction(
      routes.TransactionLedger.approveTransaction(1).url, post1.email))
  }

  val CreatePostsTransactions = BeforeHook {
    locationStore.insert(testLoc)
    postStore.insert(post1)
  }

  def createTransactionRequest(transaction: Transaction, postID: Long) = 
    FakeRequest(POST, routes.TransactionLedger.receiveNewTransaction(postID).url).
    withSession(("email", transaction.buyerEmail)).
    withFormUrlEncodedBody(
      "storageTaken" -> transaction.storageTaken.toString,
      "startDate" -> transaction.startDate.getTime().toString,
      "endDate" -> transaction.endDate.getTime().toString)

  def createRatingRequest(transaction: Transaction, score: Int) = FakeRequest(POST, 
    routes.TransactionLedger.receiveNewRating(transaction.transactionID.get).url).
    withSession(("email", transaction.buyerEmail)).
    withFormUrlEncodedBody("score" -> score.toString)

  def requestWithSessionTransaction(route: String, email: String) = 
    withSessionTransaction(FakeRequest(GET, route), email)
  def withSessionTransaction[T](request: FakeRequest[T], email: String) = 
    request.withSession(("email", email))

}

class TransactionLedgerSpec extends Specification with TransactionTest {
  "Transaction Ledger" should {
    "accept valid new transaction url" in CreatePostsTransactions {
      val Some(createForm) = route(requestWithSessionTransaction(
        routes.TransactionLedger.newTransaction(1).url, transaction1.buyerEmail))
      status(createForm) must beEqualTo(OK)
    }
    "reject invalid new transaction url" in CreatePostsTransactions {
      val Some(createForm) = route(requestWithSessionTransaction(
        routes.TransactionLedger.newTransaction(2342132).url, transaction1.buyerEmail))
      status(createForm) must beEqualTo(BAD_REQUEST)
    }
    "accept valid transaction" in CreatePostsTransactions {
      val Some(create) = route(createTransactionRequest(transaction1, 1))
      status(create) must beEqualTo(SEE_OTHER)
    }
    "reject transaction with invalid post" in CreatePostsTransactions {
      val Some(create) = route(createTransactionRequest(transaction1, 231123))
      status(create) must beEqualTo(BAD_REQUEST)
    }
    "view purchases" in CreateTransactions {
      val Some(myPurchases) = route(requestWithSessionTransaction(
        routes.TransactionLedger.myPurchases.url, transaction1.buyerEmail))
      contentAsString(myPurchases) must contain(transaction1.storageTaken.toString)
    }
    "view Sales" in CreateTransactions {
      val Some(mysales) = route(requestWithSessionTransaction(
        routes.TransactionLedger.mySales.url, post1.email))
      contentAsString(mysales) must contain(transaction1.storageTaken.toString)
    }
    "approve transaction" in CreateTransactions {
      val Some(approve) = route(requestWithSessionTransaction(
        routes.TransactionLedger.approveTransaction(1).url, post1.email))
      contentAsString(approve) must not contain("Approve")
    }
    "reject approval for non-existant transaction" in CreateTransactions {
      val Some(approve) = route(requestWithSessionTransaction(
        routes.TransactionLedger.approveTransaction(345234).url, post1.email))
      status(approve) must beEqualTo(BAD_REQUEST)
    }
    "reject approval by non-seller" in CreateTransactions {
      val Some(approve) = route(requestWithSessionTransaction(
        routes.TransactionLedger.approveTransaction(1).url, "buyer@user.com"))
      val Some(mysales) = route(requestWithSessionTransaction(
        routes.TransactionLedger.mySales.url,post1.email))
      status(approve) must beEqualTo(BAD_REQUEST)
      contentAsString(mysales) must contain("Approve")
    }
    "cancel transaction as buyer" in CreateTransactions {
      val Some(cancel) = route(requestWithSessionTransaction(
        routes.TransactionLedger.cancelTransactionAsBuyer(1).url, "buyer@user.com"))
      contentAsString(cancel) must not contain(transaction1.storageTaken.toString)
    }
    "cancel transaction as seller" in CreateTransactions {
      val Some(cancel) = route(requestWithSessionTransaction(
        routes.TransactionLedger.cancelTransactionAsSeller(1).url, post1.email))
      contentAsString(cancel) must not contain(transaction1.storageTaken.toString)
    }
    "reject cancel transaction as buyer by non-buyer" in CreateTransactions {
      val Some(cancel) = route(requestWithSessionTransaction(
        routes.TransactionLedger.cancelTransactionAsBuyer(1).url, post1.email))
      val Some(myPurchases) = route(requestWithSessionTransaction(
        routes.TransactionLedger.myPurchases.url, transaction1.buyerEmail))
      status(cancel) must beEqualTo(BAD_REQUEST)
      contentAsString(myPurchases) must contain(transaction1.storageTaken.toString)
    }
    "reject cancel transaction as seller by non-seller" in CreateTransactions {
      val Some(cancel) = route(requestWithSessionTransaction(
        routes.TransactionLedger.cancelTransactionAsSeller(1).url, "buyer@user.com"))
      val Some(mysales) = route(requestWithSessionTransaction(
        routes.TransactionLedger.mySales.url, post1.email))
      status(cancel) must beEqualTo(BAD_REQUEST)
      contentAsString(mysales) must contain(transaction1.storageTaken.toString)
    }
    "avg rating does not show up on unrated post page" in CreateTransactionRatings {
      val Some(postInfo) = route(viewPost(post1.postID.get)) 
      contentAsString(postInfo) must not contain("Avg. Rating")
    }
  }
}