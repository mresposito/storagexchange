package com.storagexchange.search

import com.storagexchange.models._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.KeywordAnalyzer
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mapping.FieldType._
import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.update.UpdateResponse
import org.elasticsearch.action.delete.DeleteResponse

trait DataSearch {

  def insertPost(post: Post): Future[IndexResponse]
  def getPosts: Future[SearchResponse] = getPosts()
  def getPosts(searches: SearchBuilder*): Future[SearchResponse]

	def deletePost(id: Long): Future[DeleteResponse]
  def updatePost(post: Post): Future[UpdateResponse] 

  def createIndices: Future[CreateIndexResponse]
  def deleteIndices: Unit
}

trait SearchBuilder
case class SearchFilter(field: String, gt: Int, lt: Int) extends SearchBuilder
case class Query(term: String) extends SearchBuilder

@Singleton
class ElasticSearch @Inject() (clientInjector: ElasticClientInjector) extends DataSearch {
  import clientInjector._
  
  def insertPost(post: Post): Future[IndexResponse] = client execute {
    index into "posts" -> "post" fields (
      "id" -> post.postID.get,
      "description" -> post.description,
      "storageSize" -> post.storageSize)
  }
  
  def createIndices: Future[CreateIndexResponse] = client execute {
    create index "posts" mappings (
      "post" as (
        "id" typed IntegerType,
        "description" typed StringType,
        "storageSize" typed IntegerType)
    )
  }
  def deleteIndices = client execute {
    delete index "posts"
  }

  def deletePost(postId: Long): Future[DeleteResponse] = for {
    resp <- makeIdRequest(postId)
    delete <- mkDeletion(resp.getHits().hits().head.getId())
  } yield (delete)
  
  private def mkDeletion(esPostId: String): Future[DeleteResponse] = client execute {
    delete(esPostId) from "posts" types "post"
  }
  
  private def makeIdRequest(postID: Long): Future[SearchResponse] = client.execute {
    search in "posts" types "post" query { term("id", postID) }
  }
  
  def updatePost(post: Post): Future[UpdateResponse] = for {
    resp <- makeIdRequest(post.postID.get)
    update <- mkUpdate(post, resp.getHits().hits().head.getId())
  } yield (update)
  
  private def mkUpdate(post: Post, id: String): Future[UpdateResponse] = client execute {
    update(id).in("posts/post").doc(
      "description" -> post.description,
      "storageSize" -> post.storageSize)
  }

  private def defaultSearch = search in "posts" types "post" facets {
    facet range "size" field "storageSize" range(0 -> 60) range(60 -> 100)
  }
  
  def getPosts(searches: SearchBuilder*): Future[SearchResponse] = client execute {
    searches.foldLeft(defaultSearch) {
      (red: SearchDefinition, build: SearchBuilder) => build match {
		    case SearchFilter(field, gt, lt) => red filter {
		      rangeFilter(field) lte lt.toString gte gt.toString
		    }
		    case Query(term) => red query term
	    }
    }
  }
}
