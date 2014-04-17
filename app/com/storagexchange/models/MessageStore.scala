package com.storagexchange.models

import java.sql.Timestamp
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import javax.inject.Singleton
import javax.inject.Inject

case class Message(fromUser: String,
  toUser: String,
  message: String,
  parentID: Option[Long] = None,
  childID: Option[Long] = None,
  messageID: Option[Long] = None)

/**
 * Methods that we will be using from
 * the interface to the database
 */
trait MessageStore {
  def insert(message: Message): Long
  def reply(id: Long, message: Message): Long

  def getById(id: Long): Option[Message]
  def getConversationById(id: Long): List[Message]
  def getByEmail(email: String): List[Message]

  def updateById(id: Long, fromUser: String, message: String): Int
}

@Singleton
class MessageDAL extends MessageStore {

  private[this] val createMessageSql = {
    SQL("""
      INSERT INTO Message
        (fromUser, toUser, message)
      VALUES
        ({fromUser}, {toUser}, {message})
    """.stripMargin)
  }

  private[this] val createReplySql = {
    SQL("""
      INSERT INTO Message
        (fromUser, toUser, message, parentID)
      VALUES
        ({fromUser}, {toUser}, {message}, {parentID})
    """.stripMargin)
  }

  private[this] val findMessageByIdSql = {
    SQL("""
       SELECT *
       FROM Message
       WHERE messageID = {messageID}
    """.stripMargin)
  }

  private[this] val findChildByIdSql = {
    SQL("""
       SELECT *
       FROM Message
       WHERE childID = {childID}
    """.stripMargin)
  }

  private[this] val findMessagesByEmailSql = {
    SQL("""
       SELECT *
       FROM Message
       WHERE (fromUser = {email} OR toUser = {email}) AND parentID IS NULL
    """.stripMargin) 
  }

  private[this] val updateMessageByIdSql = {
    SQL("""
       Update Message
       SET message = {message}
       WHERE messageID = {messageID} AND
        fromUser = {fromUser}
    """.stripMargin)
  }

  private[this] val updateChildByIdSql = {
    SQL("""
       Update Message
       SET childID = {childID}
       WHERE messageID = {messageID}
    """.stripMargin)
  }

  private[this] val updateParentByIdSql = {
    SQL("""
       Update Message
       SET parentID = {parentID}
       WHERE messageID = {messageID}
    """.stripMargin)
  }

  implicit val messageParser = 
    str("fromUser") ~
    str("toUser") ~
    str("message") ~
    long("parentID").? ~
    long("childID").? ~
    long("messageID").? map {
      case fromUser ~ toUser ~ message ~ parentID ~ childID ~ messageID =>
        Message(fromUser, toUser, message, parentID, childID, messageID)
    }

  def insert(message: Message): Long = DB.withConnection { implicit conn =>
    createMessageSql.on(
      'fromUser -> message.fromUser,
      'toUser -> message.toUser,
      'message -> message.message
    ).executeInsert(scalar[Long].single)
  }

  def reply(id: Long, message: Message): Long = DB.withConnection { implicit conn =>
    val childID = createReplySql.on(
      'fromUser -> message.fromUser,
      'toUser -> message.toUser,
      'message -> message.message,
      'parentID -> Some(id)
    ).executeInsert(scalar[Long].single)

    // Update parent message's childID to that returned above.
    updateChildByIdSql.on(
      'childID -> childID,
      'messageID -> id
    ).executeUpdate()

    childID
  }

  def getById(id: Long): Option[Message] = DB.withConnection { implicit conn =>
    findMessageByIdSql.on(
      'messageID -> id
    ).as(messageParser.singleOpt)
  }

  /** 
   * Gets conversation rooted at message with id i.e. recurse until childID is None.
   */
  def getConversationById(id: Long): List[Message] = getById(id).map { message => 
    message.childID.map { childID => 
      message :: getConversationById(childID) 
    }.getOrElse(List(message))
  }.getOrElse(Nil)

  /**
   * Messages where email is either from or to and parent is None. This will give us the
   * first message of a conversation initiated either by user with email or another user
   * sent to user with email. Can get remainder of conversation via call to getConversationById().
   */
  def getByEmail(email: String): List[Message] = DB.withConnection { implicit conn =>
    findMessagesByEmailSql.on(
      'email -> email
    ).as(messageParser *)
  }

  def updateById(id: Long, fromUser: String, message: String): Int = DB.withConnection { implicit conn =>
    updateMessageByIdSql.on(
      'messageID -> id,
      'fromUser -> fromUser,
      'message -> message
    ).executeUpdate()
  }

}
