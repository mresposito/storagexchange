package com.storagexchange.utils

import org.apache.commons.codec.binary.Base64
import play.api.Play
import play.api.libs.Crypto
import java.net.URLEncoder
import java.net.URI

trait IdHasher {

  def encrypt(id: Long): String
  def decrypt(id: String): String

  def decryptLong(id: String): Long = {
    val Digit = "(\\d+)".r
    decrypt(id) match {
      case Digit(num) => num.toLong
      case msg => throw new Exception(s"The number ${msg} is not a long ")
    }
  }
}

class Base64AES extends IdHasher {

  def encrypt(id: Long): String = encrypt(id.toString)

  def encrypt(message: String): String = {
    val aes = Crypto.encryptAES(message)
    val base64Encoded = new String(Base64.encodeBase64(aes.getBytes))
    URLEncoder.encode(base64Encoded, "UTF-8")
  }

  def decrypt(message: String): String = {
    val urlDecoded = new URI(message).getPath()
    val aes = Base64.decodeBase64(urlDecoded.getBytes)
    Crypto.decryptAES(new String(aes))
  }
}

class FakeIdHasher extends IdHasher {
  
  def encrypt(id: Long): String = id.toString
  def decrypt(id: String): String = id
}
