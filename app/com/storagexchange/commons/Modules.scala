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
    bind[PostStore].to[PostDAL]
    bind[DataGenerator].to[JavaFakerDataGenerator]
    additionalConf
  }
  def additionalConf = {}
}

class ProdModule extends CommodModule {
  override def additionalConf = {
    bind[MailSender].to[MandrillSender]
    bind[PasswordHelper].to[PlayWithBCryptHelper]
    bind[IdHasher].to[Base64AES]
    bind[Clock].to[RealClock]
  }
}

class DevModule extends CommodModule {
  override def additionalConf = {
    bind[MailSender].to[FakeSender]
    bind[IdHasher].to[FakeIdHasher]
    bind[PasswordHelper].to[PlayWithBCryptHelper]
    bind[Clock].to[RealClock]
  }
}

class TestModule extends CommodModule {
  override def additionalConf = {
    bind[MailSender].to[FakeSender]
    bind[IdHasher].to[FakeIdHasher]
    bind[PasswordHelper].to[FakePasswordHelper]
    bind[Clock].to[FakeClock]
  }
}
