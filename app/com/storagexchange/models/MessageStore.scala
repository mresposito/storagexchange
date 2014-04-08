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
  def reply(id: Long, message: Message): Option[Message]

  def getById(id: Long): Option[Message]
  def getConversationById(id: Long): List[Message]
  def getRootIdsByEmail(email: String): List[Long]

  def updateById(id: Long, email: String, message: String): Int
}

@Singleton
class MessageDAL extends MessageStore {
  def insert(message: Message): Long = throw new UnsupportedOperationException("not implemented")

  // Return None if id doesn't exist or has a child already.
  def reply(id: Long, message: Message): Option[Message] = throw new UnsupportedOperationException("not implemented")

  def getById(id: Long): Option[Message] = throw new UnsupportedOperationException("not implemented")

  /** 
   * Gets conversation rooted at message with id i.e. recurse/iterate until childID is None.
   */
  def getConversationById(id: Long): List[Message] = throw new UnsupportedOperationException("not implemented")

  /**
   * Message ids where email is either from or to and parent is None. This will give us the
   * beginning of a conversation initiated either by user with email or another user. Can
   * get remainder of conversation via call to getConversationById().
   */
  def getRootIdsByEmail(email: String): List[Long] = throw new UnsupportedOperationException("not implemented")

  def updateById(id: Long, email: String, message: String): Int = throw new UnsupportedOperationException("not implemented")
}
