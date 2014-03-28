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

case class PostRequest(
  description: String,
  storageSize: Int)

case class PostIdRequest(
  postID: Long)

case class PostModifyRequest(
  description: String,
  storageSize: Int,
  postID: Long)

@Singleton
class PostBoard @Inject()(postStore: PostStore) 
  extends Controller with Secured {

  val newPostForm = Form(
    mapping(
      "description" -> nonEmptyText(minLength = 4),
      "storageSize" -> number(min=0)
      )(PostRequest.apply)(PostRequest.unapply)
    )

  val postModifyInitialForm = Form(
    mapping(
      "postID" -> longNumber(min=0)
      )(PostIdRequest.apply)(PostIdRequest.unapply)
    )

  val postModifyForm = Form(
    mapping(
      "description" -> nonEmptyText(minLength = 4),
      "storageSize" -> number(min=0),
      "postID" -> longNumber(min=0)
      )(PostModifyRequest.apply)(PostModifyRequest.unapply)
    )

  val postDeleteForm = Form(
    mapping(
      "postID" -> longNumber(min=0)
      )(PostIdRequest.apply)(PostIdRequest.unapply)
    )

  def newPost = IsAuthenticated { username => _ =>
    Ok(views.html.newpost(newPostForm))
  }

  def postReceive = IsAuthenticated { username => implicit request =>
    newPostForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.error404()),
      postData => { 
        postStore.insert(Post(username, postData.description, postData.storageSize))
        Ok(views.html.newpost(newPostForm))
      }
    )
  }

  def myPosts = IsAuthenticated { username => _ => 
      val postList = postStore.getByEmail(username)
      Ok(views.html.post.myposts(postList))
  }

  def postViewAll = Action { request =>
    val postList = postStore.getAll()
    Ok(views.html.postboard(postList))
  }

  def postModifyInitial = IsAuthenticated { username => implicit request =>
    postModifyInitialForm.bindFromRequest.fold(
      // Fix: should go to proper page with errors.
      // Having issue with that however.
      formWithErrors => BadRequest(views.html.error404()),
      modifyRequest => {
        postStore.getById(modifyRequest.postID).map { oldPost =>
          Ok(views.html.modifypost(newPostForm, oldPost))  
        }.getOrElse(Ok(views.html.index()))
      }
    )
  }

  def postModify = IsAuthenticated { username => implicit request =>
    postModifyForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.error404()),
      updatedPost => {
        postStore.updateById(updatedPost.postID, updatedPost.description, updatedPost.storageSize)
        Redirect("myposts")
      }
    )
  }

  def postDelete = IsAuthenticated{ username => implicit request =>
    postDeleteForm.bindFromRequest.fold(
      formWithErrors => Redirect("/myposts"),
      deletedPost => {
        postStore.removeById(deletedPost.postID)
        Redirect("myposts")
      }
    )
  }
}