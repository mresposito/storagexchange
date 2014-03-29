package com.storagexchange.controllers

import com.storagexchange.models._
import com.storagexchange.utils._
import play.api.test._
import play.api.test.Helpers._
import play.api.Play.current
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import com.storagexchange.models.University

trait UniversityTest extends Specification {
   
  val universityStore: UniversityStore = new UniversityDAL()

  val testUniversity = University(5,"University of California, Berkeley", "http://www.berkeley.edu", 
                                  "http://upload.wikimedia.org/wikipedia/commons/f/fc/The_University_of_California_1868.svg",
                                  "Yale Blue, California Gold", Option(5)) 

}
