package com.storagexchange.controllers

import com.storagexchange.search.SearchBuilder
import com.storagexchange.search.SearchFilter
import com.storagexchange.search.Query
import com.storagexchange.search.DataSearch
import com.storagexchange.search.Offset
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
import com.storagexchange.models.University
import com.storagexchange.models.UniversityStore

case class SearchQuery(query: Option[Query], filters: Option[List[SearchFilter]],
  offset: Option[Offset]) {
  lazy val all = List(query, offset).filter(_.isDefined).map(_.get)
  lazy val unfilters = filters.getOrElse(List())
  def allQueries: List[SearchBuilder] = all ++ unfilters
}

object JsonSearchFormatters {
  implicit val place = Json.format[SearchFilter]
  implicit val query = Json.format[Query]
  implicit val offsetQuery = Json.format[Offset]
  implicit val searchQuery = Json.format[SearchQuery]
  implicit val uniJson = Json.format[University]
}

@Singleton
class SearchAPI @Inject()(dataSearch: DataSearch,
  universityStore: UniversityStore) extends Controller with Secured {
  
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
  
  def getUniversities = IsAuthenticated { _ => _ => 
    val uniList = universityStore.getAll
    Ok(Json.toJson(uniList))
  }
}
