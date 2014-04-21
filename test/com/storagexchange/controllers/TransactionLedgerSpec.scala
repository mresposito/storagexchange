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
    val Some(create1) = route(createRequest(transaction1))
  }

  def createRequest(transaction: Transaction) = FakeRequest(POST, "/post").
  withSession(("email", transaction.buyerEmail)).
  withFormUrlEncodedBody(
    "storageTaken" -> transaction.storageTaken.toString,
    "startDate" -> transaction.startDate.toString,
    "endDate" -> transaction.endDate.toString)

  def requestWithSessionBuyer(route: String) = withSession(FakeRequest(GET, route))
  def withSessionBuyer[T](request: FakeRequest[T]) = request.withSession(("email", transaction1.buyerEmail))

  def requestWithSessionSeller(route: String) = withSession(FakeRequest(GET, route))
  def withSessionSeller[T](request: FakeRequest[T]) = request.withSession(("email", post1.email))
}

class TransactionLedgerSpec extends Specification with TransactionTest{
  "Transaction Ledger" should{
    "accept valid transaction" in CreatePosts {
      val Some(create) = route(createRequest(transaction1))
      status(create) must beEqualTo(SEE_OTHER)
    }
    "reject transaction with invalid post" in CreatePosts{
      val Some(create) = route(createRequest(transaction1))
      status(create) must beEqualTo(BAD_REQUEST)
    }
    "view purchases" in CreateTransactions{
      val Some(myPurchases) = route(requestWithSessionBuyer(routes.TransactionLedger.myPurchases.url))
      contentAsString(myPurchases) must contain(transaction1.storageTaken.toString)
    }
    "view Sales" in CreateTransactions{
      val Some(mysales) = route(requestWithSessionSeller(routes.TransactionLedger.mySales.url))
      contentAsString(mysales) must contain(transaction1.storageTaken.toString)
    }
  }
}