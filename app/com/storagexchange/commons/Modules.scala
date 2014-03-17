package com.storagexchange.commons

import com.tzavellas.sse.guice.ScalaModule
import com.storagexchange.models._
import com.storagexchange.utils._
import com.storagexchange.mails._

trait CommodModule extends ScalaModule {
  /**
   * Method to use to define bindings
   */
  def configure = {
    bind[UserStore].to[UserDAL]
    bind[Clock].to[RealClock]
    additionalConf
  }
  def additionalConf = {}
}

class ProdModule extends CommodModule {
  override def additionalConf = {
    bind[MailSender].to[MandrillSender]
    bind[PasswordHelper].to[PlayWithBCryptHelper]
    bind[IdHasher].to[Base64AES]
  }
}

class DevModule extends CommodModule {
  override def additionalConf = {
    bind[MailSender].to[FakeSender]
    bind[IdHasher].to[FakeIdHasher]
    bind[PasswordHelper].to[PlayWithBCryptHelper]
  }
}

class TestModule extends CommodModule {
  override def additionalConf = {
    bind[MailSender].to[FakeSender]
    bind[IdHasher].to[FakeIdHasher]
    bind[PasswordHelper].to[FakePasswordHelper]
  }
}
