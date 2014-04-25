package com.storagexchange.search

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import com.sksamuel.elastic4s.ElasticDsl._
import org.elasticsearch.common.Priority
import com.storagexchange.controllers.PostTest
import com.storagexchange.models.Post
import scala.concurrent.ExecutionContext.Implicits.global
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.delete.DeleteResponse
import org.elasticsearch.action.update.UpdateResponse

class PostSearchSpec extends FlatSpec with Matchers
	with MockitoSugar with ElasticSugar with PostTest {

  try {
	  blockUntilCount(2, "posts")
  } catch {
    case e: IllegalArgumentException => {
		  refresh("posts")
		  dataSearch.insertPost(post1)
		  dataSearch.insertPost(post2)
		  blockUntilCount(2, "posts")
    }
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
   val resp: SearchResponse = dataSearch.getPosts(SearchFilter("storageSize", 0, 50))
   resp.getHits.totalHits() should equal(1)
  }
  it should "include post 2 description for less than 50" in {
   val resp: SearchResponse = dataSearch.getPosts(SearchFilter("storageSize", 0, 50))
   resp.toString should include(post2.description)
  }
  it should "have 1 item more than 50" in {
   val resp: SearchResponse = dataSearch.getPosts(SearchFilter("storageSize", 50, 50000))
   resp.getHits.totalHits() should equal(1)
  }
  it should "include post 2 description for more than 50" in {
   val resp: SearchResponse = dataSearch.getPosts(SearchFilter("storageSize", 50, 50000))
   resp.toString should include(post1.description)
  }
  it should "have nothing on an inexistent range" in {
   val resp: SearchResponse = dataSearch.getPosts(SearchFilter("storageSize", 0, 0))
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
  
  "deleting posts" should "delete the first post" in {
    val resp: DeleteResponse = dataSearch.deletePost(post1.postID.get)
    resp.isFound() should be(true)
  }
  
  "update post" should "update the second post" in {
    val resp: UpdateResponse = dataSearch.updatePost(post2.copy(storageSize = 3999))
    resp.isCreated() should be(false)
  }
  
  "facets" should "have facets in its response" in {
    val resp: SearchResponse = dataSearch.getPosts
    resp.getFacets().facets().size() should be(1)
  }
  it should "count 2 facets in total" in {
    val resp: SearchResponse = dataSearch.getPosts
    facetToSum(resp) should be(2)
  }
  it should "count 1 facet if query 'beer'" in {
    val resp: SearchResponse = dataSearch.getPosts(Query("beer"))
    facetToSum(resp) should be(1)
  }
  it should "count 2 facet if use range " in {
    val resp: SearchResponse = dataSearch.getPosts(SearchFilter("storageSize", 0, 60))
    facetToSum(resp) should be(2)
  }
  it should "count nothing on invalid query" in {
    val resp: SearchResponse = dataSearch.getPosts(Query("I love ES"))
    facetToSum(resp) should be(0)
  }
  it should "contain 4 facets" in {
    val resp: SearchResponse = dataSearch.getPosts(Query("I love ES"))
    countFacets(resp) should be(4)
  }
  it should "not be effected by offsets" in {
    val resp: SearchResponse = dataSearch.getPosts(Offset(0, 1))
    facetToSum(resp) should be(2)
  }
  
  "offsets" should "find 1 post in offset (0,1)" in {
    val resp: SearchResponse = dataSearch.getPosts(Offset(0,1))
    resp.getHits().hits().length should be(1) 
  } 
  it should "find 1 post in offset (1,1)" in {
    val resp: SearchResponse = dataSearch.getPosts(Offset(1,1))
    resp.getHits().hits().length should be(1) 
  }
}