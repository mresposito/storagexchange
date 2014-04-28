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
  startDate: Timestamp,
  endDate: Timestamp,
  postID: Long,
  buyerEmail: String,
  sellerEmail: Option[String] = None,
  transactionID: Option[Long] = None,
  approved: Boolean = false,
  canceled: Boolean = false)

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
  def cancelAsBuyer(transactionID: Long, userEmail: String): Int
  def cancelAsSeller(transactionID: Long, userEmail: String): Int
}

// Actual implementation of Transaction Store method
@Singleton
class TransactionDAL extends TransactionStore {

  private[this] val createTransactionSql = {
    SQL("""
      INSERT INTO Transaction
        (storageTaken, startDate, endDate, postID, buyerEmail, sellerEmail)
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
       WHERE buyerEmail = {buyerEmail}
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

  private[this] val cancelAsBuyerSql = {
    SQL("""
       Update Transaction
       SET canceled=1
       WHERE transactionID = {transactionID} AND buyerEmail = {userEmail}
    """.stripMargin)
  }

  private[this] val cancelAsSellerSql = {
    SQL("""
       Update Transaction
       SET canceled=1
       WHERE transactionID = {transactionID} AND sellerEmail = {userEmail}
    """.stripMargin)
  }

  implicit val transactionParser = 
    long("transactionID")~
    str("buyerEmail")~
    str("sellerEmail")~
    long("postID")~
    int("storageTaken") ~
    long("startDate") ~
    long("endDate")~
    bool("approved")~
    bool("canceled") map {
      case transactionID ~buyerEmail ~ sellerEmail ~ postID ~ storageTaken 
        ~ startDate ~ endDate ~ approved ~ canceled =>
        Transaction(storageTaken, new Timestamp(startDate), new Timestamp(endDate), postID, 
          buyerEmail, Some(sellerEmail), Some(transactionID), approved, canceled)
    }

  def insert(transaction: Transaction): Long = DB.withConnection { implicit conn =>
    createTransactionSql.on(
      'storageTaken -> transaction.storageTaken,
      'startDate -> transaction.startDate.getTime(),
      'endDate -> transaction.endDate.getTime(),
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

  def cancelAsBuyer(transactionID: Long, buyerEmail: String): Int = DB.withConnection { implicit conn =>
    cancelAsBuyerSql.on(
      'transactionID -> transactionID,
      'userEmail -> buyerEmail
    ).executeUpdate()
  }

  def cancelAsSeller(transactionID: Long, sellerEmail: String): Int = DB.withConnection { implicit conn =>
    cancelAsSellerSql.on(
      'transactionID -> transactionID,
      'userEmail -> sellerEmail
    ).executeUpdate()
  }
}
