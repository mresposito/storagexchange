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
import java.util.Calendar

case class TransactionRequest(
  storageTaken: Int,
  startDate: Long,
  endDate: Long)

case class RatingRequest(
  score: Int)

@Singleton
class TransactionLedger @Inject()(transactionStore: TransactionStore, 
  ratingStore: RatingStore, postStore: PostStore) extends Controller with Secured {

  val newTransactionForm = Form(
    mapping(
      "storageTaken" -> number(min=0),
      "startDate" -> longNumber(min=0),
      "endDate" -> longNumber(min=0)
      )(TransactionRequest.apply)(TransactionRequest.unapply)
    )

  val newRatingForm = Form(
    mapping(
      "score" -> number(min=1)
      )(RatingRequest.apply)(RatingRequest.unapply)
    )

  def newTransaction(postID: Long) = IsAuthenticated { username => _ =>
    if (postStore.getById(postID).isEmpty) { 
      BadRequest(views.html.error404())
    } else { 
      Ok(views.html.transaction.newtransaction(newTransactionForm, postID))
    }
  }

  def receiveNewTransaction(postID: Long) = IsAuthenticated { username => implicit request =>
    newTransactionForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.transaction.newtransaction(formWithErrors,postID)),
      transactionData => 
      if (postStore.getById(postID).isEmpty) {
        BadRequest(views.html.error404())
      } else {
        transactionStore.insert(Transaction(transactionData.storageTaken, 
          new Timestamp(transactionData.startDate), 
          new Timestamp(transactionData.endDate), postID, username))
        Redirect(routes.TransactionLedger.myPurchases)
      }
    )
  }

  def receiveNewRating(transactionID: Long) = IsAuthenticated { _ => implicit request =>
    newRatingForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.error404()),
      scoreData => { 
        transactionStore.getByID(transactionID).map { transaction =>
          ratingStore.getByTransactionID(transactionID).map { rating =>
            ratingStore.updateByTransactionID(transactionID, scoreData.score)
          }.getOrElse {
            ratingStore.insert(Rating(transactionID, scoreData.score, 
              Some(transaction.buyerEmail), transaction.sellerEmail))
          }
          Redirect(routes.TransactionLedger.myPurchases)     
        }.getOrElse {
          BadRequest(views.html.error404())  
        }
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

  def approveTransaction(transactionID: Long) = IsAuthenticated { username => _ =>
    val result = transactionStore.approve(transactionID, username)
    if (result == 0) { 
      BadRequest(views.html.error404())
    } else { 
      Redirect(routes.TransactionLedger.mySales)
    }
  }

  def cancelTransactionAsBuyer(transactionID: Long) = IsAuthenticated { username => _ =>
    val result = transactionStore.cancelAsBuyer(transactionID, username)
    if (result == 0) { 
      BadRequest(views.html.error404())
    } else { 
      Redirect(routes.TransactionLedger.myPurchases)
    }
  }

  def cancelTransactionAsSeller(transactionID: Long) = IsAuthenticated { username => _ =>
    val result = transactionStore.cancelAsSeller(transactionID, username)
    if (result == 0) { 
      BadRequest(views.html.error404())
    } else { 
      Redirect(routes.TransactionLedger.mySales)
    }
  }

}