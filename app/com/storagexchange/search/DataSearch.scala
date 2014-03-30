package com.storagexchange.search

import org.elasticsearch.node.NodeBuilder._
import com.storagexchange.models._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.KeywordAnalyzer
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mapping.FieldType._
import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.Future
import org.elasticsearch.action.index.IndexResponse

trait ElasticClientInjector {
  val client: ElasticClient
  def tearDown: Unit = {}
  def close = client.close
}

class LocalElasticClient extends ElasticClientInjector {
  val client = ElasticClient.local
}

class EmbeddedElasticClient extends ElasticClientInjector {
  val node = nodeBuilder().client(true).node()
  val client = ElasticClient.fromNode(node)
  /**
   * Stop the local run node
   */
  override def tearDown = {
    node.stop()
  }
}

trait DataSearch {

  def insertPost(post: Post): Future[IndexResponse]

  def createIndices: Unit
  def deleteIndices: Unit
}

@Singleton
class ElasticSearch @Inject() (clientInjector: ElasticClientInjector) extends DataSearch {
  import clientInjector._

  def insertPost(post: Post): Future[IndexResponse] = client execute {
    index into "posts" -> "post" fields (
      "id" -> post.postID.get,
      "description" -> post.description,
      "storageSize" -> post.storageSize)
  }

  def createIndices = client execute {
    create index "posts" mappings (
      "post" as (
        "id" typed IntegerType,
        "description" typed StringType analyzer "name",
        "storageSize" typed IntegerType)
    ) analysis (
      CustomAnalyzerDefinition(
        "nameAnalyzer",
        EdgeNGramTokenizer("myedge", minGram=3, maxGram=8),
        LowercaseTokenFilter,
        StandardTokenFilter)
    )
  }
  def deleteIndices = client execute {
    delete index "posts"
  }
}
