package com.storagexchange.search

import com.storagexchange.models.Post
import com.storagexchange.utils._
import org.specs2.mutable._
import org.specs2.execute.AsResult
import play.api.test._
import play.api.test.Helpers._
import play.api.Play.current
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global


class DataSearchSpec extends Specification {
  
  val client: ElasticClientInjector = new EmbeddedElasticClient
  val search: DataSearch = new ElasticSearch(client)
  val post = Post("m@e.com", "first post", 80, Some(1))
  val atMost = Duration.fromNanos(1000)
  
//  "Data Search" should {
//    "insert a post successfully" in RunningApp { 
//       search.insertPost(post) must beEqualTo("hello").await
//    }
//  }
}