package com.storagexchange.search

import com.sksamuel.elastic4s.ElasticDsl._
import scala.concurrent.duration._
import org.elasticsearch.indices.IndexMissingException
import org.scalatest.{ Suite, BeforeAndAfterAll }
import com.typesafe.scalalogging.slf4j.Logging
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.Future
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.facet.range.RangeFacet
import scala.collection.JavaConversions._
import com.storagexchange.models._

/** @author Stephen Samuel, @modified Michele Esposito*/
trait ElasticSugar extends BeforeAndAfterAll with Logging {

  this: Suite =>

  val clientInjector = new EmbeddedElasticClient
  val postStore = new PostDAL
  val dataSearch = new ElasticSearch(clientInjector, postStore)
  implicit val client = clientInjector.client

  val atMost: Duration = Duration(10, "seconds")
  
  implicit def unrollFuture[A](f: Future[A]):A = Await.result(f, atMost)

  def facetToSum(resp: SearchResponse): Long = {
    val fct: RangeFacet = resp.getFacets().
	    facetsAsMap().get("size").asInstanceOf[RangeFacet]
    fct.iterator().toList.map(_.getCount()).sum
  }
  
  def countFacets(resp: SearchResponse): Int = {
    val fct: RangeFacet = resp.getFacets().
	    facetsAsMap().get("size").asInstanceOf[RangeFacet]
    fct.iterator().toList.length
  }
  
  def refresh(indexes: String*) {
    val i = indexes.size match {
      case 0 => Seq("_all")
      case _ => indexes
    }
    val listener = client.client.admin().indices().prepareRefresh(i: _*).execute()
    listener.actionGet()
  }

  def blockUntilCount(expected: Long,
                      index: String,
                      types: String*) {

    var backoff = 0
    var actual = 0l

    while (backoff <= 64 && actual != expected) {
      if (backoff > 0)
        Thread.sleep(backoff * 100)
      backoff = if (backoff == 0) 1 else backoff * 2
      try {
        actual = Await.result(client execute {
          count from index types types
        }, 5 seconds).getCount
      } catch {
        case e: IndexMissingException => 0
      }
    }

    require(expected == actual, s"Block failed waiting on count: Expected was $expected but actual was $actual")
  }
}