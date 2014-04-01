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
import play.api.libs.functional.syntax._
import com.storagexchange.models._
import java.math.BigDecimal

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
      case Mode.Dev => initializeUniversities
      case _ => Unit
    }
  }

  case class UniversityInformation(name: String,
    website: String,
    colors: String,
    logo: String,
    locationID: Long,
    lat: BigDecimal,
    lng: BigDecimal,
    city: String,
    state: String,
    address: String,
    zip: String)

  implicit val universityReader: Reads[UniversityInformation] = (
    (__ \ "name").read[String] and
    (__ \ "website").read[String] and
    (__ \ "colors").read[String] and
    (__ \ "logo").read[String] and
    (__ \ "locationID").read[Long] and
    (__ \ "lat").read[BigDecimal] and
    (__ \ "lng").read[BigDecimal] and
    (__ \ "city").read[String] and
    (__ \ "state").read[String] and
    (__ \ "address").read[String] and
    (__ \ "zip").read[String] 
  )(UniversityInformation)

  private def getJsonList( ) : List[UniversityInformation] = {
    val jsonFile = Play.application.getFile("universities.json")
    val filePath = jsonFile.toString()
    val jsonContent = scala.io.Source.fromFile(filePath).mkString
    val jsonObj: JsValue = Json.parse(jsonContent)
    val universityList = (jsonObj \ "universities")
    val universities = universityList.as[List[UniversityInformation]]
    return universities
  }

  private def initializeUniversities = {
    val universityTable = injector.getInstance(classOf[UniversityStore])
    val locationTable = injector.getInstance(classOf[LocationStore])
    val universities = getJsonList() 
    //insert json content into universities table
    universities.foreach(university => 
                            university match {
                              case UniversityInformation(name, website, colors, logo, locationID, lat, lng, city, state, address, zip) =>
                                locationTable.insert(Location(name,lat,lng,city,state,address,zip,None))
                                universityTable.insert(University(locationID,name,website,logo,colors,None))
                              case _ => logger.error("Invalid JSON formatting")
                            }
                        )

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
