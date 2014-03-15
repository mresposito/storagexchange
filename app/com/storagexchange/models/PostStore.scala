package com.storagexchange.models

import java.sql.Timestamp
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import javax.inject.Singleton
import javax.inject.Inject

case class Post(email: String,
	description: String,
  postID: Option[Long] = None)

/**
 * Methods that we will be using from
 * the interface to the database
 */
trait PostStore {
  def insert(post: Post): Long
  def getById(id: Long): Option[Post]
  def getByEmail(email: String): List[Post]
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

  private[this] val findPostByIdSql = {
    SQL("""
       SELECT *
       FROM Post
       WHERE postID = {postID}
    """.stripMargin)
  }

  private[this] val findPostByEmailSql = {
    SQL("""
       SELECT *
       FROM Post
       WHERE email = {email}
    """.stripMargin)
  }

  implicit val postParser = 
    str("email") ~
    str("description") ~
    long("postID").? map {
      case email ~ description ~ postID =>
        Post(email, description, postID)
    }

  def insert(post: Post): Long = DB.withConnection { implicit conn =>
  	createPostSql.on(
      'email -> post.email,
	    'description -> post.description
		).executeInsert(scalar[Long].single)
  }

  def getById(id: Long): Option[Post] = DB.withConnection { implicit conn =>
    findPostByIdSql.on(
      'postID -> id
    ).as(postParser.singleOpt)
  }

  def getByEmail(email: String): List[Post] = DB.withConnection { implicit conn =>
    findPostByEmailSql.on(
      'email -> email
    ).as(postParser *)
  }

  def authenticate(email: String, password: String): Boolean = false
}
