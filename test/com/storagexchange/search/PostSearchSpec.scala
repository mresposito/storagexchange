package com.storagexchange.search

import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import com.sksamuel.elastic4s.ElasticDsl._
import org.elasticsearch.common.Priority
import com.storagexchange.controllers.PostTest
import com.storagexchange.models.Post
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.Matchers
import scala.concurrent.Future
import org.elasticsearch.action.search.SearchResponse

class PostSearchSpec extends FlatSpec with Matchers
	with MockitoSugar with ElasticSugar {
  val post1 = Post("m@e.com", "This is the first post", 95, Some(1))
  val post2 = Post("hsimpson@uis.edu", "Homer no function beer well without", 45, Some(2))
  val atMost: Duration = Duration(10, "seconds")
  
  implicit def unrollFuture[A](f: Future[A]):A = Await.result(f, atMost)

  val dataSearch = new ElasticSearch(new GenericClient(client))
  dataSearch.insertPost(post1)
  dataSearch.insertPost(post2)

  client.admin.cluster.prepareHealth().setWaitForEvents(Priority.LANGUID).setWaitForGreenStatus().execute().actionGet

  refresh("posts")
  blockUntilCount(2, "posts")

  client.admin.cluster.prepareHealth().setWaitForEvents(Priority.LANGUID).setWaitForGreenStatus().execute().actionGet

  "post index" should "return two hits" in {
    val resp = client.sync.execute {
      search in "posts" -> "post"
    }
    resp.getHits.totalHits() should equal(2)
  }
  
  "filtering" should "return 1 if less than 50" in {
   val resp = client.sync.execute {
      search in "posts" types "post" filter {
        rangeFilter("storageSize") lte "50"
      }
   }
    resp.getHits.totalHits() should equal(1)
  }
  it should "return 1 if greater than 50" in {
   val resp = client.sync.execute {
      search in "posts" types "post" filter {
        rangeFilter("storageSize") gte "50"
      }
   }
    resp.getHits.totalHits() should equal(1)
  }
  it should "return none if invalid range" in {
   val resp = client.sync.execute {
      search in "posts" types "post" filter {
        rangeFilter("storageSize") lte "0"
      }
   }
    resp.getHits.totalHits() should equal(0)
  }
  
  /**
   * Data Search
   */
  "data search" should "return one hit by keyword" in {
   val resp: SearchResponse = dataSearch.getPosts
    resp.getHits.totalHits() should equal(2)
  }
  it should "contain the first post description" in {
   val resp: SearchResponse = dataSearch.getPosts
   resp.toString() should include(post1.description)
  }
  it should "contain the second post description" in {
   val resp: SearchResponse = dataSearch.getPosts
   resp.toString() should include(post2.description)
  }
  it should "not contain an email address" in {
   val resp: SearchResponse = dataSearch.getPosts
   resp.toString() should not include(post2.email)
  }

  /**
   * SearchBuilder Filter
   */
  "data search with SearchBuilder Filter" should "have 1 item less than 50" in {
   val resp: SearchResponse = dataSearch.getPosts(Filter("storageSize", 0, 50))
   resp.getHits.totalHits() should equal(1)
  }
  it should "include post 2 description for less than 50" in {
   val resp: SearchResponse = dataSearch.getPosts(Filter("storageSize", 0, 50))
   resp.toString should include(post2.description)
  }
  it should "have 1 item more than 50" in {
   val resp: SearchResponse = dataSearch.getPosts(Filter("storageSize", 50, 50000))
   resp.getHits.totalHits() should equal(1)
  }
  it should "include post 2 description for more than 50" in {
   val resp: SearchResponse = dataSearch.getPosts(Filter("storageSize", 50, 50000))
   resp.toString should include(post1.description)
  }
  it should "have nothing on an inexistent range" in {
   val resp: SearchResponse = dataSearch.getPosts(Filter("storageSize", 0, 0))
   resp.getHits.totalHits() should equal(0)
  }
  
  /**
   * SearchBuilder Query
   */
  "data search with SearchBuilder Query" should "find post 1 on query 'first post'" in {
    val resp: SearchResponse = dataSearch.getPosts(Query("first post"))
    resp.toString should include(post1.description)
  }
  it should "not include post 2 on query 'first post'" in {
    val resp: SearchResponse = dataSearch.getPosts(Query("first post"))
    resp.toString should not include(post2.description)
  }
  it should "find post 2 on query 'homer'" in {
    val resp: SearchResponse = dataSearch.getPosts(Query("homer"))
    resp.toString should include(post2.description)
  }
  it should "not include post 1 on query 'homer'" in {
    val resp: SearchResponse = dataSearch.getPosts(Query("homer"))
    resp.toString should not include(post1.description)
  }
  it should "find post 2 on query 'beer'" in {
    val resp: SearchResponse = dataSearch.getPosts(Query("beer"))
    resp.toString should include(post2.description)
  }
  it should "not include post 1 on query 'beer'" in {
    val resp: SearchResponse = dataSearch.getPosts(Query("beer"))
    resp.toString should not include(post1.description)
  }
}