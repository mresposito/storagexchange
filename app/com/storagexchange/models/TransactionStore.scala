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
  /*def getByID(ID: Long): Option[Transaction]
  def getByPostID(postID: Long): Option[Transaction]
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

  /*private[this] val findTransactionByIdSql = {
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

  private[this] val updateTransactionById = {
    SQL("""
       Update Transaction
       SET description={description}, storageSize={storageSize}
       WHERE transactionID = {transactionID} AND
        email = {email}
    """.stripMargin)
  }

  private[this] val removeTransactionByIdSql = {
    SQL("""
       DELETE FROM Transaction
       WHERE transactionID = {transactionID} AND
        email = {email}
    """.stripMargin)
  }

  implicit val transactionParser = 
    str("email") ~
    str("description") ~
    int("storageSize") ~
    long("transactionID").? map {
      case email ~ description ~ storageSize ~ transactionID =>
        Transaction(email, description, storageSize,transactionID)
    }*/

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

  /*def getById(id: Long): Option[Transaction] = DB.withConnection { implicit conn =>
    findTransactionByIdSql.on(
      'transactionID -> id
    ).as(transactionParser.singleOpt)
  }*/
}
