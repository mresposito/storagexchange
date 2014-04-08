package com.storagexchange.models

import com.storagexchange.utils._
import org.specs2.mutable._
import org.specs2.execute.AsResult
import play.api.test._
import play.api.test.Helpers._
import play.api.db._
import play.api.Play.{current => curr}
import java.sql.Timestamp
import org.h2.jdbc.JdbcSQLException

class MessageStoreSpec extends Specification {
  val messageStore: MessageStore = new MessageDAL
  val message1 = Message("user1@test.com", "user2@test.com", "My message to user2.")
  val message2 = Message("user2@test.com", "user1@test.com", "My message to user1.")
  val message1Copy = message1.copy(messageID = Some(1))
  val message2Copy = message2.copy(messageID = Some(2))
  val message1ReplyCopy = message1.copy(childID = Some(2), messageID = Some(1))
  val message2ReplyCopy = message2.copy(parentID = Some(1), messageID = Some(2))

  val InsertMessage = BeforeHook {
    DB.withConnection { implicit conn =>
      messageStore.insert(message1).toInt must beEqualTo(1)
      messageStore.insert(message2).toInt must beEqualTo(2)
    }
  }

  val InsertMessageReply = BeforeHook {
    DB.withConnection { implicit conn =>
      messageStore.insert(message1).toInt must beEqualTo(1)
      messageStore.reply(1, message2).toInt must beEqualTo(2)
    }
  }

  "Message Store" should {
    "insert a message" in RunningApp {
      messageStore.insert(message1).toInt must beEqualTo(1)
    }
    "find message by id" in InsertMessage {
      messageStore.getById(1) must beSome(message1Copy)
    }
    "not find a non-existent message by id" in InsertMessage {
      messageStore.getById(3) must beNone
    }
    "find reply to a message by id" in InsertMessageReply {
      messageStore.getById(2) must beSome(message2ReplyCopy)
    }
    "update parent message after reply" in InsertMessageReply {
      messageStore.getById(1) must beSome(message1ReplyCopy)
    }
    "find root messages by email for sender" in InsertMessageReply {
      messageStore.getMessagesByEmail(message1.fromUser) must beEqualTo(List(message1ReplyCopy))
    }
    "find root messages by email for recipient" in InsertMessageReply {
      messageStore.getMessagesByEmail(message2.fromUser) must beEqualTo(List(message1ReplyCopy))
    }
    "get conversation by root message id" in InsertMessageReply {
      messageStore.getConversationById(1) must beEqualTo(List(message1Copy, message2Copy))
    }   
    "get conversation by internal message id" in InsertMessageReply {
      messageStore.getConversationById(2) must beEqualTo(List(message2Copy))
    }  
    "update message by id" in InsertMessage {
      val updatedMessage = Message("user1@test.com", "user2@test.com", 
        "My updated message to user2", None, None, Some(1))
      messageStore.updateById(1, message1.fromUser, "My updated message to user2")
      messageStore.getById(1) must beSome(updatedMessage)
    }
  }
}
