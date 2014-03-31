package com.storagexchange.models

import com.storagexchange.utils.PasswordHelper
import java.sql.Timestamp
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import javax.inject.Singleton
import javax.inject.Inject
import com.typesafe.scalalogging.slf4j.Logging
import com.storagexchange.utils.Clock

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
  def verify(id: Long): Boolean

  def getAll(): List[User]

  trait SelectQueries {
    def getById(id: Long): Option[User]
    def getByEmail(email: String): Option[User]
  }

  /**
   * Fake method to stub select
   * queries for verified users
   */
  def verified: SelectQueries
}

/**
 * All the user queries that we want to run
 */
trait UserSQLQueries {

  private[models] val userTable = "User"
   
  private[models] val createUserSql = {
    SQL("""
      INSERT INTO User
        (name, surname, email, password, universityID, creationTime, lastLogin)
      VALUES
        ({name}, {surname}, {email}, {password}, {universityId}, {creationTime}, {lastLogin})
    """.stripMargin)
  }

  private[models] val getUserPasswordSql = {
    SQL("""
      SELECT password
      FROM User
      WHERE email = {email}
    """.stripMargin)
  }
  private[models] val verifyUserSql = {
    SQL("""
      UPDATE User
      SET verifiedEmail=1
      WHERE userID = {id}
    """.stripMargin)
  }
  private[models] val updateLoginTimeSql = {
    SQL("""
      UPDATE User
      SET lastLogin={lastLogin}
      WHERE email={email}
    """.stripMargin)
  }
  
  /**
   * These queries change with user table
   */
  lazy private[models] val findUserByEmailSql = {
    SQL(s"""
       SELECT *
       FROM ${userTable}
       WHERE email = {email}
    """.stripMargin)
  }

  lazy private[models] val findUserById = {
    SQL(s"""
       SELECT *
       FROM ${userTable}
       WHERE userID = {id}
    """.stripMargin)
  }

  lazy private[models] val selectUser = {
    SQL(s"""
        SELECT * 
        FROM User
        """.stripMargin)
  }
}

// Actual implementation of User Store method
@Singleton
class UserDAL @Inject()(passwordHasher: PasswordHelper,
    clock: Clock) extends UserStore with UserSQLQueries with Logging {
  
  implicit val userParser = str("name") ~
    str("surname") ~
    str("email") ~
    str("password") ~
    long("universityId") ~ 
    long("userID").? ~
    long("creationTime") ~
    long("lastLogin") map {
      case name ~ surname ~ email ~ password ~ universityId ~ userId ~ created ~ lastLogin => {
        User(name, surname, email, password, universityId,
          Some(new Timestamp(created)), Some(new Timestamp(lastLogin)), userId)
      }
    }

  trait SelectQueriesImp extends SelectQueries {
    this: UserSQLQueries =>
    def getById(id: Long): Option[User] = DB.withConnection { implicit conn =>
     findUserById.on(
       'id -> id
     ).as(userParser.singleOpt)
    }

    def getByEmail(email: String): Option[User] = DB.withConnection { implicit conn =>
     findUserByEmailSql.on(
       'email -> email
     ).as(userParser.singleOpt)
    }
  }
  /**
   * Access objects for verified or all users.
   * Private is the default, so should only be accessed
   * here
   */
  private object all extends SelectQueriesImp with UserSQLQueries
  object verified extends SelectQueriesImp with UserSQLQueries {
    override val userTable = "VerifiedUser"
  }

  def getById(id: Long): Option[User] = all.getById(id)
  def getByEmail(email: String): Option[User] = all.getByEmail(email) 

  def getAll(): List[User] = DB.withConnection { implicit conn =>
    selectUser.as(userParser *)
  } 


  def insert(user: User): Long = DB.withConnection { implicit conn =>
    createUserSql.on(
      'name -> user.name,
      'surname -> user.surname,
      'email -> user.email,
      'password -> user.password,
      'universityId -> user.universityId,
      'creationTime -> clock.now.getTime(),
      'lastLogin -> clock.now.getTime()
    ).executeInsert(scalar[Long].single)
  }

  def authenticate(email: String, password: String): Boolean = {
    val valid = validateUser(email, password)
    if(valid) {
      updateLoginTime(email, clock.now.getTime())
    }
    valid
  }
  
  private def validateUser(email: String,
      password: String): Boolean = DB.withConnection { implicit conn =>
    getUserPasswordSql.on(
      'email -> email
    ).as(scalar[String].singleOpt).map { hashedPassword => 
      passwordHasher.checkPassword(password, hashedPassword) 
    }
  }.getOrElse(false)
  
  private [models] def updateLoginTime(email: String,
      time: Long): Boolean =  DB.withConnection { implicit conn =>
    updateLoginTimeSql.on(
       'email -> email,
       'lastLogin -> time
    ).executeUpdate() > 0
  }

  def verify(id: Long): Boolean = DB.withConnection { implicit conn =>
    verifyUserSql.on(
      'id -> id
    ).executeUpdate() > 0
  }
}
