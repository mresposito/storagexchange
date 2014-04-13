package com.storagexchange.controllers

import com.storagexchange.search.SearchBuilder
import com.storagexchange.search.SearchFilter
import com.storagexchange.search.Query
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
import scala.concurrent.Future

case class SearchQuery(query: Option[Query], filters: Option[List[SearchFilter]]) {
  def unfilters = filters.getOrElse(List())
  def allQueries: List[SearchBuilder] = query.map { q => 
    q::unfilters
  }.getOrElse(unfilters)
}

object JsonSearchFormatters {
  implicit val place = Json.format[SearchFilter]
  implicit val query = Json.format[Query]
  implicit val searchQuery = Json.format[SearchQuery]
}

@Singleton
class SearchAPI @Inject()(dataSearch: DataSearch) 
  extends Controller with Secured {
  
  import JsonSearchFormatters._
  
  def getPosts = Action.async(parse.json) { request =>
   request.body.asOpt[SearchQuery].map { search: SearchQuery =>
	    dataSearch.getPosts(search.allQueries:_*).map { posts => 
		    Ok(posts.toString())
	    }
    }.getOrElse {
      Future(BadRequest("""{"error": "Could not parse Json"}"""))
    }
  }
}