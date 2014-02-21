package com.storagexchange.utils

import org.apache.commons.codec.binary.Base64
import play.api.Play
import play.api.libs.Crypto
import java.net.URLEncoder
import java.net.URI

object Base64AES {

  def encrypt(id: Long): String = encrypt(id.toString)
  def decryptLong(message: String): Long = {
    val Digit = "(\\d+)".r
    decrypt(message) match {
      case Digit(num) => num.toLong
      case msg => throw new Exception(s"The number ${msg} is not a long ")
    }
  }

  def encrypt(message: String): String = {
    val aes = Crypto.encryptAES(message)
    val base64Encoded = new String(Base64.encodeBase64(aes.getBytes))
    URLEncoder.encode(base64Encoded)
  }

  def decrypt(message: String): String = {
    val urlDecoded = new URI(message).getPath()
    val aes = Base64.decodeBase64(urlDecoded.getBytes)
    Crypto.decryptAES(new String(aes))
  }
}
