package com.storagexchange.search

import org.elasticsearch.node.NodeBuilder.nodeBuilder
import java.io.File
import java.util.UUID
import org.elasticsearch.common.settings.ImmutableSettings
import com.sksamuel.elastic4s.ElasticClient

trait ElasticClientInjector {
  val client: ElasticClient
  def tearDown: Unit = {}
  def close = client.close
}

class LocalElasticClient extends ElasticClientInjector {33.858878
  val client = ElasticClient.local
}

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

