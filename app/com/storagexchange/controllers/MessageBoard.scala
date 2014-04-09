package com.storagexchange.controllers

import com.storagexchange.models._
import com.storagexchange.views

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import com.typesafe.scalalogging.slf4j.Logging
import javax.inject.Singleton
import javax.inject.Inject

case class MessageRequest(
  toUser:  String,
  message: String)

case class ReplyRequest(
  replyToId: Long,
  replyToUser: String,
  message: String)

@Singleton
class MessageBoard @Inject()(messageStore: MessageStore) 
  extends Controller with Secured {

  val newMessageForm = Form(
    mapping(
      "toUser"  -> nonEmptyText(minLength = 4),
      "message" -> nonEmptyText(minLength = 4)
      )(MessageRequest.apply)(MessageRequest.unapply)
    )

  val newReplyForm = Form(
    mapping(
      "replyToId"   -> longNumber(min = 1),
      "replyToUser" -> nonEmptyText(minLength = 4),
      "message"     -> nonEmptyText(minLength = 4)
      )(ReplyRequest.apply)(ReplyRequest.unapply)
    )

  def newMessage = IsAuthenticated { username => _ =>
    Ok(views.html.message.newmessage(newMessageForm))
  }

  // TODO: have id and email of message we're replying to as parameter
  def newReply = IsAuthenticated { username => _ =>
    Ok(views.html.message.newreply(newReplyForm))
  }

  def receiveNewMessage = IsAuthenticated { username => implicit request =>
    newMessageForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.error404()),
      messageData => { 
        messageStore.insert(Message(username, messageData.toUser, messageData.message))
        Redirect(routes.MessageBoard.myMessages)
      }
    )
  }

  def receiveNewReply = IsAuthenticated { username => implicit request =>
    newReplyForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.error404()),
      replyData => { 
        messageStore.reply(replyData.replyToId, 
          Message(username, replyData.replyToUser, replyData.message))
        Redirect(routes.MessageBoard.myMessages)
      }
    )
  }

  def myMessages = IsAuthenticated { username => _ => 
    val messageList = messageStore.getByEmail(username)
    val conversationList = for (message <- messageList) yield messageStore.getConversationById(message.messageID.getOrElse(0))
    Ok(views.html.message.mymessages(conversationList))
  }
  
}
