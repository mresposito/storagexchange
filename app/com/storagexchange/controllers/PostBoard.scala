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
import com.storagexchange.search.DataSearch

case class PostRequest(
  description: String,
  storageSize: Int)

@Singleton
class PostBoard @Inject()(postStore: PostStore, dataSearch: DataSearch) 
  extends Controller with Secured {

  val newPostForm = Form(
    mapping(
      "description" -> nonEmptyText(minLength = 4),
      "storageSize" -> number(min=0)
      )(PostRequest.apply)(PostRequest.unapply)
    )

  def newPost = IsAuthenticated { username => _ =>
    Ok(views.html.post.newpost(newPostForm))
  }

  def recieveNewPost = IsAuthenticated { username => implicit request =>
    newPostForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.error404()),
      postData => { 
        val post = Post(username, postData.description, postData.storageSize)
//FIXME:        dataSearch.insertPost(post)
        postStore.insert(post)
        Redirect(routes.PostBoard.myPosts)
      }
    )
  }

  def myPosts = IsAuthenticated { username => _ => 
      val postList = postStore.getByEmail(username)
      Ok(views.html.post.myposts(postList))
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
        val rows = postStore.updateById(id, username,
            updatedPost.description, updatedPost.storageSize)
        if(rows > 0) {
	        Redirect(routes.PostBoard.myPosts)
        } else {
          BadRequest(views.html.error404())
        }
      }
    )
  }
}
