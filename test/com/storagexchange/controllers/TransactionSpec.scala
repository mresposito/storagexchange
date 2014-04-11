package com.storagexchange.controllers

import com.storagexchange.models._
import com.storagexchange.utils._
import play.api.test._
import play.api.test.Helpers._
import play.api.Play.current
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import com.storagexchange.models.PostStore

trait TransactiomTest extends Specification {
/*
    val transaction1 = Transaction(10, "2014-01-19 03:14:07", "2014-01-19 03:14:07", "2014-01-19 03:14:07",Some(1),1,2,Some(1))

    val CreateTransactions = BeforeHook {
        val Some(create1) = route(createRequest(transaction1))
    }

    def createRequest(transaction: Transaction) = FakeRequest(POST, "/post").
    withSession(("userID", transaction.buyerID)).
    withFormUrlEncodedBody(
      "postID" -> transaction.postID,
      "sellerID" -> transaction.sellerID,
      "storageSize" -> transaction.storageSize.toString,
      "fromDate" -> transaction.fromDate,
      "toDate" -> transaction.toDate)

    def requestWithSession(route: String) = withSession(FakeRequest(GET, route))
    def withSession[T](request: FakeRequest[T]) = request.withSession(("userID", transaction1.buyerID))
    */
}

class TransactionSpec extends Specification with PostTest {
}