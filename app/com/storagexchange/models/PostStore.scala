package com.storagexchange.models

import java.sql.Timestamp
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

case class Post(email: String,
	description: String
)


/**
 * Methods that we will be using from
 * the interface to the database
 */
trait PostStore {
  def insert(post: Post): Long
  def getById(id: Long): Option[Post]
  def getByEmail(email: String): Option[List[Post]]
}


// Actual implementation of User Store method
object PostDAL extends PostStore {
  
  private[this] val createPostSql = {
    SQL("""
      INSERT INTO Post
        (email, description)
      VALUES
        ({email}, {description})
    """.stripMargin)
  }

  def insert(post: Post): Long = DB.withConnection { implicit conn =>
  	createPostSql.on(
      'email -> post.email,
	    'description -> post.description
		).executeInsert(scalar[Long].single)
  }

  def getById(id: Long): Option[Post] = DB.withConnection { implicit conn =>
    var selectPosts = SQL("SELECT * FROM Post WHERE postID = " + id).apply()
    if (selectPosts.isEmpty) None
    else {
      val row = selectPosts.head
      Option(new Post(row[String]("email"), row[String]("description")))
    }
  }

  def getByEmail(email: String): Option[List[Post]] = DB.withConnection { implicit conn =>
    var selectPosts = SQL("SELECT * FROM Post WHERE email = " + email)
    val posts : Option[List[Post]] = Option(selectPosts().map(row =>
      new Post(row[String]("email"), row[String]("description"))).toList)
    posts
  }

  def authenticate(email: String, password: String): Boolean = false
}