package com.storagexchange.models

import java.sql.Timestamp
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import javax.inject.Singleton
import javax.inject.Inject
import java.math.BigDecimal

case class Post (
  email: String,
  description: String,
  storageSize: Int,
  locationID: Long,
  postID: Option[Long] = None)
  
case class PostInfo (
  post: Post,
  location: Location,
  user: User)

/**
 * Methods that we will be using from
 * the interface to the database
 */
trait PostStore {
  def insert(post: Post): Long

  def getById(id: Long): Option[Post]
  def getByEmail(email: String): List[Post]
  def getPostInfo(id: Long): Option[PostInfo]
  def getPostsByLocationID(locationID: Long): List[Post]
  def getPostsByCity(city: String): List[Post]

  def removeById(id: Long, email: String): Boolean
  def updateById(id: Long, email: String, description: String, storageSize: Int): Int
}

// Actual implementation of Post Store method
@Singleton
class PostDAL extends PostStore {
  
  private[this] val createPostSql = {
    SQL("""
      INSERT INTO Post
        (email, description, locationID, storageSize)
      VALUES
        ({email}, {description}, {locationID}, {storageSize})
    """.stripMargin)
  }

  private[this] val findPostByIdSql = {
    SQL("""
       SELECT *
       FROM Post
       WHERE postID = {postID}
    """.stripMargin)
  }
  private[this] val findPostsByLocationID = {
    SQL("""
       SELECT * 
       FROM Post
       WHERE locationID = {locationID}
    """.stripMargin)
  }
  private[this] val findPostByEmailSql = {
    SQL("""
       SELECT *
       FROM Post
       WHERE email = {email}
    """.stripMargin)
  }

  private[this] val updatePostById = {
    SQL("""
       UPDATE Post
       SET description={description}, storageSize={storageSize}
       WHERE postID = {postID} AND
        email = {email}
    """.stripMargin)
  }
  private[this] val getPostInfoSql = {
    SQL("""
       SELECT *
       FROM Post, User u, Location
       WHERE postID = {postID} 
         AND u.email = Post.email
         AND Location.id = Post.locationID
    """.stripMargin)
  }
  
  private[this] val findPostsByCity = {
    SQL("""
       SELECT * 
       FROM Post
       WHERE locationID IN 
       (SELECT id FROM Location WHERE city = {city})
    """.stripMargin)
  }

  private[this] val removePostByIdSql = {
    SQL("""
       DELETE FROM Post
       WHERE postID = {postID} AND
        email = {email}
    """.stripMargin)
  }

  private[this] val joinPostLocation = {
    SQL(
      """
        SELECT *
        FROM Post, Location
        WHERE locationID = id AND postID = {postID}
      """.stripMargin)
  }

  implicit val postParser = 
    str("email") ~
    str("description") ~
    int("storageSize") ~
    long("locationID") ~
    long("postID").? map {
      case email ~ description ~ storageSize ~ locationID ~ postID =>
        Post(email, description, storageSize, locationID, postID)
    }

  def insert(post: Post): Long = DB.withConnection { implicit conn =>
    createPostSql.on(
      'email -> post.email,
      'description -> post.description,
      'storageSize -> post.storageSize,
      'locationID -> post.locationID
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

  def getPostInfo(id: Long): Option[PostInfo] = DB.withConnection { implicit conn =>
    import LocationSql.locationParser
    import UserSqlParser.userParserGen
    val userParser = userParserGen("USER.")
    getPostInfoSql.on(
      'postID -> id
    ).as((postParser ~ locationParser ~ userParser).singleOpt).map { 
      case post ~ location ~ user => PostInfo(post, location,  user)
    }
  }
  
  def getPostsByLocationID(locationID: Long): List[Post] = DB.withConnection { implicit conn =>
    findPostsByLocationID.on(
      'locationID -> locationID
    ).as(postParser *)
  }
  
  def getPostsByCity(city: String): List[Post] = DB.withConnection { implicit conn =>
    findPostsByCity.on(
      'city -> city
    ).as(postParser *)
  }

  def removeById(id: Long, email: String): Boolean = DB.withConnection { implicit conn =>
    removePostByIdSql.on(
      'postID -> id,
      'email -> email
    ).executeUpdate() > 0
  }

  def updateById(id: Long, email: String, description: String,
      storageSize: Int): Int = DB.withConnection { implicit conn =>
    updatePostById.on(
      'postID-> id,
      'description-> description,
      'storageSize-> storageSize,
      'email -> email
    ).executeUpdate()
  }
}
