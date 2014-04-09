package com.storagexchange.mails

import play.api.Play
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.ws.WS
import play.api.Play.current
import play.api.libs.concurrent._
import Execution.Implicits._
import com.typesafe.scalalogging.slf4j.Logging

//not sure why we use case classes
case class Recipient(name: String, email: String)
case class EmailMessage(html: String,
  subject: String,
  to: List[Recipient],
  from_name: String = "StorageExchange",
  from_email: String = "noreply@storagexchange.com")

trait MailSender {
  def send (message: EmailMessage)
}

class FakeSender extends MailSender with Logging {
  def send(message: EmailMessage) = {
    logger.info(s"Sending email to ${message.to}")
  }
}

class MandrillSender extends MailSender {
  val mandrillKey = Play.current.configuration.getString("mandrill.key")
  val mandrillRoot = Play.current.configuration.getString("mandrill.apiURL")

  case class MandrillWrapper( 
    message: EmailMessage,
    async: Boolean = false,
    ip_pool: String = "Main Pool",
    //getOrElse is for defaults
    key: String  = mandrillKey.getOrElse("no key"))

  implicit val recipientFormatter = Json.format[Recipient]
  implicit val messageFormatter = Json.format[EmailMessage]
  implicit val mandrillFormatter = Json.format[MandrillWrapper]

  //non-blocking
  def send(message: EmailMessage) = scala.concurrent.Future {
    val mandrill = MandrillWrapper(message)
    WS.url(mandrillRoot.get + "/messages/send").post(Json.toJson(mandrill))
  }
}
