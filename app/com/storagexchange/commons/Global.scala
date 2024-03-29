package com.storagexchange.commons

import com.google.inject.Guice
import com.typesafe.scalalogging.slf4j.Logging
import play.Logger
import play.api._
import play.api.db.DB
import play.api.Play.current
import java.io.File
import com.typesafe.config.ConfigFactory
import com.storagexchange.search.DataSearch
import org.h2.jdbc.JdbcSQLException

object Global extends GlobalSettings with Logging {

  /**
   * Select modules to load from according
   * to the Play mode
   */
  private lazy val injector = {
    import Mode._
    Play.mode match {
      case Prod => Guice.createInjector(new ProdModule)
      case Dev => Guice.createInjector(new DevModule)
      case Test => Guice.createInjector(new TestModule)
    }
  }

  override def onStart(app: Application) {
  	val search = getControllerInstance(classOf[DataSearch])
    // refresh everything
  	search.deleteIndices
  	search.createIndices
    Play.mode match {
      case Mode.Dev => {
        injectData
      }
      case _ => Unit
    }
  }
  
  private def injectData = {
    val generator = injector.getInstance(classOf[DataGenerator])
    try {
     generator.initializeUniversities 
    } catch {
      case e: JdbcSQLException => Unit
    }
    generator.createFakeData
  }

  /**
   * overrides the default injector
   */
  override def getControllerInstance[A](clazz: Class[A]) = {
    injector.getInstance(clazz)
  }

  /**
   * Loads the configuration file
   */
  override def onLoadConfig(config: Configuration, path: File, classloader: ClassLoader, mode: Mode.Mode): Configuration = {
    val cnfFile = s"${mode.toString.toLowerCase}.conf"
    Logger.info(s" loading config from ${cnfFile}")
    val modeSpecificConfig = config ++ Configuration(ConfigFactory.load(cnfFile))
    super.onLoadConfig(modeSpecificConfig, path, classloader, mode)
  }
}
