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
    bind[PasswordHelper].to[PlayWithBCryptHelper]
    additionalConf
  }
  def additionalConf = {}
}

class ProdModule extends CommodModule {
}

class DevModule extends CommodModule {
}

class TestModule extends CommodModule {
}
