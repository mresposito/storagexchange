package com.storagexchange.controllers

import com.storagexchange.models._
import com.storagexchange.views

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import com.typesafe.scalalogging.slf4j.Logging
import javax.inject.Singleton
import javax.inject.Inject

case class TransactionRequest(
  storageTaken: Int,
  startDate: String,
  endDate: String,
  postID: Long)

@Singleton
class TransactionLedger @Inject()(transactionStore: TransactionStore) 
  extends Controller with Secured {

  val newTransactionForm = Form(
    mapping(
      "storageTaken" -> number(min=0),
      "startDate" -> nonEmptyText(minLength = 4),
      "endDate" -> nonEmptyText(minLength = 4),
      "postID" -> of[Long]
      )(TransactionRequest.apply)(TransactionRequest.unapply)
    )

  def newTransaction(postID: Long) = IsAuthenticated { username => _ =>
    Ok(views.html.transaction.newtransaction(newTransactionForm,postID))
  }

  def receiveNewTransaction  = IsAuthenticated { username => implicit request =>
    newTransactionForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.error404()),
      transactionData => {
        println(transactionData.postID)
        transactionStore.insert(Transaction(10, "2014-01-19 03:14:07.0", "2014-01-19 03:14:07.0",2,3,1,Some(1)/*transactionData.storageTaken,transactionData.startDate, transactionData.endDate, 1,2,transactionData.postID*/))
        Redirect(routes.PostBoard.myPosts)
      }
    )
  }

  def myPurchases =  IsAuthenticated { username => _ =>
    println(username);
    val purchaselist = transactionStore.getByBuyerUsername(username)
    Ok(views.html.transaction.mypurchases(purchaselist))
  }
}
