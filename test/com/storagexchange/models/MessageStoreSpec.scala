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
  val message1 = Message("user1@test.com", "user2@test.com", "My message to user2.",
    None, Some(2), Some(1))
  val message2 = Message("user2@test.com", "user1@test.com", "My reply to user1.",
    Some(1), None, Some(2))
  val message1Copy = message1.copy()
  val message2Copy = message2.copy()

  val InsertMessage = BeforeHook {
    DB.withConnection { implicit conn =>
      messageStore.insert(message1).toInt must beEqualTo(1)
      messageStore.insert(message2).toInt must beEqualTo(2)
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
      messageStore.getById(99) must beNone
    }
    "find root message ids by email" in InsertMessage {
      messageStore.getRootIdsByEmail(message1.fromUser) must beEqualTo(List(Some(1)))
      messageStore.getRootIdsByEmail(message2.fromUser) must beNone
    }
    "update message by id" in InsertMessage {
      val updatedMessage = Message("user1@test.com", "user2@test.com", 
        "My updated message to user2", None, Some(2), Some(1))
      messageStore.updateById(1, message1.fromUser, "My updated message to user2")
      messageStore.getById(1) must beSome(updatedMessage)
    }

  }

}
