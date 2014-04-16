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

case class TransactionByEmail(storageTaken: Int,
  startDate: String,
  endDate: String,
  buyerEmail: String,
  sellerEmail: String,
  postID: Long,
  transactionID: Option[Long] = None)

case class TransactionDetails(transactionID: Long,
  storageTaken: Int,
  startDate: String,
  endDate: String,
  buyerID: Long,
  sellerID: Long,
  buyerEmail: String,
  sellerEmail: String,
  postID: Long,
  confirmed: Boolean,
  canceled: Int)

/**
 * Methods that we will be using from
 * the interface to the database
 */
trait TransactionStore {
  def insert(trasaction: Transaction): Long
  def insertByEmail(trasaction: TransactionByEmail): Long
  def getByID(ID: Long): Option[TransactionDetails]
  def getByPostID(postID: Long): List[TransactionDetails]
  def getByBuyerID(buyerID: Long): List[TransactionDetails]
  def getByBuyerEmail(buyerEmail: String): List[TransactionDetails]
  def getBySellerID(sellerID: Long): List[TransactionDetails]
  def getBySellerEmail(sellerEmail: String): List[TransactionDetails]
}

// Actual implementation of Transaction Store method
@Singleton
class TransactionDAL extends TransactionStore {
  
  private[this] val createTransactionSql = {
    SQL("""
      INSERT INTO Transaction
        (storageTaken, startDate, endDate, buyerID, sellerID, buyerEmail, sellerEmail, postID)
      VALUES
        ({storageTaken}, {startDate}, {endDate}, {buyerID}, {sellerID}, (SELECT email FROM User WHERE userID={buyerID}), (SELECT email FROM User WHERE userID={sellerID}), {postID})
    """.stripMargin)
  }

  private[this] val createTransactionByEmailSql = {
    SQL("""
      INSERT INTO Transaction
        (storageTaken, startDate, endDate, buyerID, sellerID, buyerEmail, sellerEmail, postID)
      VALUES
        ({storageTaken}, {startDate}, {endDate}, (SELECT userID FROM User WHERE email={buyerEmail}), (SELECT userID FROM User WHERE email={sellerEmail}), {buyerEmail}, {sellerEmail}, {postID})
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

  private[this] val findTransactionByBuyerIDSql = {
    SQL("""
       SELECT *
       FROM Transaction
       WHERE buyerID = {buyerID}
    """.stripMargin)
  }

  private[this] val findTransactionByBuyerEmailSql = {
    SQL("""
       SELECT *
       FROM Transaction
       WHERE buyerID = (SELECT userID FROM User WHERE email={buyerEmail})
    """.stripMargin)
  }

  private[this] val findTransactionBySellerIDSql = {
    SQL("""
       SELECT *
       FROM Transaction
       WHERE sellerID = {sellerID}
    """.stripMargin)
  }

  private[this] val findTransactionBySellerEmailSql = {
    SQL("""
       SELECT *
       FROM Transaction
       WHERE sellerEmail = {sellerEmail}
    """.stripMargin)
  }

  implicit val transactionParser = 
    long("transactionID")~
    long("buyerID")~
    long("sellerID")~
    str("buyerEmail")~
    str("sellerEmail")~
    long("postID")~
    int("storageTaken") ~
    date("startDate") ~
    date("endDate")~
    bool("approved")~
    int("canceled") map {
      case transactionID ~ buyerID ~ sellerID ~buyerEmail ~ sellerEmail ~ postID ~ storageTaken ~ startDate ~ endDate ~ approved ~ canceled =>
        TransactionDetails(transactionID,storageTaken, startDate.toString(), endDate.toString(),buyerID,sellerID, buyerEmail, sellerEmail, postID,approved,canceled)
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

  def insertByEmail(transaction: TransactionByEmail): Long = DB.withConnection { implicit conn =>
    createTransactionByEmailSql.on(
      'storageTaken -> transaction.storageTaken,
      'startDate -> transaction.startDate,
      'endDate -> transaction.endDate,
      'buyerEmail -> transaction.buyerEmail,
      'sellerEmail -> transaction.sellerEmail,
      'postID -> transaction.postID
    ).executeInsert(scalar[Long].single)
  }

  def getByID(ID: Long): Option[TransactionDetails] = DB.withConnection { implicit conn =>
    findTransactionByIdSql.on(
      'transactionID -> ID
    ).as(transactionParser.singleOpt)
  }

  def getByPostID(postID: Long): List[TransactionDetails] = DB.withConnection { implicit conn =>
    findTransactionByPostIDSql.on(
      'postID -> postID
    ).as(transactionParser *)
  }

  def getByBuyerID(buyerID: Long): List[TransactionDetails] = DB.withConnection { implicit conn =>
    findTransactionByBuyerIDSql.on(
      'buyerID -> buyerID
    ).as(transactionParser *)
  }

  def getByBuyerEmail(buyerEmail: String): List[TransactionDetails] = DB.withConnection { implicit conn =>
    findTransactionByBuyerEmailSql.on(
      'buyerEmail -> buyerEmail
    ).as(transactionParser *)
  }

  def getBySellerID(sellerID: Long): List[TransactionDetails] = DB.withConnection { implicit conn =>
    findTransactionBySellerIDSql.on(
      'sellerID -> sellerID
    ).as(transactionParser *)
  }

  def getBySellerEmail(sellerEmail: String): List[TransactionDetails] = DB.withConnection { implicit conn =>
    findTransactionBySellerEmailSql.on(
      'sellerEmail -> sellerEmail
    ).as(transactionParser *)
  }
}
