package com.storagexchange.common

import com.tzavellas.sse.guice.ScalaModule

trait CommodModule extends ScalaModule {
  /**
   * Method to use to define bindings
   */
  def configure = {
    // example configuration:
    // bind[MailSender].to[FakeSender]
  }
}

class ProdModule extends CommodModule {
}

class DevModule extends CommodModule {
}

class TestModule extends CommodModule {
}
