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

  def insert(university: University): Option[Long]
  
  def getByCity(city: String): Option[University]

  def getIdByName(name: String): Option[University]

  def getAll(): List[University]

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
    SQL("""
       SELECT *
       FROM University
       WHERE city = {city}
    """.stripMargin)
  }
  
  private[this] val getUniversityIdByName = {
    SQL("""
        SELECT *
        FROM University
        WHERE name = {name}
      """.stripMargin)
  }

  private[this] val selectUniversity = {
    SQL("""
        SELECT * 
        FROM University
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

  def insert(university: University): Option[Long] = DB.withConnection { implicit conn =>
    createUniversitySql.on(
      'locationID -> university.locationID,
      'name -> university.name,
      'website -> university.website,
      'logo -> university.logo,
      'colors -> university.colors,
      'id -> university.id
    ).executeInsert(scalar[Long].singleOpt)
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

  def getAll(): List[University] = DB.withConnection { implicit conn =>
    selectUniversity.as(universityParser *)
  } 

}
