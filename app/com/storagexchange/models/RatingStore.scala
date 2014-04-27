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
  raterEmail: String,
  rateeEmail: String,
  ratingID: Option[Long] = None)

/**
 * Methods that we will be using from
 * the interface to the database
 */
trait RatingStore{
    def insert(rating : Rating): Long
    def getByID(ratingID : Long): Option[Rating]
    def getAvgByRatee(rateeEmail: String): Option[Double]
}

// Actual implementation of Transaction Store method
@Singleton
class RatingDAL extends RatingStore {
    private[this] val createRatingSql = {
    SQL("""
      INSERT INTO Rating
        (transactionID, score, raterEmail, rateeEmail)
      VALUES
        ({transactionID}, {score}, {raterEmail}, {rateeEmail})
    """.stripMargin)
  }

  private[this] val findRatingByIdSql = {
    SQL("""
      SELECT * FROM Rating
        WHERE ratingID = {ratingID}
    """.stripMargin)
  }

  private[this] val getAvgByRateeSql = {
    SQL("""
      SELECT AVG(SCORE * 10) AS avgScore FROM RATING WHERE rateeEmail = {rateeEmail} GROUP BY rateeEmail
    """.stripMargin)
  }

  implicit val ratingParser = 
    long("transactionID")~
    int("score")~
    str("raterEmail")~
    str("rateeEmail")~
    long("ratingID") map {
      case transactionID ~ score ~ raterEmail ~ rateeEmail ~ ratingID =>
        Rating(transactionID, score, raterEmail, rateeEmail, Some(ratingID))
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

  def getAvgByRatee(rateeEmail: String): Option[Double] = DB.withConnection { implicit conn =>
    getAvgByRateeSql.on(
      'rateeEmail -> rateeEmail
    ).as(ratingAvgParser.singleOpt)
  }
}