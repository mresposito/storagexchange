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
  postID: Long,
  sellerID: Long)

@Singleton
class TransactionLedger @Inject()(transactionStore: TransactionStore) 
  extends Controller with Secured {

  val newTransactionForm = Form(
    mapping(
      "storageTaken" -> number(min=0),
      "startDate" -> nonEmptyText(minLength = 4),
      "endDate" -> nonEmptyText(minLength = 4),
      "postID" -> of[Long],
      "sellerID" -> of[Long]
      )(TransactionRequest.apply)(TransactionRequest.unapply)
    )

  def newTransaction(postID: Long) = IsAuthenticated { username => _ =>
    Ok(views.html.transaction.newtransaction(newTransactionForm,postID))
  }

  def receiveNewTransaction  = IsAuthenticated { username => implicit request =>
    newTransactionForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.error404()),
      transactionData => {
        BadRequest(views.html.error404())
      }
    )
  }
}
