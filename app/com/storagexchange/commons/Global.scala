package com.storagexchange.commons

import com.google.inject.Guice
import com.typesafe.scalalogging.slf4j.Logging
import play.Logger
import play.api._
import play.api.db.DB
import play.api.Play.current
import java.io.File
import com.typesafe.config.ConfigFactory
import play.api.Play.current
import play.api.libs.json._

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

  /**
   * overrides the default injector
   */
  override def getControllerInstance[A](clazz: Class[A]) = {
    injector.getInstance(clazz)
  }


  override def onStart(app: Application) {
    Play.mode match {
      //we need to populate the table of universities everytime the app starts, be it dev or production
      case Mode.Dev => initialize_universities
      case Mode.Prod => initialize_universities
    }
  }

  private def initialize_universities = {
    val jsonFile = Play.application.getFile("universities.json")
    val filePath = jsonFile.toString()
    val jsonContent = scala.io.Source.fromFile(filePath).mkString
    val jsonObj: JsValue = Json.parse(jsonContent)
    //insert json content into universities table
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
