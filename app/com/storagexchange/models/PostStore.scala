package com.storagexchange.models

import java.sql.Timestamp
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

case class Post(
	description : String
)


/**
 * Methods that we will be using from
 * the interface to the database
 */
trait PostStore {
  
  def insert(post: Post): Long
  
  def getById(id: Long): Option[Post]
}


// Actual implementation of User Store method
object PostDAL extends PostStore {
  
  private[this] val createPostSql = {
    SQL("""
      INSERT INTO Post
        (description)
      VALUES
        ({description})
    """.stripMargin)
  }

  def insert(post: Post): Long = DB.withConnection { implicit conn =>
  	createPostSql.on(
	    'description -> post.description
		).executeInsert(scalar[Long].single)
  }

  def getById(id: Long): Option[Post] = None
  def getByEmail(email: String): Option[Post] = None

  def authenticate(email: String, password: String): Boolean = false
}