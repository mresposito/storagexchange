package com.storagexchange.models

import java.sql.Timestamp
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import javax.inject.Singleton
import javax.inject.Inject

case class Rating(
  transactionID: Long,
  score: Int,
  raterEmail: Option[String],
  rateeEmail: Option[String],
  ratingID: Option[Long] = None)

/**
 * Methods that we will be using from
 * the interface to the database
 */
trait RatingStore{
    def insert(rating : Rating): Long
    def getByID(ratingID : Long): Option[Rating]
    def getByTransactionID(transactionID : Long): Option[Rating]
    def getAvgByRatee(rateeEmail: String): Option[Double]
    def updateByTransactionID(id: Long, score: Int): Int
}

// Actual implementation of Transaction Store method
@Singleton
class RatingDAL extends RatingStore {
    private[this] val createRatingSql = {
    SQL("""
      INSERT INTO Rating
        (transactionID, score, raterEmail, rateeEmail)
      VALUES
        ({transactionID}, {score}, 
          (SELECT buyerEmail FROM Transaction WHERE transactionID = {transactionID}), 
          (SELECT sellerEmail FROM Transaction WHERE transactionID = {transactionID}))
    """.stripMargin)
  }

  private[this] val findRatingByIdSql = {
    SQL("""
      SELECT * FROM Rating
        WHERE ratingID = {ratingID}
    """.stripMargin)
  }

  private[this] val findByTransactionIdSql = {
    SQL("""
      SELECT * FROM Rating
        WHERE transactionID = {transactionID}
    """.stripMargin)
  }

  private[this] val getAvgByRateeSql = {
    SQL("""
      SELECT AVG(SCORE * 10) AS avgScore FROM RATING WHERE rateeEmail = {rateeEmail} GROUP BY rateeEmail
    """.stripMargin)
  }

  private[this] val updateRatingByTransactionIDSql = {
    SQL("""
       Update Rating
       SET score={score}
       WHERE transactionID = {transactionID}
    """.stripMargin)
  }

  implicit val ratingParser = 
    long("transactionID")~
    int("score")~
    str("raterEmail")~
    str("rateeEmail")~
    long("ratingID") map {
      case transactionID ~ score ~ raterEmail ~ rateeEmail ~ ratingID =>
        Rating(transactionID, score, Some(raterEmail), Some(rateeEmail), Some(ratingID))
    }

    implicit val ratingAvgParser = 
      int("avgScore") map {
        case avgScore => avgScore/10.0
    }

  def insert(rating: Rating): Long = DB.withConnection { implicit conn =>
    createRatingSql.on(
      'transactionID -> rating.transactionID,
      'score -> rating.score,
      'raterEmail -> rating.raterEmail,
      'rateeEmail -> rating.rateeEmail
    ).executeInsert(scalar[Long].single)
  }

  def getByID(ID: Long): Option[Rating] = DB.withConnection { implicit conn =>
    findRatingByIdSql.on(
      'ratingID -> ID
    ).as(ratingParser.singleOpt)
  }

  def getByTransactionID(ID: Long): Option[Rating] = DB.withConnection { implicit conn =>
    findByTransactionIdSql.on(
      'transactionID -> ID
    ).as(ratingParser.singleOpt)
  }

  def getAvgByRatee(rateeEmail: String): Option[Double] = DB.withConnection { implicit conn =>
    getAvgByRateeSql.on(
      'rateeEmail -> rateeEmail
    ).as(ratingAvgParser.singleOpt)
  }

  def updateByTransactionID(id: Long, score: Int): Int = 
      DB.withConnection { implicit conn =>
    updateRatingByTransactionIDSql.on(
        'transactionID-> id,
        'score-> score
      ).executeUpdate()
  }
}