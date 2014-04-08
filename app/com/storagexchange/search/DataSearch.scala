package com.storagexchange.search

import org.elasticsearch.node.NodeBuilder.nodeBuilder
import com.storagexchange.models._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.KeywordAnalyzer
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mapping.FieldType._
import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.Future
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.common.settings.ImmutableSettings
import java.io.File
import java.util.UUID

trait ElasticClientInjector {
  val client: ElasticClient
  def tearDown: Unit = {}
  def close = client.close
}

class LocalElasticClient extends ElasticClientInjector {
  val client = ElasticClient.local
}

class GenericClient(val client: ElasticClient) extends ElasticClientInjector

class EmbeddedElasticClient extends ElasticClientInjector {
  
  val tempFile = File.createTempFile("elasticsearchtests", "tmp")
  val homeDir = new File(tempFile.getParent + "/" + UUID.randomUUID().toString)
  homeDir.mkdir()
  homeDir.deleteOnExit()
  tempFile.deleteOnExit()

  val settings = ImmutableSettings.settingsBuilder()
    .put("node.http.enabled", false)
    .put("http.enabled", false)
    .put("path.home", homeDir.getAbsolutePath)
    .put("index.number_of_shards", 1)
    .put("index.number_of_replicas", 0)

  val client = ElasticClient.local(settings.build)
}

trait DataSearch {

  def insertPost(post: Post): Future[IndexResponse]
  def getPosts: Future[SearchResponse] = getPosts()
  def getPosts(searches: SearchBuilder*): Future[SearchResponse]

  def createIndices: Future[CreateIndexResponse]
  def deleteIndices: Unit
}

trait SearchBuilder
case class Filter(field: String, gt: Int, lt: Int) extends SearchBuilder
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
  
  def getPosts(searches: SearchBuilder*): Future[SearchResponse] = client execute {
    searches.foldLeft(search in "posts" types "post") {
      (red: SearchDefinition, build: SearchBuilder) => build match {
		    case Filter(field, gt, lt) => red filter {
		      rangeFilter(field) lte lt.toString gte gt.toString
		    }
		    case Query(term) => red query term
	    }
    }
  }
}