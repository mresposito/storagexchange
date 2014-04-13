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
class PostBoard @Inject()(postStore: PostStore, locationStore: LocationStore) 
  extends Controller with Secured {

  val newPostForm = Form(
    mapping(
      "description" -> nonEmptyText(minLength = 4),
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
      formWithErrors => BadRequest(views.html.error404()),
      postData => {
        val fullStreet = postData.streetNum.toString + " " + postData.street
        val locID: Long = locationStore.insert(Location(postData.description, new BigDecimal(postData.lat), new BigDecimal(postData.lng), 
                                                        postData.city, postData.state, 
                                                        fullStreet, postData.zip, None))
        postStore.insert(Post(username, postData.description, postData.storageSize, locID))
        Redirect(routes.PostBoard.myPosts)
      }
    )
  }

  def myPosts = IsAuthenticated { username => _ => 
      val postList = postStore.getByEmail(username)
      Ok(views.html.post.myposts(postList, locationStore))
  }

  def delete(id: Long) = IsAuthenticated { username => _ => 
    if(postStore.removeById(id, username)) {
	    Ok
    } else {
      BadRequest(views.html.error404())  
    }
  }
 
  def modify(id: Long) = IsAuthenticated { username => implicit request =>
    newPostForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.error404()),
      updatedPost => {
        val streetWithoutNumber = updatedPost.street.replace(updatedPost.streetNum+" ", "")
        val rows = postStore.updateById(id, username,
          updatedPost.description, updatedPost.storageSize, updatedPost.streetNum,
          streetWithoutNumber, updatedPost.city, updatedPost.state, updatedPost.zip,
          updatedPost.lat, updatedPost.lng)
        if(rows > 0) {
	        Redirect(routes.PostBoard.myPosts)
        } else {
          BadRequest(views.html.error404())
        }
      }
    )
  }
}
