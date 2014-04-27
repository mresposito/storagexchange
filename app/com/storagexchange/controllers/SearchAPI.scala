package com.storagexchange.controllers

import com.storagexchange.search.SearchBuilder
import com.storagexchange.search.SearchFilter
import com.storagexchange.search.LocationQuery
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
import com.storagexchange.models.Location
import com.storagexchange.models.LocationConversions

case class SearchQuery(query: Option[Query], addressQuery: Option[LocationQuery],
  filters: Option[List[SearchFilter]], offset: Option[Offset], university: Option[String]) {

  lazy val all = List(query, addressQuery, offset).filter(_.isDefined).map(_.get)
  lazy val unfilters = filters.getOrElse(List())
  def allQueries: List[SearchBuilder] = all ++ unfilters
}

object JsonSearchFormatters {
  implicit val place = Json.format[SearchFilter]
  implicit val query = Json.format[Query]
  implicit val addressQuery = Json.format[LocationQuery]
  implicit val offsetQuery = Json.format[Offset]
  implicit val searchQuery = Json.format[SearchQuery]
  implicit val uniJson = Json.format[University]
}

@Singleton
class SearchAPI @Inject()(dataSearch: DataSearch,
  universityStore: UniversityStore) extends Controller with Secured with LocationConversions {
  
  import JsonSearchFormatters._

  def getPosts = Action.async(parse.json) { request =>
   request.body.asOpt[SearchQuery].map { search: SearchQuery =>
     val queries = makeQueries(search)
	    dataSearch.getPosts(queries:_*).map { posts =>
		    Ok(posts.toString())
	    }
    }.getOrElse {
      Future(BadRequest("""{"error": "Could not parse Json"}"""))
    }
  }
  
  private def makeQueries(search: SearchQuery): List[SearchBuilder] = {
    val loc: Option[Location] = search.university.map { 
      universityStore.getUniversityLocation
    }.getOrElse(None)
    loc.map { l =>
      (LocationQuery(l.lat, l.lng, 100)) :: search.allQueries
    }.getOrElse(search.allQueries)
  }
  
  def getUniversities = IsAuthenticated { _ => _ => 
    val uniList = universityStore.getAll
    Ok(Json.toJson(uniList))
  }
}