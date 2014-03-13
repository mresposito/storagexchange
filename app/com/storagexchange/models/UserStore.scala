package com.storagexchange.models

import java.sql.Timestamp
import anorm._
import play.api.db._
import play.api.Play.current

case class User(name: String,
  email: String,
  password: String,
  universityId: Long,
  lastLogin: Option[Timestamp] = None,
  created: Option[Timestamp] = None,
  userId: Option[Long] = None)

/**
 * Methods that we will be using from
 * the interface to the database
 */
trait UserStore {
  
  def insert(user: User): Long
  
  def getById(id: Long): Option[User]
  def getByEmail(email: String): Option[User]
  
  def authenticate(email: String, password: String): Boolean
}

// Actual implementation of User Store method
object UserDAL extends UserStore {

  private[this] val createUserSql = {
    SQL("""
      INSERT INTO "User"
        ("userID", "name", "email", "verifiedEmail", "universityID", "thumbnail", "creationTime", "lastLogin")
      VALUES
        (1, 'john', 'tmp@gmail.com', 'true', 1,'hello',3,4)
    """.stripMargin)
  }

  def insert(user: User): Long = DB.withConnection { implicit conn =>
  	createUserSql.execute()
  	0
  }

  def getById(id: Long): Option[User] = None
  def getByEmail(email: String): Option[User] = None

  def authenticate(email: String, password: String): Boolean = false
}
