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

class PostStoreSpec extends Specification {
  val postStore: PostStore = PostDAL
  val post = Post("user@test.com", "My post", 95)
  val postID = post.copy(postID = Some(1))
  
  val InsertPost = BeforeHook {
    DB.withConnection { implicit conn =>
        postStore.insert(post).toInt must beEqualTo(1)
    }
  }
  
  "Post Store" should {
    "insert a post" in RunningApp {
        postStore.insert(post).toInt must beEqualTo(1)
    }
    "find post by email" in InsertPost {
      postStore.getByEmail(post.email) must beEqualTo(List(postID))
    }
    "not find a non-existent post by email" in InsertPost {
      postStore.getByEmail("hello@me") must beEqualTo(List())
    }
    "find post by id" in InsertPost {
      postStore.getById(1) must beSome(postID)
    }
    "not find a non-existent post by id" in InsertPost {
      postStore.getById(3049) must beNone
    }
    "remove post by id" in InsertPost {
      postStore.getById(1) must beSome(postID)
      postStore.removeById(1)
      postStore.getById(1) must beNone
    }
  }
}
