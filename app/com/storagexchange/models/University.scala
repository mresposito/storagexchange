package com.storagexchange.models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import javax.inject.Singleton
import javax.inject.Inject
import com.typesafe.scalalogging.slf4j.Logging

case class University(locationID: Long,
  name: String,
  website: String,
  logo: String,
  colors: String,
  universityID: Option[Long] = None)

/**
 * Methods that we will be using from
 * the interface to the database
 */
trait UniversityStore {

  def insert(university: University): Long
  
  def getByCity(city: String): Option[University]

}

@Singleton
class UniversityDAL extends UniversityStore {
  
  private[this] val createUniversitySql = {
    SQL("""
      INSERT INTO University
        (locationID, name, website, logo, colors)
      VALUES
        ({locationID}, {name}, {website}, {logo}, {colors})
    """.stripMargin)
  }

  private[this] val getUniversitiesByCity = {
    SQL(s"""
       SELECT *
       FROM UNIVERSITY
       WHERE city = {city}
    """.stripMargin)
  }
  
  private[this] val getUniversityIdByName = {
    SQL(s"""
        SELECT universityID
        FROM UNIVERSITY
        WHERE name = {name}
      """.stripMargin)
  }

  implicit val universityParser = 
    long("locationID") ~
    str("name") ~
    str("website") ~
    str("logo") ~
    str("colors") ~
    long("universityID").? map {
      case locationID ~ name ~ website ~ logo ~ colors ~ universityID =>
        University(locationID, name, website, logo, colors, universityID)
    }

  def insert(university: University): Long = DB.withConnection { implicit conn =>
    createUniversitySql.on(
      'locationID -> university.locationID,
      'name -> university.name,
      'website -> university.website,
      'logo -> university.logo,
      'colors -> university.colors,
      'universityID -> university.universityID
    ).executeInsert(scalar[Long].single)
  }
 
  def getByCity(city: String): Option[University] = DB.withConnection { implicit conn =>
        getUniversitiesByCity.on(
          'city -> city
        ).as(universityParser.singleOpt)
  } 

  def getIdByName(name: String): Option[University] = DB.withConnection { implicit conn =>
        getUniversityIdByName.on(
          'name -> name
        ).as(universityParser.singleOpt)
  }

}
