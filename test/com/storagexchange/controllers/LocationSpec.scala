package com.storagexchange.controllers

import com.storagexchange.models._
import com.storagexchange.utils._
import play.api.test._
import play.api.test.Helpers._
import play.api.Play.current
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import com.storagexchange.models.Location
import java.math.BigDecimal

trait LocationTest extends Specification {
   
  val locationStore: LocationStore = new LocationDAL()

  val x= new BigDecimal(15.000000).setScale(6,BigDecimal.ROUND_HALF_UP)

  val location = Location("Home", x, x, "Cerritos", "California", "12640 Misty Place", "90703")

  val y = new BigDecimal(37.000000).setScale(6,BigDecimal.ROUND_HALF_UP)
  val z = new BigDecimal(122.000000).setScale(6,BigDecimal.ROUND_HALF_UP)

  val loc1 = Location("Stanford University", y, z, "Stanford", "California", "450 Serra Mall", "94305", Some(2))

}
