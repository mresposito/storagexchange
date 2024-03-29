package com.storagexchange.search

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mapping.FieldType._
import org.elasticsearch.common.Priority
import com.storagexchange.controllers.PostTest
import org.elasticsearch.common.unit.DistanceUnit

class ElasticClientSpec extends FlatSpec with Matchers
	with ElasticSugar with PostTest {
  
  dataSearch.createIndices
  dataSearch.insertPost(post1, stanford.lat, stanford.lng)
  dataSearch.insertPost(post2, berkley.lng, berkley.lng)

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
  
  "offset" should "return 1 in (0, 1)" in {
    val resp = client.sync.execute {
      search in "posts" types "post" start 0 limit 1 
    }
    resp.getHits().hits().length should be(1) 
  }
  it should "contain 'first' in starting 0" in {
    val resp = client.sync.execute {
      search in "posts" types "post" start 0 limit 1 sort {
        by field "id"
      }
    }
    resp.toString should include(post1.description) 
  }
  it should "return 1 in (1, 2)" in {
    val resp = client.sync.execute {
      search in "posts" types "post" start 1 limit 1
    }
    resp.getHits().hits().length should be(1) 
  }
  it should "contain the second post starting at  1" in {
    val resp = client.sync.execute {
      search in "posts" types "post" start 1 limit 1 sort {
        by field "id"
      }
    }
    resp.toString() should include(post2.description)
  }
  "location search" should "find that stanford is close to berkeley" in {
    val resp = client.sync.execute {
      search in "posts" types "post" filter {
        geoDistance("location") lat berkley.lat lon berkley.lng distance (200, DistanceUnit.KILOMETERS)
      }
    }
    resp.getHits.totalHits() should equal(1)
    resp.toString() should include(post1.description)
  }
  it should "not find anything within 1 km of berkeley" in {
    val resp = client.sync.execute {
      search in "posts" types "post" filter {
        geoDistance("location") lat berkley.lat lon berkley.lng distance (1, DistanceUnit.KILOMETERS)
      }
    }
    resp.getHits.totalHits() should equal(0)
  }
}