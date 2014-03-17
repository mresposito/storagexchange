package com.storagexchange.models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import javax.inject.Singleton
import javax.inject.Inject
import com.typesafe.scalalogging.slf4j.Logging

case class Location(name: String,
  //TODO: obviously should be double primitive type. figure out how to use within locationParser as double("lat")
  lat: Long,
  lng: Long,
  city: String,
  state: String,
  address: String,
  id: Option[Long] = None)


trait LocationStore {
  //TODO: add some more interesting and useful SQL queries
  def insert(location: Location): Long

  def getAll(): List[Location]
}

@Singleton
class LocationDAL extends LocationStore {
  
  private[this] val createLocationSql = {
    SQL("""
      INSERT INTO Location
        (name, lat, lng, city, state, address, id)
      VALUES
        ({name}, {lat}, {lng}, {city}, {state}, {address}, {id})
    """.stripMargin)
  }
  
  private[this] val selectLocation = {
    SQL("""
        SELECT * 
        FROM Location
      """.stripMargin)
  }


  implicit val locationParser =
    str("name") ~
    long("lat") ~
    long("lng") ~
    str("city") ~
    str("state") ~
    str("address") ~
    long("id").? map {
      case name ~ lat ~ lng ~ city ~ state ~ address ~ id =>
        Location(name, lat, lng, city, state, address, id)
    }

  def insert(location: Location): Long = DB.withConnection { implicit conn =>
    createLocationSql.on(
      'name -> location.name,
      'lat -> location.lat,
      'lng -> location.lng,
      'city -> location.city,
      'state -> location.state,
      'address -> location.address,
      'id -> location.id
    ).executeInsert(scalar[Long].single)
  }

  def getAll(): List[Location] = DB.withConnection { implicit conn =>
    selectLocation.as(locationParser *)
  } 


}
