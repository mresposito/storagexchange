package com.storagexchange.controllers

import java.sql.Timestamp
import com.storagexchange.models._
import com.storagexchange.utils._
import play.api.test._
import play.api.test.Helpers._
import play.api.Play.current
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

trait TransactionTest extends Specification with PostTest{
  val postStore: PostStore = new PostDAL
  val transaction1 = Transaction(10,new Timestamp(1397857973), new Timestamp(1397857973),
    1, "buyer@user.com", Some(post1.email), Some(1))

  val CreateTransactions = BeforeHook {
    locationStore.insert(testLoc)
    postStore.insert(post1)
    val Some(create1) = route(createRequest(transaction1,1))
    status(create1) must beEqualTo(SEE_OTHER)
  }

  val CreatePostsTransactions = BeforeHook {
    locationStore.insert(testLoc)
    postStore.insert(post1)
  }

  def createRequest(transaction: Transaction, postID: Long) = FakeRequest(POST, routes.TransactionLedger.receiveNewTransaction(postID).url).
    withSession(("email", transaction.buyerEmail)).
    withFormUrlEncodedBody(
      "storageTaken" -> transaction.storageTaken.toString,
      "startDate" -> transaction.startDate.getTime().toString,
      "endDate" -> transaction.endDate.getTime().toString)

  def requestWithSessionTransaction(route: String, email: String) = withSessionTransaction(FakeRequest(GET, route), email)
  def withSessionTransaction[T](request: FakeRequest[T], email: String) = request.withSession(("email", email))

}

class TransactionLedgerSpec extends Specification with TransactionTest {
  "Transaction Ledger" should {
    "accept valid transaction" in CreatePostsTransactions {
      val Some(create) = route(createRequest(transaction1,1))
      status(create) must beEqualTo(SEE_OTHER)
    }
    "reject transaction with invalid post" in CreatePostsTransactions {
      val Some(create) = route(createRequest(transaction1,231123))
      status(create) must beEqualTo(BAD_REQUEST)
    }
    "view purchases" in CreateTransactions {
      val Some(myPurchases) = route(requestWithSessionTransaction(routes.TransactionLedger.myPurchases.url, transaction1.buyerEmail))
      contentAsString(myPurchases) must contain(transaction1.storageTaken.toString)
    }
    "view Sales" in CreateTransactions {
      val Some(mysales) = route(requestWithSessionTransaction(routes.TransactionLedger.mySales.url,post1.email))
      contentAsString(mysales) must contain(transaction1.storageTaken.toString)
    }
    "approve transaction" in CreateTransactions{
      val Some(approve) = route(requestWithSessionTransaction(routes.TransactionLedger.approveTransaction(1).url,post1.email))
      contentAsString(approve) must not contain("Approve")
    }
    "reject approval for non-existant transaction" in CreateTransactions{
      val Some(approve) = route(requestWithSessionTransaction(routes.TransactionLedger.approveTransaction(345234).url,post1.email))
      status(approve) must beEqualTo(BAD_REQUEST)
    }
  }
}