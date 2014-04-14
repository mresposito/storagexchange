package com.storagexchange.controllers

import play.api.test._
import play.api.test.Helpers._
import play.api.Play.current
import org.specs2.mutable._
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._
import com.storagexchange.utils._
import com.storagexchange.search.EmbeddedElasticClient
import com.storagexchange.search.ElasticSearch
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class SearchAPISpec extends Specification with PostTest {
   args(skipAll = true)
  
  import JsonSearchFormatters._
  
  def searchRequest(body: String) = route { 
    FakeRequest(POST, routes.SearchAPI.getPosts.url).
	    withJsonBody(Json.parse(body))
  }
  
  "A Search API" should {
    
    "Get posts should" in {
      "give 200 with json {}" in RunningApp {
	      val Some(page) = searchRequest("{}")
	      status(page) must beEqualTo(OK)
      }
      "give 200 with json {'query'}" in RunningApp {
	      val Some(page) = searchRequest("""
	        {"query": {"term": "hello"}}""")
	      status(page) must beEqualTo(OK)
      }
      "give 200 with json {'query'}" in RunningApp {
	      val Some(page) = searchRequest("""{
          "query": {"term": "hello"},
          "filters": []
        }""")
	      status(page) must beEqualTo(OK)
      }
      "give 400 for invalid json" in RunningApp {
	      val Some(page) = searchRequest("""{"page": []}""")
	      status(page) must beEqualTo(BAD_REQUEST)
      }
      "invalid json should contain error message" in RunningApp {
	      val Some(page) = searchRequest("""{"page": []}""")
	      contentAsString(page) must contain("Could not parse Json")
      }
	    "give 200 if there are posts" in CreatePosts {
	      val Some(page) = searchRequest("{}")
	      status(page) must beEqualTo(OK)
	    }
	    "have the post description in the body" in CreatePosts {
	      val Some(page) = searchRequest("{}")
	      contentAsString(page) must contain(post1.description)
	      contentAsString(page) must contain(post2.description)
	    }
	    "have the post size in the body" in CreatePosts {
	      val Some(page) = searchRequest("{}")
	      contentAsString(page) must contain(post1.storageSize.toString)
	      contentAsString(page) must contain(post2.storageSize.toString)
	    }
    }
  }
}