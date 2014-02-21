package com.storagexchange.utils

import org.mindrot.jbcrypt.BCrypt
import play.api.libs.Crypto

trait PasswordHelper {

  def createPassword(password: String): String
  def checkPassword(password: String, hashedPassword: String): Boolean
}

class BCryptHelper extends PasswordHelper {

  def createPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt)
  def checkPassword(password: String, hashedPassword: String): Boolean = {
    BCrypt.checkpw(password, hashedPassword)
  }
}
/**
 * Strongest encrypter, use this one
 */
class PlayWithBCryptHelper extends PasswordHelper {

  def createPassword(password: String): String = {
    val first = BCrypt.hashpw(password, BCrypt.gensalt)
    Crypto.encryptAES(first)
  }

  def checkPassword(password: String, hashedPassword: String): Boolean = {
    val decrypt = Crypto.decryptAES(hashedPassword)
    BCrypt.checkpw(password, decrypt)
  }
}

class FakeHelper extends PasswordHelper {

  def createPassword(password: String): String = password + "hello"
  def checkPassword(password: String, hashedPassword: String): Boolean =  {
    password == hashedPassword
  }
}
