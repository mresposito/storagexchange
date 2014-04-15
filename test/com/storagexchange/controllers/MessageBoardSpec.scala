package com.storagexchange.controllers

import com.storagexchange.models._
import com.storagexchange.utils._
import play.api.test._
import play.api.test.Helpers._
import play.api.Play.current
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import com.storagexchange.models.MessageStore

trait MessageTest extends Specification {
    
  val message1 = Message("user1@gmail.com", "user2@yahoo.com", "user1 message to user2")
  val message2 = Message("user2@yahoo.com", "user1@gmail.com", "user2 reply to user1", Some(1))
  val message3 = Message("user1@yahoo.com", "user2@gmail.com", "user2 reply to user1", Some(2))
  val message4 = Message("user2@yahoo.com", "user3@gmail.com", "user2 message to user3")
  val message1Modified = message1.copy(message = "My updated message", messageID = Some(1))
  val message2Modified = message2.copy(message = "My updated message", messageID = Some(2))  

  val CreateMessages = BeforeHook {
    val Some(create1) = route(createMessageRequest(message1))
    val Some(create2) = route(createMessageRequest(message2))
    val Some(create3) = route(createMessageRequest(message3)) 
  }

  val CreateReplies = BeforeHook {
    val Some(create1) = route(createMessageRequest(message1))
    val Some(create2) = route(createReplyRequest(message2))
    val Some(create3) = route(createReplyRequest(message3))
  }

  def createMessageRequest(message: Message) = FakeRequest(POST, "/message").
    withSession(("email", message.fromUser)).
    withFormUrlEncodedBody(
      "fromUser" -> message.fromUser,
      "toUser"   -> message.toUser,
      "message"  -> message.message)

  def createReplyRequest(reply: Message) = FakeRequest(POST, "/reply").
    withSession(("email", reply.fromUser)).
    withFormUrlEncodedBody(
      "replyToId"   -> reply.parentID.toString,
      "replyToUser" -> reply.toUser,
      "message"     -> reply.message)

  def modifyMessage(message: Message) = withSession(
    FakeRequest(POST, routes.MessageBoard.modify(message.messageID.get).url).
    withFormUrlEncodedBody(
      "toUser"  -> message.toUser,
      "message" -> message.message))

  def requestWithSession(route: String) = withSession(FakeRequest(GET, route))
  def withSession[T](request: FakeRequest[T]) = request.withSession(("email", message1.fromUser))
}

class MessageBoardSpec extends Specification with MessageTest {

    "Message Board" should {

        "View messages" in {
            "accept valid message" in RunningApp {
              val Some(create) = route(createMessageRequest(message1))
              status(create) must beEqualTo(SEE_OTHER)
            }
            "view messages I've sent" in CreateMessages {
              val Some(myMessages) = route(requestWithSession(routes.MessageBoard.myMessages.url))
              contentAsString(myMessages) must contain(message1.message)
            }
            "view messages I've received" in CreateMessages {
              val Some(myMessages) = route(requestWithSession(routes.MessageBoard.myMessages.url))
              contentAsString(myMessages) must contain(message2.message)
            }
            "should not display messages between other users" in CreateMessages {
              val Some(myMessages) = route(requestWithSession(routes.MessageBoard.myMessages.url))
              contentAsString(myMessages) must not contain(message4.message)
            }
            "cannot view my messages if not logged in" in CreateMessages {
              val Some(myMessages) = route(FakeRequest(GET, routes.MessageBoard.myMessages.url))
              status(myMessages) must beEqualTo(SEE_OTHER)
            }
        }

        "Reply to messages" in {
            "sender can view 1st reply" in CreateReplies {
              val Some(myMessages) = route(requestWithSession(routes.MessageBoard.myMessages.url))
              contentAsString(myMessages) must contain(message2.message)
            }
            "sender can view 2nd reply" in CreateReplies {
              val Some(myMessages) = route(requestWithSession(routes.MessageBoard.myMessages.url))
              contentAsString(myMessages) must contain(message3.message)
            }
        }

        "Modify messages" in {
            "can't modify a message that does not exist" in RunningApp {
              val Some(modify) = route(modifyMessage(message1Modified)) 
              status(modify) must beEqualTo(BAD_REQUEST)
            }
            "Can't modify a message I don't own" in CreateMessages {
              val Some(modify) = route(modifyMessage(message2Modified)) 
              status(modify) must beEqualTo(BAD_REQUEST)
            }
            "modifying my message should be successful" in CreateMessages {
              val Some(modify) = route(modifyMessage(message1Modified)) 
              status(modify) must beEqualTo(SEE_OTHER)
            }
            "change message text in modified message" in CreateMessages {
              val Some(modify) = route(modifyMessage(message1Modified)) 
              val Some(myMessages) = route(requestWithSession(routes.MessageBoard.myMessages.url))
              contentAsString(myMessages) must contain(message1Modified.message)
            }
            "modify message should update the message text" in CreateMessages {
              val Some(modify) = route(modifyMessage(message1Modified)) 
              val Some(myMessages) = route(requestWithSession(routes.MessageBoard.myMessages.url))
              contentAsString(myMessages) must not contain(message1.message)
            }
        }
    }
}