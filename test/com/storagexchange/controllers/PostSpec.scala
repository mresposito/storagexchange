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
    
  val postStore: PostStore = PostDAL

  val post1 = Post("m@e.com", "My post", 95, Some(1))
  val post2 = Post("hsimpson@uis.edu", "Homer no function beer well without", 45, Some(2))

  def createPosts = {
    val Some(create1) = route(createRequest(post1))
    status(create1) must equalTo(OK)
    val Some(create2) = route(createRequest(post2))
    status(create2) must equalTo(OK)
  }
  val CreatePosts = BeforeHook {
    createPosts
  }

  def createRequest(post: Post) = FakeRequest(POST, "/post").
    withSession(("email", post.email)).withFormUrlEncodedBody(
      "description" -> post.description,
      "storageSize" -> post.storageSize.toString)

  def modifyRequest(post: Post) = FakeRequest(POST, "/modifypost").
    withSession(("email", post.email)).withFormUrlEncodedBody(
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
      val Some(myPosts) = route(requestWithSession("/myposts"))
      contentAsString(myPosts) must contain("Description: " + post1.description)
      contentAsString(myPosts) must not contain("Description: " + post2.description)
    }

    "not view my posts if not logged in" in CreatePosts {
      // Redirect to login page
      val Some(myPosts) = route(FakeRequest(GET, "/myposts"))
      status(myPosts) must beEqualTo(SEE_OTHER)
    }

    "display all posts on post board" in CreatePosts {
      val Some(allPosts) = route(requestWithSession("/postboard"))
      contentAsString(allPosts) must contain("User: " + post1.email)
      contentAsString(allPosts) must contain("User: " + post2.email)
    }

  }
}