package com.storagexchange.controllers

import com.storagexchange.models._
import com.storagexchange.views
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import com.typesafe.scalalogging.slf4j.Logging
import javax.inject.Singleton
import javax.inject.Inject
import java.math.BigDecimal
import com.storagexchange.search.DataSearch

case class PostRequest(
  description: String,
  storageSize: Int,
  streetNum: Int,
  street: String,
  city: String,
  state: String,
  zip: String,
  lat: String,
  lng: String)

@Singleton
class PostBoard @Inject()(postStore: PostStore, locationStore: LocationStore, ratingStore: RatingStore,
  dataSearch: DataSearch) extends Controller with Secured with LocationConversions {

  val newPostForm = Form(
    mapping(
      "description" -> nonEmptyText(minLength=4),
      "storageSize" -> number(min=0),
      "streetNum" -> number(min=0),
      "street" -> nonEmptyText(minLength=5),
      "city" -> nonEmptyText(minLength=1),
      "state" -> nonEmptyText(minLength=2), 
      "zip" -> nonEmptyText(minLength=5),
      "lat" -> nonEmptyText(minLength=4),
      "lng" -> nonEmptyText(minLength=4)
      )(PostRequest.apply)(PostRequest.unapply)
    )

  def newPost = IsAuthenticated { username => _ =>
    Ok(views.html.post.newpost(newPostForm))
  }

  def recieveNewPost = IsAuthenticated { username => implicit request =>
    newPostForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.post.newpost(formWithErrors)),
      postData => {
        //When inserting an address into Location, we concatenate the street number with the street
        val fullStreet = postData.streetNum.toString + " " + postData.street
        val location = Location(postData.description, 
            new BigDecimal(postData.lat), 
            new BigDecimal(postData.lng), 
            postData.city, postData.state, 
            fullStreet, postData.zip)
        val locID: Long = locationStore.insert(location)
        val post = Post(username, postData.description, postData.storageSize, locID)
        val id = postStore.insert(post)
        dataSearch.insertPost(post.copy(postID = Some(id)),
          location.lat, location.lng)
        Redirect(routes.PostBoard.myPosts)
      }
    )
  }

  def myPosts = IsAuthenticated { username => _ => 
    val postList = for {
      post <- postStore.getByEmail(username)
      location <- locationStore.getById(post.locationID)
    } yield (post, location)
    Ok(views.html.post.myposts(postList))
  }
  
  def viewPost(id: Long) = IsAuthenticated { _ => _ => 
    postStore.getPostInfo(id).map { info =>
      Ok(views.html.post.info(info, ratingStore.getAvgByRatee(info.user.email)))
    }.getOrElse {
      BadRequest(views.html.error404())  
    }
  }

  def delete(id: Long) = IsAuthenticated { username => _ => 
    if(postStore.removeById(id, username)) {
      dataSearch.deletePost(id)
      Ok
    } else {
      BadRequest(views.html.error404())  
    }
  }
 
  def modify(id: Long, locId: Long) = IsAuthenticated { username => implicit request =>
    newPostForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.error404()),
      updatedPost => {
        val rows = postStore.updateById(id, username,
          updatedPost.description, updatedPost.storageSize)
        if(rows > 0) {
          dataSearch.updatePost(Post(username,
            updatedPost.description, updatedPost.storageSize, locId, Some(id)))
          Redirect(routes.PostBoard.myPosts)
        } else {
          BadRequest(views.html.error404())
        }
      }
    )
  }
}
