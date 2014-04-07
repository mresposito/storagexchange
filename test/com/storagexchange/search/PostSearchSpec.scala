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

class PostSearchSpec extends FlatSpec 
	with MockitoSugar with ElasticSugar {
  val post1 = Post("m@e.com", "This is the first post", 95, Some(1))
  val post2 = Post("hsimpson@uis.edu", "Homer no function beer well without", 45, Some(2))
  val atMost: Duration = Duration(10, "seconds")

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
    assert(2 === resp.getHits.totalHits())
  }
  
  "filtering" should "return 1 if less than 50" in {
   val resp = client.sync.execute {
      search in "posts" types "post" filter {
        rangeFilter("storageSize") lte "50"
      }
   }
    assert(1 === resp.getHits.totalHits())
  }
  it should "return 1 if greater than 50" in {
   val resp = client.sync.execute {
      search in "posts" types "post" filter {
        rangeFilter("storageSize") gte "50"
      }
   }
    assert(1 === resp.getHits.totalHits())
  }
  it should "return none if invalid range" in {
   val resp = client.sync.execute {
      search in "posts" types "post" filter {
        rangeFilter("storageSize") lte "0"
      }
   }
    assert(0 === resp.getHits.totalHits())
  }
  
  "data search" should "return one hit by keyword" in {
   val resp = Await.result(dataSearch.getPosts, atMost)
    assert(2 === resp.getHits.totalHits())
  }
  it should "contain the first post description" in {
   val resp = Await.result(dataSearch.getPosts, atMost)
    assert(resp.toString().contains(post1.description))
  }
  it should "contain the second post description" in {
   val resp = Await.result(dataSearch.getPosts, atMost)
    assert(resp.toString().contains(post2.description))
  }
  it should "not contain an email address" in {
   val resp = Await.result(dataSearch.getPosts, atMost)
    assert(! resp.toString().contains(post2.email))
  }
}