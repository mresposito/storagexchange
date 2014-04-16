package com.storagexchange.search

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import com.sksamuel.elastic4s.ElasticDsl._
import org.elasticsearch.common.Priority
import com.storagexchange.controllers.PostTest

class ElasticClientSpec extends FlatSpec with Matchers
	with ElasticSugar with PostTest {
  
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

  "facets" should "not modify the number of hits" in {
    val resp = client.sync.execute {
      search in "posts" types "post" facets {
        facet range "size" field "storageSize" range(0 -> 1200)
      }
    }
    resp.getHits.totalHits() should equal(2)
  }
  it should "have 1 facet" in {
    val resp = client.sync.execute {
      search in "posts" types "post" facets {
        facet range "size" field "storageSize" range(0 -> 1200)
      }
    }
    resp.getFacets().facets().size() should be(1)
  }
  it should "count 2 in the facet 0 to 1200" in {
    val resp = client.sync.execute {
      search in "posts" types "post" facets {
        facet range "size" field "storageSize" range(0 -> 1200)
      }
    }
    facetToSum(resp) should be(2)
  }
  it should "count 1 in the facet 0 to 50" in {
    val resp = client.sync.execute {
      search in "posts" types "post" facets {
        facet range "size" field "storageSize" range(0 -> 50)
      }
    }
    facetToSum(resp) should be(1)
  }
  it should "contain 1 facet if 0 to 50" in {
    val resp = client.sync.execute {
      search in "posts" types "post" facets {
        facet range "size" field "storageSize" range(0 -> 50)
      }
    }
    countFacets(resp) should be(1)
  }
  it should "contain 2 facets if 0 to 50 and 50 to 90" in {
    val resp = client.sync.execute {
      search in "posts" types "post" facets {
        facet range "size" field "storageSize" range(0 -> 50) range(50 -> 90)
      }
    }
    countFacets(resp) should be(2)
  }
  it should "count 0 in the facet 0 to 1" in {
    val resp = client.sync.execute {
      search in "posts" types "post" facets {
        facet range "size" field "storageSize" range(0 -> 1)
      }
    }
    facetToSum(resp) should be(0)
  }
  it should "count 2 in the facet 0 to 60 and 60 to 100" in {
    val resp = client.sync.execute {
      search in "posts" types "post" facets {
        facet range "size" field "storageSize" range(0 -> 100) range(
          101 -> 300) range(301 ->1000) range(1001->99999)
      }
    }
    facetToSum(resp) should be(2)
  }
}