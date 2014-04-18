package com.storagexchange.models

import java.sql.Timestamp
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import javax.inject.Singleton
import javax.inject.Inject

case class Transaction(
  storageTaken: Int,
  startDate: String,
  endDate: String,
  postID: Long,
  buyerEmail: String,
  sellerEmail: Option[String] = None,
  transactionID: Option[Long] = None,
  approved: Option[Boolean] = None,
  canceled: Option[Int] = None)

/**
 * Methods that we will be using from
 * the interface to the database
 */
trait TransactionStore {
  def insert(trasaction: Transaction): Long
  def getByID(ID: Long): Option[Transaction]
  def getByPostID(postID: Long): List[Transaction]
  def getByBuyerEmail(buyerEmail: String): List[Transaction]
  def getBySellerEmail(sellerEmail: String): List[Transaction]
  def approve(transactionID: Long, sellerEmail: String): Int
}

// Actual implementation of Transaction Store method
@Singleton
class TransactionDAL extends TransactionStore {

  private[this] val createTransactionSql = {
    SQL("""
      INSERT INTO Transaction
        (storageTaken, startDate, endDate, buyerID, sellerID, buyerEmail, sellerEmail, postID)
      VALUES
        ({storageTaken}, {startDate}, {endDate}, {postID}, {buyerEmail}, 
          (SELECT email FROM Post WHERE postID={postID}))
    """.stripMargin)
  }

  private[this] val findTransactionByIdSql = {
    SQL("""
       SELECT *
       FROM Transaction
       WHERE transactionID = {transactionID}
    """.stripMargin)
  }

  private[this] val findTransactionByPostIDSql = {
    SQL("""
       SELECT *
       FROM Transaction
       WHERE postID = {postID}
    """.stripMargin)
  }

  private[this] val findTransactionByBuyerEmailSql = {
    SQL("""
       SELECT *
       FROM Transaction
       WHERE buyerID = (SELECT userID FROM User WHERE email={buyerEmail})
    """.stripMargin)
  }

  private[this] val findTransactionBySellerEmailSql = {
    SQL("""
       SELECT *
       FROM Transaction
       WHERE sellerEmail = {sellerEmail}
    """.stripMargin)
  }

  private[this] val approveSql = {
    SQL("""
       Update Transaction
       SET approved=1
       WHERE transactionID = {transactionID} AND sellerEmail = {sellerEmail}
    """.stripMargin)
  }

  implicit val transactionParser = 
    long("transactionID")~
    str("buyerEmail")~
    str("sellerEmail")~
    long("postID")~
    int("storageTaken") ~
    date("startDate") ~
    date("endDate")~
    bool("approved")~
    int("canceled") map {
      case transactionID ~buyerEmail ~ sellerEmail ~ postID ~ storageTaken ~ startDate ~ endDate ~ approved ~ canceled =>
        Transaction(storageTaken, startDate.toString(), endDate.toString(), postID, 
          buyerEmail, Some(sellerEmail), Some(transactionID), Some(approved),Some(canceled))
    }

  def insert(transaction: Transaction): Long = DB.withConnection { implicit conn =>
    createTransactionSql.on(
      'storageTaken -> transaction.storageTaken,
      'startDate -> transaction.startDate,
      'endDate -> transaction.endDate,
      'postID -> transaction.postID,
      'buyerEmail -> transaction.buyerEmail
    ).executeInsert(scalar[Long].single)
  }

  def getByID(ID: Long): Option[Transaction] = DB.withConnection { implicit conn =>
    findTransactionByIdSql.on(
      'transactionID -> ID
    ).as(transactionParser.singleOpt)
  }

  def getByPostID(postID: Long): List[Transaction] = DB.withConnection { implicit conn =>
    findTransactionByPostIDSql.on(
      'postID -> postID
    ).as(transactionParser *)
  }

  def getByBuyerEmail(buyerEmail: String): List[Transaction] = DB.withConnection { implicit conn =>
    findTransactionByBuyerEmailSql.on(
      'buyerEmail -> buyerEmail
    ).as(transactionParser *)
  }

  def getBySellerEmail(sellerEmail: String): List[Transaction] = DB.withConnection { implicit conn =>
    findTransactionBySellerEmailSql.on(
      'sellerEmail -> sellerEmail
    ).as(transactionParser *)
  }

  def approve(transactionID: Long, sellerEmail: String): Int = DB.withConnection { implicit conn =>
    approveSql.on(
      'transactionID -> transactionID,
      'sellerEmail -> sellerEmail
      ).executeUpdate()
  }
}
