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

  val user = User("michele", "esposito", "m@e.com", "test123", 0)
  val post = Post("user@test.com", "My post", 95)
  val postCopy = post.copy(postID = Some(1))

  def createPost = {
    val Some(create) = route(genericCreateRequest(post))
    status(create) must equalTo(OK)
  }
  val CreatePost = BeforeHook {
    createPost
  }

  def genericCreateRequest(post: Post) = FakeRequest(POST,"/post").
    withSession(("email", user.email)).withFormUrlEncodedBody(
      "description" -> post.description,
      "storageSize" -> post.storageSize.toString)

  def requestWithSession(route: String) = FakeRequest(GET, route).withSession(("email", user.email))
}

class PostSpec extends Specification with PostTest {

  "Post" should {

    "Create my post correctly" in {

      "accept valid post" in RunningApp {
        val Some(create) = route(genericCreateRequest(post))
        status(create) must beEqualTo(OK)
      }

      "view my posts" in CreatePost {
        val Some(myPosts) = route(requestWithSession("/myposts"))
        status(myPosts) must beEqualTo(OK)
        contentAsString(myPosts) must contain("Description: " + post.description)
        contentAsString(myPosts) must contain("Size: " + post.storageSize.toString)
      }

      "not view my posts if not logged in" in CreatePost {
        val Some(myPosts) = route(FakeRequest(GET, "/myposts"))
        status(myPosts) must beEqualTo(SEE_OTHER)
      }
    }

  }
}