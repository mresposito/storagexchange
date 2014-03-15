package com.storagexchange.commons

import com.tzavellas.sse.guice.ScalaModule
import com.storagexchange.models._
import com.storagexchange.utils._

trait CommodModule extends ScalaModule {
  /**
   * Method to use to define bindings
   */
  def configure = {
    bind[UserStore].to[UserDAL]
    additionalConf
  }
  def additionalConf = {}
}

class ProdModule extends CommodModule {
  override def additionalConf = {
    bind[PasswordHelper].to[PlayWithBCryptHelper]
    bind[IdHasher].to[Base64AES]
  }
}

class DevModule extends CommodModule {
  override def additionalConf = {
    bind[PasswordHelper].to[PlayWithBCryptHelper]
    bind[IdHasher].to[Base64AES]
  }
}

class TestModule extends CommodModule {
  override def additionalConf = {
    bind[IdHasher].to[FakeIdHasher]
    bind[PasswordHelper].to[FakePasswordHelper]
  }
}
