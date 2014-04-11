package com.storagexchange.models

import java.sql.Timestamp
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import javax.inject.Singleton
import javax.inject.Inject

case class Transaction(storageTaken: Int,
  startDate: String,
  endDate: String,
  buyerID: Long,
  sellerID: Long,
  postID: Long,
  transactionID: Option[Long] = None)

/**
 * Methods that we will be using from
 * the interface to the database
 */
trait TransactionStore {
  def insert(trasaction: Transaction): Long
  def getByID(ID: Long): Option[Transaction]
  /*def getByPostID(postID: Long): Option[Transaction]
  def getByBuyerID(email: String): List[Transaction]
  def getBySellerID(email: String): List[Transaction]*/
}

// Actual implementation of Transaction Store method
@Singleton
class TransactionDAL extends TransactionStore {
  
  private[this] val createTransactionSql = {
    SQL("""
      INSERT INTO Transaction
        (storageTaken, startDate, endDate, buyerID, sellerID, postID)
      VALUES
        ({storageTaken}, {startDate}, {endDate}, {buyerID}, {sellerID}, {postID})
    """.stripMargin)
  }

  private[this] val findTransactionByIdSql = {
    SQL("""
       SELECT *
       FROM Transaction
       WHERE transactionID = {transactionID}
    """.stripMargin)
  }

  private[this] val findTransactionByEmailSql = {
    SQL("""
       SELECT *
       FROM Transaction
       WHERE email = {email}
    """.stripMargin)
  }

  implicit val transactionParser = 
    long("transactionID")~
    long("buyerID")~
    long("sellerID")~
    long("postID")~
    int("storageTaken") ~
    date("startDate") ~
    date("endDate")~
    bool("approved")~
    int("canceled ").? map {
      case transactionID ~ buyerID ~ sellerID ~ postID ~ storageTaken ~ startDate ~ endDate ~ approved ~ canceled =>
        Transaction(storageTaken, startDate.toString(), endDate.toString(),buyerID,sellerID,postID,Some(transactionID))
        //Transaction(storageTaken, startDate.toString(), endDate.toString(),buyerID,sellerID,postID,Some(transactionID))
    }

  def insert(transaction: Transaction): Long = DB.withConnection { implicit conn =>
    createTransactionSql.on(
      'storageTaken -> transaction.storageTaken,
      'startDate -> transaction.startDate,
      'endDate -> transaction.endDate,
      'buyerID -> transaction.buyerID,
      'sellerID -> transaction.sellerID,
      'postID -> transaction.postID
    ).executeInsert(scalar[Long].single)
  }

  def getByID(ID: Long): Option[Transaction] = DB.withConnection { implicit conn =>
    findTransactionByIdSql.on(
      'transactionID -> ID
    ).as(transactionParser.singleOpt)
    //Some(Transaction(10, "2014-01-19 03:14:07", "2014-01-19 03:14:07",1,2,1,Some(1)))
  }
}
