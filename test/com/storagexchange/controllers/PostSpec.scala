package com.storagexchange.controllers

import com.storagexchange.models._
import com.storagexchange.utils._
import play.api.test._
import play.api.test.Helpers._
import play.api.Play.current
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import com.storagexchange.models.PostStore

trait PostTest extends Specification {
    
  val postStore: PostStore = new PostDAL

  val post1 = Post("m@e.com", "My post", 95, Some(1))
  val post2 = Post("hsimpson@uis.edu", "Homer no function beer well without", 45, Some(2))

  val CreatePosts = BeforeHook {
    val Some(create1) = route(createRequest(post1))
    status(create1) must equalTo(OK)
    val Some(create2) = route(createRequest(post2))
    status(create2) must equalTo(OK)
  }

  def createRequest(post: Post) = FakeRequest(POST, "/post").
    withSession(("email", post.email)).
    withFormUrlEncodedBody(
      "description" -> post.description,
      "storageSize" -> post.storageSize.toString)

  def modifyRequest(post: Post) = FakeRequest(POST, "/modifypost").
    withSession(("email", post.email)).
    withFormUrlEncodedBody(
      "description" -> post.description,
      "storageSize" -> post.storageSize.toString,
      "postID" -> post.postID.toString)

  def requestWithSession(route: String) = FakeRequest(GET, route).withSession(("email", "m@e.com"))
}

class PostSpec extends Specification with PostTest {

  "Post" should {

    "accept valid post" in RunningApp {
      val Some(create) = route(createRequest(post1))
      status(create) must beEqualTo(OK)
    }

    "view my posts" in CreatePosts {
      val Some(myPosts) = route(requestWithSession(routes.PostBoard.myPosts.url))
      contentAsString(myPosts) must contain("Description: " + post1.description)
      contentAsString(myPosts) must not contain("Description: " + post2.description)
    }

    "not view my posts if not logged in" in CreatePosts {
      // Redirect to login page
      val Some(myPosts) = route(FakeRequest(GET, routes.PostBoard.myPosts.url))
      status(myPosts) must beEqualTo(SEE_OTHER)
    }
  }
}