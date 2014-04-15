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

/**
 * Methods that we will be using from
 * the interface to the database
 */
trait TransactionStore {
  def insert(trasaction: Transaction): Long
  def insertByEmail(trasaction: TransactionByEmail): Long
  def getByID(ID: Long): Option[Transaction]
  def getByPostID(postID: Long): List[Transaction]
  def getByBuyerID(buyerID: Long): List[Transaction]
  def getByBuyerUsername(buyerUsername: String): List[Transaction]
  def getBySellerID(sellerID: Long): List[Transaction]
  def getBySellerUsername(sellerUsername: String): List[Transaction]
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

  private[this] val createTransactionByEmailSql = {
    SQL("""
      INSERT INTO Transaction
        (storageTaken, startDate, endDate, buyerID, sellerID, postID)
      VALUES
        ({storageTaken}, {startDate}, {endDate}, (SELECT userID FROM User WHERE email={buyerEmail}), (SELECT userID FROM User WHERE email={sellerEmail}), {postID})
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

  private[this] val findTransactionByBuyerUsernameSql = {
    SQL("""
       SELECT *
       FROM Transaction
       WHERE buyerID = (SELECT userID FROM User WHERE email={buyerUsername})
    """.stripMargin)
  }

  private[this] val findTransactionBySellerIDSql = {
    SQL("""
       SELECT *
       FROM Transaction
       WHERE sellerID = {sellerID}
    """.stripMargin)
  }

  private[this] val findTransactionBySellerUsernameSql = {
    SQL("""
       SELECT *
       FROM Transaction
       WHERE sellerID = (SELECT userID FROM User WHERE email={sellerUsername})
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

  def getByBuyerID(buyerID: Long): List[Transaction] = DB.withConnection { implicit conn =>
    findTransactionByBuyerIDSql.on(
      'buyerID -> buyerID
    ).as(transactionParser *)
  }

  def getByBuyerUsername(buyerUsername: String): List[Transaction] = DB.withConnection { implicit conn =>
    findTransactionByBuyerUsernameSql.on(
      'buyerUsername -> buyerUsername
    ).as(transactionParser *)
  }

  def getBySellerID(sellerID: Long): List[Transaction] = DB.withConnection { implicit conn =>
    findTransactionBySellerIDSql.on(
      'sellerID -> sellerID
    ).as(transactionParser *)
  }

  def getBySellerUsername(sellerUsername: String): List[Transaction] = DB.withConnection { implicit conn =>
    findTransactionBySellerUsernameSql.on(
      'sellerUsername -> sellerUsername
    ).as(transactionParser *)
  }
}
