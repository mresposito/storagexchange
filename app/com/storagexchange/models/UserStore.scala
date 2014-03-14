package com.storagexchange.models

import com.storagexchange.utils.PasswordHelper
import java.sql.Timestamp
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

import javax.inject.Singleton
import javax.inject.Inject

case class User(name: String,
  surname: String,
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
@Singleton
class UserDAL @Inject()(passwordHasher: PasswordHelper) extends UserStore {
  
  private[this] val createUserSql = {
    SQL("""
      INSERT INTO User
        (name, surname, email, password, universityID, creationTime)
      VALUES
        ({name}, {surname}, {email}, {password}, {universityId}, {creationTime})
    """.stripMargin)
  }

  private[this] val verifyUserSql = {
    SQL("""
      SELECT password
      FROM User
      WHERE email = {email}
    """.stripMargin)
  }

  def insert(user: User): Long = DB.withConnection { implicit conn =>
  	createUserSql.on(
	    'name -> user.name,
	    'surname -> user.surname,
	    'email -> user.email,
	    'password -> user.password,
	    'universityId -> user.universityId,
	    'creationTime -> 0 // FIXME: user clock
		).executeInsert(scalar[Long].single)
  }

  def getById(id: Long): Option[User] = None
  def getByEmail(email: String): Option[User] = None

  def authenticate(email: String, password: String): Boolean = DB.withConnection { implicit conn =>
    verifyUserSql.on(
      'email -> email
    ).as(scalar[String].singleOpt).map { hashedPassword => 
      passwordHasher.checkPassword(password, hashedPassword) 
    }.getOrElse(false)
  }
}
