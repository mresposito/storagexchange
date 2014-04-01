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
  id: Option[Long] = None)

/**
 * Methods that we will be using from
 * the interface to the database
 */
trait UniversityStore {

  def insert(university: University): Long
  
  def getByCity(city: String): Option[University]

  def getUniversitiesByName(name: String): Option[University]

}

@Singleton
class UniversityDAL extends UniversityStore with Logging {
  
  private[this] val createUniversitySql = {
    SQL("""
      INSERT INTO University
        (locationID, name, website, logo, colors)
      VALUES
        ({locationID}, {name}, {website}, {logo}, {colors})
    """.stripMargin)
  }

  private[this] val getUniversitiesByCity = {
    SQL("""
       SELECT *
       FROM University
       WHERE city = {city}
    """.stripMargin)
  }
  
  private[this] val getUnivByName = {
    SQL("""
        SELECT *
        FROM University
        WHERE name = {name}
      """.stripMargin)
  }

  implicit val universityParser = 
    long("locationID") ~
    str("name") ~
    str("website") ~
    str("logo") ~
    str("colors") ~
    long("id").? map {
      case locationID ~ name ~ website ~ logo ~ colors ~ id =>
        University(locationID, name, website, logo, colors, id)
    }

  def insert(university: University): Long  = DB.withConnection { implicit conn =>
    try { 
      createUniversitySql.on(
        'locationID -> university.locationID,
        'name -> university.name,
        'website -> university.website,
        'logo -> university.logo,
        'colors -> university.colors,
        'id -> university.id
      ).executeInsert(scalar[Long].single)
    } catch {
        case e: Exception => {
          logger.debug(e.getMessage())
          println(e.getMessage())
          throw e
        }
    }
  }
 
  def getByCity(city: String): Option[University] = DB.withConnection { implicit conn =>
        getUniversitiesByCity.on(
          'city -> city
        ).as(universityParser.singleOpt)
  } 

  def getUniversitiesByName(name: String): Option[University] = DB.withConnection { implicit conn =>
        getUnivByName.on(
          'name -> name
        ).as(universityParser.singleOpt)
  }

}
