package com.storagexchange.mail

import play.api.templates.Html
import play.api.Play
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.ws.WS
import play.api.Play.current
import play.api.libs.concurrent._
import Execution.Implicits._

//not sure why we use case classes
case class Recipient(name: String, email: String)
case class Message(html: String,
  subject: String,
  to: List[Recipient],
  from_name: String = "StorageExchange",
  from_email: String = "noreply@storagexchange.com")

trait MailSender {
  def send (message: Message)
}

class MandrillSender {
  val mandrillKey = Play.current.configuration.getString("mandrill.key")
  val mandrillRoot = Play.current.configuration.getString("mandrill.apiURL")

  case class MandrillWrapper( 
    message: Message,
    async: Boolean = false,
    ip_pool: String = "Main Pool",
    //getOrElse is for defaults
    key: String  = mandrillKey.getOrElse("no key"))

  implicit val recipientFormatter = Json.format[Recipient]
  implicit val messageFormatter = Json.format[Message]
  implicit val mandrillFormatter = Json.format[MandrillWrapper]

  //non-blocking
  def send(message: Message) = scala.concurrent.Future {
    val mandrill = MandrillWrapper(message)
    WS.url(mandrillRoot.get + "/messages/send").post(Json.toJson(mandrill))
  }
}

class MailController(val mailSender: MandrillSender)  {
  def sendVerificationEmail(recipient: Recipient, verificationURL: String) = {
    mailSender.send(Message(
      verificationURL,
      "Welcome to StorageExchange",
      List(recipient)))
  }

  def send(message: Message) = mailSender.send(message)
      
}

