package com.storagexchange.controllers

import play.api.test._
import play.api.test.Helpers._
import play.api.Play.current
import org.specs2.mutable._
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._
import com.storagexchange.utils._
import com.storagexchange.models.UniversityDAL
import com.storagexchange.models.UniversityStore
import com.storagexchange.models.UniversityTest

class SearchAPISpec extends Specification with PostTest 
  with UniversityTest {
  
  import JsonSearchFormatters._
  
  def searchRequest(body: String) = route {
    FakeRequest(POST, routes.SearchAPI.getPosts.url).
	    withJsonBody(Json.parse(body))
  }
  
  "A Search API" should {
    
    "give 200 with json {}" in RunningApp {
      val Some(page) = searchRequest("{}")
      status(page) must beEqualTo(OK)
    }
    "give 200 for universities" in RunningApp {
      val Some(page) = route(withSession(
        FakeRequest(GET, routes.SearchAPI.getUniversities.url)))
      status(page) must beEqualTo(OK)
    }
    "if no universities, return empty JSON" in RunningApp {
      val Some(page) = route(withSession(
        FakeRequest(GET, routes.SearchAPI.getUniversities.url)))
      contentAsString(page) must contain("[]")
    }
    "contain stanford when inserting stanford" in InsertUniversity {
      val Some(page) = route(withSession(
        FakeRequest(GET, routes.SearchAPI.getUniversities.url)))
      contentAsString(page) must contain(testUniversity.name)
    }
    "contain stanford's ID when inserting stanford" in InsertUniversity {
      val Some(page) = route(withSession(
        FakeRequest(GET, routes.SearchAPI.getUniversities.url)))
      contentAsString(page) must contain("1")
    }
  }
}