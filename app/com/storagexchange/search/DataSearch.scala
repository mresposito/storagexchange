package com.storagexchange.search

import com.storagexchange.models._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.KeywordAnalyzer
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mapping.FieldType._
import com.sksamuel.elastic4s.ElasticClient._
import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.update.UpdateResponse
import org.elasticsearch.action.delete.DeleteResponse
import java.math.BigDecimal
import org.elasticsearch.common.unit.DistanceUnit

trait DataSearch {

  def insertPost(post: Post): Future[IndexResponse]
  def insertPost(post: Post, lat: Double, lng: Double): Future[IndexResponse]

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
case class LocationQuery(lat: Double, lng: Double, range: Double) extends SearchBuilder
case class Offset(start: Int, limit: Int = 10) extends SearchBuilder

@Singleton
class ElasticSearch @Inject() (clientInjector: ElasticClientInjector,
   locationStore: LocationStore) extends DataSearch {
   import clientInjector._

  def insertPost(post: Post, lat: Double, lng: Double): Future[IndexResponse] = client execute {
    index into "posts" -> "post" fields (
      "id" -> post.postID.get,
      "description" -> post.description,
      "storageSize" -> post.storageSize,
      "location" -> (lat.doubleValue().toString() + ", " + lng.doubleValue().toString()))
  }

  def insertPost(post: Post): Future[IndexResponse] = client execute {
    val location: Option[Location] = locationStore.getById(post.locationID)
    index into "posts" -> "post" fields (
      "id" -> post.postID.get,
      "description" -> post.description,
      "storageSize" -> post.storageSize,
      "location" -> location.map(_.toGeo).getOrElse("0, 0")
    )
  }

  def createIndices: Future[CreateIndexResponse] = client execute {
    create index "posts" mappings (
      "post" as (
        "id" typed IntegerType,
        "description" typed StringType,
        "storageSize" typed IntegerType,
        "location" typed GeoPointType)
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
    facet range "size" field "storageSize" range(0 -> 100) range(
      101 -> 300) range(301 ->1000) range(1001->99999)
  } sort {
    by field "id"
  }

  def getPosts(searches: SearchBuilder*): Future[SearchResponse] = client execute {
    searches.foldLeft(defaultSearch) {
      (red: SearchDefinition, build: SearchBuilder) => build match {
		    case SearchFilter(field, gt, lt) => red filter {
		      rangeFilter(field) lte lt.toString gte gt.toString
		    }
        case LocationQuery(latit, long, range) => red filter {
          geoDistance("location") lat latit lon long distance (range, DistanceUnit.MILES) 
        }
        case Query(term) => red query term
		    case Offset(at, max) => red start at limit max
	    }
    }
  }
}
