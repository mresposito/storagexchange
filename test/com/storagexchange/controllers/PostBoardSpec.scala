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

trait PostTest extends Specification with LocationTest {
    
  val post1 = Post("m@e.com", "This is the first post", 95, 1, Some(1))
  val post1Modified = post1.copy(description = "This is the second post")
  val post2 = Post("hsimpson@uis.edu", "Homer no function beer well without", 45, 1, Some(2))
  val testLocId: Long = 2

  val CreatePosts = BeforeHook {
    val Some(create1) = route(createRequest(post1))
    val Some(create2) = route(createRequest(post2))
  }

  def createRequest(post: Post) = FakeRequest(POST, "/post").
    withSession(("email", post.email)).
    withFormUrlEncodedBody(
      "description" -> post.description,
      "storageSize" -> post.storageSize.toString,
      "streetNum" -> "450",
      "street" -> "Serra Mall",
      "city" -> testLoc.city,
      "state" -> testLoc.state,
      "zip" -> testLoc.zip,
      "lat" -> testLoc.lat.toString,
      "lng" -> testLoc.lng.toString
    )
  
  def createInvalidLocationRequest(post: Post) = FakeRequest(POST, "/post").
    withSession(("email", post.email)).
    withFormUrlEncodedBody(
      "description" -> post.description,
      "storageSize" -> post.storageSize.toString,
      "streetNum" -> "450",
      "street" -> "",
      "city" -> testLoc.city,
      "state" -> testLoc.state,
      "zip" -> testLoc.zip,
      "lat" -> testLoc.lat.toString,
      "lng" -> testLoc.lng.toString
    )

  def deletePost(id: Long) = withSession(FakeRequest(DELETE, routes.PostBoard.delete(id).url))
  def modifyPost(post: Post) = withSession(
    FakeRequest(POST, routes.PostBoard.modify(post.postID.get, testLocId).url).
    withFormUrlEncodedBody(
      "description" -> post.description,
      "storageSize" -> post.storageSize.toString,
      "streetNum" -> "450",
      "street" -> "Serra Mall",
      "city" -> testLoc.city,
      "state" -> testLoc.state,
      "zip" -> testLoc.zip,
      "lat" -> testLoc.lat.toString,
      "lng" -> testLoc.lng.toString
    ))

  def requestWithSession(route: String) = withSession(FakeRequest(GET, route))
  def withSession[T](request: FakeRequest[T]) = request.withSession(("email", post1.email))
}

class PostBoardSpec extends Specification with PostTest {

	"Post Board" should {

		"View posts" in {
			"accept valid post" in RunningApp {
				val Some(create) = route(createRequest(post1))
						status(create) must beEqualTo(SEE_OTHER)
			}
      "reject post with invalid location format" in RunningApp {
        val Some(create) = route(createInvalidLocationRequest(post1))
          status(create) must beEqualTo(BAD_REQUEST)
      }
			"view my posts" in CreatePosts {
				val Some(myPosts) = route(requestWithSession(routes.PostBoard.myPosts.url))
						contentAsString(myPosts) must contain(post1.description)
			}
			"view my posts should not display posts that are not my" in CreatePosts {
				val Some(myPosts) = route(requestWithSession(routes.PostBoard.myPosts.url))
						contentAsString(myPosts) must not contain(post2.description)
			}
			"cannot view my posts if not logged in" in CreatePosts {
				val Some(myPosts) = route(FakeRequest(GET, routes.PostBoard.myPosts.url))
						status(myPosts) must beEqualTo(SEE_OTHER)
			}
		}

		"Delete Posts" in {
			"deleting a non existing post gives you bad request" in RunningApp {
				val Some(delete) = route(deletePost(490))
						status(delete) must beEqualTo(BAD_REQUEST)
			}
			"deleting a post you dont own gives you bad requests" in CreatePosts {
				val Some(delete) = route(deletePost(2))
						status(delete) must beEqualTo(BAD_REQUEST)
			}
			"delete my post should be successful" in CreatePosts {
				val Some(delete) = route(deletePost(1))
						status(delete) must beEqualTo(OK)
			}
			"can't see my post after deleting" in CreatePosts {
				val Some(delete) = route(deletePost(1))
						val Some(myPosts) = route(requestWithSession(routes.PostBoard.myPosts.url))
						contentAsString(myPosts) must not contain(post1.description)
			}
		}

		"Modify Posts" in {
			"can't modify a post that does not exist" in RunningApp {
				val Some(modify) = route(modifyPost(post1Modified)) 
						status(modify) must beEqualTo(BAD_REQUEST)
			}
			"Can't modify a post I don't own" in CreatePosts {
				val Some(modify) = route(modifyPost(post2)) 
						status(modify) must beEqualTo(BAD_REQUEST)
			}
			"modify my post should be successful" in CreatePosts {
				val Some(modify) = route(modifyPost(post1Modified)) 
						status(modify) must beEqualTo(SEE_OTHER)
			}
			"change description in modified post" in CreatePosts {
				val Some(modify) = route(modifyPost(post1Modified)) 
						val Some(myPosts) = route(requestWithSession(routes.PostBoard.myPosts.url))
						contentAsString(myPosts) must contain(post1Modified.description)
			}
			"modify post should update the description" in CreatePosts {
				val Some(modify) = route(modifyPost(post1Modified)) 
						val Some(myPosts) = route(requestWithSession(routes.PostBoard.myPosts.url))
						contentAsString(myPosts) must not contain(post1.description)
			}
		}
	}
}
