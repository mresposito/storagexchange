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
import java.sql.Timestamp

case class TransactionRequest(
  storageTaken: Int,
  startDate: Int,
  endDate: Int,
  postID: Int)

@Singleton
class TransactionLedger @Inject()(transactionStore: TransactionStore) 
  extends Controller with Secured{

  val newTransactionForm = Form(
    mapping(
      "storageTaken" -> number(min=0),
      "startDate" -> number(min=0),
      "endDate" -> number(min=0),
      "postID" -> number(min=0)
      )(TransactionRequest.apply)(TransactionRequest.unapply)
    )

  def newTransaction(postID: Long) = IsAuthenticated { username => _ =>
    Ok(views.html.transaction.newtransaction(newTransactionForm,postID))
  }

  def receiveNewTransaction  = IsAuthenticated { username => implicit request =>
    newTransactionForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.error404()),
      transactionData => {
        transactionStore.insert(Transaction(transactionData.storageTaken, 
          new Timestamp(transactionData.startDate), new Timestamp(transactionData.endDate), transactionData.postID, username))
        Redirect(routes.PostBoard.myPosts)
      }
    )
  }

  def myPurchases =  IsAuthenticated { username => _ =>
    val purchaselist = transactionStore.getByBuyerEmail(username)
    Ok(views.html.transaction.mypurchases(purchaselist))
  }
}
