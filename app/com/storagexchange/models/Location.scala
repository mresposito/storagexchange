package com.storagexchange.models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import javax.inject.Singleton
import javax.inject.Inject
import com.typesafe.scalalogging.slf4j.Logging
import java.math.BigDecimal

case class Location(name: String,
 /** 
  * We use BigDecimal because when you try to retrieve
  * Location by id, anorm cannot convert from BigDecimal
  * to Double
  */
  lat: BigDecimal,
  lng: BigDecimal,
  city: String,
  state: String,
  address: String,
  zip: String,
  id: Option[Long] = None)


trait LocationStore {
  def insert(location: Location): Long

  def getById(id: Long): Option[Location]
}

@Singleton
class LocationDAL extends LocationStore {
  
  private[this] val createLocationSql = {
    SQL("""
      INSERT INTO Location
        (name, lat, lng, city, state, address, zip, id)
      VALUES
        ({name}, {lat}, {lng}, {city}, {state}, {address}, {zip}, {id})
    """.stripMargin)
  }
  
  private[this] val selectById = {
    SQL("""
      SELECT *
      FROM Location 
      WHERE id = {id}
      """.stripMargin)
  }

  implicit val locationParser = str("name") ~
    get[BigDecimal]("lat") ~
    get[BigDecimal]("lng") ~
    str("city") ~
    str("state") ~
    str("address") ~
    str("zip") ~
    long("id").? map {
      case name ~ lat ~ lng ~ city ~ state ~ address ~ zip ~ id =>
        Location(name, lat, lng, city, state, address, zip, id)
    }

  def insert(location: Location): Long = DB.withConnection { implicit conn =>
    createLocationSql.on(
      'name -> location.name,
      'lat -> location.lat,
      'lng -> location.lng,
      'city -> location.city,
      'state -> location.state,
      'address -> location.address,
      'zip -> location.zip,
      'id -> location.id
    ).executeInsert(scalar[Long].single)
  }

  def getById(id: Long): Option[Location] = DB.withConnection { implicit conn =>
    selectById.on(
      'id -> id
    ).as(locationParser.singleOpt)
  }

}
