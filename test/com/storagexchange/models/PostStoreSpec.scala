package com.storagexchange.models

import com.storagexchange.utils._
import org.specs2.mutable._
import org.specs2.execute.AsResult
import play.api.test._
import play.api.test.Helpers._
import play.api.db._
import play.api.Play.{current => curr}
import java.sql.Timestamp
import org.h2.jdbc.JdbcSQLException
import com.storagexchange.controllers.UserTest

class PostStoreSpec extends Specification with UserTest {
  val postStore: PostStore = new PostDAL
  val post1 = Post(user.email, "My post", 95, 1,Some(1))
  val post2 = Post("other@me.com", "Some other post", 42, 2,Some(2))
  val post1Copy = post1.copy()
  val post2Copy = post2.copy()

  val InsertLocation = BeforeHook {
    DB.withConnection { implicit conn =>
      locationStore.insert(testLoc)
    }
  }

  val InsertPost = BeforeHook {
    DB.withConnection { implicit conn =>
      //need to insert location first 
      userStore.insert(user)
      locationStore.insert(testLoc)
      locationStore.insert(testLoc2)
      postStore.insert(post1).toInt must beEqualTo(1)
      postStore.insert(post2).toInt must beEqualTo(2)
    }
  }
  
  "Post Store" should {
    "insert a post" in InsertLocation {
        postStore.insert(post1).toInt must beEqualTo(1)
    }
    "find post by email" in InsertPost {
      postStore.getByEmail(post1.email) must beEqualTo(List(post1Copy))
    }
    "not find a non-existent post by email" in InsertPost {
      postStore.getByEmail("hello@me") must beEmpty
    }
    "find post by id" in InsertPost {
      postStore.getById(1) must beSome(post1Copy)
    }
    "not find a non-existent post by id" in InsertPost {
      postStore.getById(3049) must beNone
    }
    "Remove post" in {
	    "remove extisting post by id should be true" in InsertPost {
	      postStore.removeById(1, post1.email) must beTrue
	    }
	    "Can't get the post by id after deleting it" in InsertPost {
	      postStore.removeById(1, post1.email)
	      postStore.getById(1) must beNone
	    }
	    "Deleting a not existing post must be false" in InsertPost {
	      postStore.removeById(4902, post1.email) must beFalse
	    }
	    "Deleting a post that is not mine should be false" in InsertPost {
	      postStore.removeById(1, "myfakeemal") must beFalse
	    }
    }
    "get posts by location id" in InsertPost {
      postStore.getPostsByLocationID(1) must beEqualTo(List(post1Copy))
    }
    "get posts by city" in InsertPost {
      postStore.getPostsByCity("Champaign") must beEqualTo(List(post2Copy))
    }
    "update post by id" in InsertPost {
      val updatedPost = post1.copy(description = "My updated post", storageSize = 101)
      postStore.updateById(1, post1.email, "My updated post", 101)
      postStore.getById(1) must beSome(updatedPost)
    }
  }
  "Get Informations" in {
    "return none for non existing post" in RunningApp {
      postStore.getPostInfo(post1.postID.get) must beNone
    }
    "return none if info is not complete" in InsertPost {
      postStore.getPostInfo(post2.postID.get) must beNone
    }
    "get the right info" in InsertPost {
      postStore.getPostInfo(1) must beSome(
        PostInfo(post1Copy, testLoc.copy(id = Some(1)), userId))
    }
  }
}