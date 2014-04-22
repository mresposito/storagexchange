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
  endDate: Int)

case class TransactionApproveRequest(
  transactionID: Int)

@Singleton
class TransactionLedger @Inject()(transactionStore: TransactionStore) 
  extends Controller with Secured {

  val newTransactionForm = Form(
    mapping(
      "storageTaken" -> number(min=0),
      "startDate" -> number(min=0),
      "endDate" -> number(min=0)
      )(TransactionRequest.apply)(TransactionRequest.unapply)
    )

  val transactionApproveForm = Form(
    mapping(
      "transactionID" -> number(min=0)
      )(TransactionApproveRequest.apply)(TransactionApproveRequest.unapply)
    )

  def newTransaction(postID: Long) = IsAuthenticated { username => _ =>
    Ok(views.html.transaction.newtransaction(newTransactionForm,postID))
  }

  def receiveNewTransaction(postID: Long)  = IsAuthenticated { username => implicit request =>
    newTransactionForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.error404()),
      transactionData => {
        transactionStore.insert(Transaction(transactionData.storageTaken, 
          new Timestamp(transactionData.startDate), new Timestamp(transactionData.endDate), postID, username))
        Redirect(routes.TransactionLedger.myPurchases)
      }
    )
  }

  def myPurchases = IsAuthenticated { username => _ =>
    val purchaselist = transactionStore.getByBuyerEmail(username)
    Ok(views.html.transaction.mypurchases(purchaselist))
  }

  def mySales = IsAuthenticated { username => _ =>
    val salelist = transactionStore.getBySellerEmail(username)
    Ok(views.html.transaction.mysales(salelist))
  }

  def approveTransaction(transactionID : Long) =  IsAuthenticated { username => implicit request =>
    transactionStore.approve(transactionID, username)
    Redirect(routes.TransactionLedger.mySales)
  }
}
