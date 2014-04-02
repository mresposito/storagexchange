package com.storagexchange.controllers

import com.storagexchange.search.DataSearch
import com.storagexchange.views
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import com.typesafe.scalalogging.slf4j.Logging
import javax.inject.Singleton
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits.defaultContext


@Singleton
class SearchAPI @Inject()(dataSearch: DataSearch) 
  extends Controller with Secured {
  
  def getPosts = Action.async {
    dataSearch.getPosts.map { posts => 
	    Ok(posts.toString())
    }
  }
}