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


/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class SearchAPISpec extends Specification with PostTest {
  
  val client = new EmbeddedElasticClient
  val search = new ElasticSearch(client)
  val IndexPosts = BeforeHook {
    for {
      index <- search.createIndices
	    post <- search.insertPost(post1)
    } yield (post)
  }
  
  "A Search API" should {
    
    "Get posts should" in {
	    "give 200 if there are posts" in IndexPosts {
	      val Some(page) = route(FakeRequest(GET, routes.SearchAPI.getPosts.url))
	      client.tearDown
	      status(page) must beEqualTo(OK)
	    }
	    "have the post description in the body" in IndexPosts {
	      val Some(page) = route(FakeRequest(GET, routes.SearchAPI.getPosts.url))
	      client.tearDown
	      contentAsString(page) must contain(post1.description)
	    }
	    "have the post size in the body" in IndexPosts {
	      val Some(page) = route(FakeRequest(GET, routes.SearchAPI.getPosts.url))
	      client.tearDown
	      contentAsString(page) must contain(post1.storageSize.toString)
	    }
    }
  }
}