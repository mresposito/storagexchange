package com.storagexchange.search

import com.sksamuel.elastic4s.ElasticDsl._
import scala.concurrent.Await
import scala.concurrent.duration._
import org.elasticsearch.indices.IndexMissingException
import org.scalatest.{ Suite, BeforeAndAfterAll }
import com.typesafe.scalalogging.slf4j.Logging

/** @author Stephen Samuel, @modified Michele Esposito*/
trait ElasticSugar extends BeforeAndAfterAll with Logging {

  this: Suite =>

  val clientInjector = new EmbeddedElasticClient
  val dataSearch = new ElasticSearch(clientInjector)
  implicit val client = clientInjector.client

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