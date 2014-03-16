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
  storageSize: Int,
  postID: Option[Long] = None)

/**
 * Methods that we will be using from
 * the interface to the database
 */
trait PostStore {
  def insert(post: Post): Long
  def getById(id: Long): Option[Post]
  def getByEmail(email: String): List[Post]
  def removeById(id: Long): Boolean
  def updateById(id: Long, description: String, storageSize: Int): Int
  def getAll(): List[Post]
  def getAllWithCondition(condition: String): List[Post]
}

// Actual implementation of Post Store method
object PostDAL extends PostStore {
  
  private[this] val createPostSql = {
    SQL("""
      INSERT INTO Post
        (email, description, storageSize)
      VALUES
        ({email}, {description},{storageSize})
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

  private[this] val selectPost = {
    SQL("""
       SELECT *
       FROM Post
    """.stripMargin)
  }

  private[this] val updatePostById = {
    SQL("""
       Update Post
       SET description={description}, storageSize={storageSize}
       WHERE postID = {postID}
    """.stripMargin)
  }

  private[this] val selectPostWithSuffix = {
    SQL("""
       SELECT *
       FROM Post
       {suffix}
    """.stripMargin)
  }

  private[this] val removePostByIdSql = {
    SQL("""
       DELETE FROM Post
       WHERE postID = {postID}
    """.stripMargin)
  }

  implicit val postParser = 
    str("email") ~
    str("description") ~
    int("storageSize") ~
    long("postID").? map {
      case email ~ description ~ storageSize ~ postID =>
        Post(email, description, storageSize,postID)
    }

  def insert(post: Post): Long = DB.withConnection { implicit conn =>
  	createPostSql.on(
      'email -> post.email,
	    'description -> post.description,
      'storageSize -> post.storageSize
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

  def removeById(id: Long): Boolean = DB.withConnection { implicit conn =>
    removePostByIdSql.on(
      'postID -> id
    ).execute()
  }

  def updateById(id: Long, description: String, storageSize: Int): Int = DB.withConnection{ implicit conn =>
    updatePostById.on(
        'postID->id,
        'description->description,
        'storageSize->storageSize
      ).executeUpdate()
  }

  def getAll(): List[Post] = DB.withConnection { implicit conn =>
    selectPost.as(postParser *)
  }
  
  def getAllWithCondition(condtion: String): List[Post] = DB.withConnection { implicit conn =>
    selectPostWithSuffix.on(
      'condition -> condtion
    ).as(postParser *)
  }

}
