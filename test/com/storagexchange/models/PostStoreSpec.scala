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
  val post1 = Post("user@test.com", "My post", 95, Some(1))
  val post2 = Post("other@me.com", "Some other post", 42, Some(2))
  val post1Copy = post1.copy()
  val post2Copy = post2.copy()

  val InsertPost = BeforeHook {
    DB.withConnection { implicit conn =>
      postStore.insert(post1).toInt must beEqualTo(1)
      postStore.insert(post2).toInt must beEqualTo(2)
    }
  }
  
  "Post Store" should {
    "insert a post" in RunningApp {
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
    "remove post by id" in InsertPost {
      postStore.removeById(1)
      postStore.getById(1) must beNone
    }
    "update post by id" in InsertPost {
      val updatedPost = Post("user@test.com", "My updated post", 101, Some(1))
      postStore.updateById(1, "My updated post", 101)
      postStore.getById(1) must beSome(updatedPost)
    }
    "get all posts" in InsertPost {
      postStore.getAll() must beEqualTo(List(post1Copy, post2Copy))
    }
  }
}
