package com.storagexchange.models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import javax.inject.Singleton
import javax.inject.Inject
import com.typesafe.scalalogging.slf4j.Logging

case class University(name: String,
  website: String,
  logo: String,
  colors: Option[String] = None)

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
        (name, website, logo, colors)
      VALUES
        ({name}, {website}, {logo}, {colors})
    """.stripMargin)
  }

  private[this] val getUniversitiesByCity = {
    SQL(s"""
       SELECT *
       FROM UNIVERSITY
       WHERE city = {city}
    """.stripMargin)
  }
  
  implicit val universityParser = 
    str("name") ~
    str("website") ~
    str("logo") ~
    str("colors").? map {
      case name ~ website ~ logo ~ colors =>
        University(name, website, logo, colors)
    }

  def insert(university: University): Long = DB.withConnection { implicit conn =>
    createUniversitySql.on(
      'name -> university.name,
      'website -> university.website,
      'logo -> university.logo,
      'colors -> university.colors
    ).executeInsert(scalar[Long].single)
  }
 
  def getByCity(city: String): Option[University] = DB.withConnection { implicit conn =>
        getUniversitiesByCity.on(
          'city -> city
        ).as(universityParser.singleOpt)
  }  

}
