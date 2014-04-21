package com.storagexchange.commons

import com.storagexchange.models._
import com.github.javafaker.Faker
import scala.util.Random
import javax.inject.Singleton
import javax.inject.Inject
import play.api.db.DB
import play.api.Play.current
import com.storagexchange.utils.PasswordHelper
import com.storagexchange.search.DataSearch
import java.math.BigDecimal
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.Play
import com.typesafe.scalalogging.slf4j.Logging

trait DataGenerator {
  def createFakeData: Unit
  def initializeUniversities: Unit
}

@Singleton
class JavaFakerDataGenerator @Inject()(userStore: UserStore,
    postStore: PostStore, search: DataSearch,
    passwordHasher: PasswordHelper, universityStore: UniversityStore,
    locationStore: LocationStore) extends DataGenerator with Logging {
  
  private val maxStorage = 3000
  private val numberOfUsers = 100
  private val maxPosts = 10
  private val locationGenVal = 4
  private val universityGenVal = 4

  private val random = new Random()
  private val faker = new Faker
  private val michele = User("michele", "esposito",
      "m@e.com", 
      passwordHasher.createPassword("123456"), 0)
  
  def createFakeData = for {
    user <- (generateUsers(numberOfUsers) ++ List(michele))
  } yield {
    insertUser(user)
    createPosts(user)
  }

  private def createPosts(user: User) = for {
    i <- 1 to random.nextInt(maxPosts)
  } yield {
    insertPost(generatePost(user.email))
  }

	private def insertPost(post: Post) = {
	  val id = postStore.insert(post)
	  search.insertPostLoc(post.copy(postID = Some(id)))
  }

  private def insertUser(user: User) = try { 
	  val userId = userStore.insert(user)
  } catch {
    case e: Exception => Unit
  }
  /**
   * Generating methods
   */
  private def generateUsers(limit: Int): Seq[User] = for(i <- 1 to limit) yield generateUser
  private def generateUser = User(faker.firstName,
     faker.lastName,
     emailAddress,
     password,
     random.nextInt(universityGenVal)+1)
  private def generatePost(email: String) = Post(email, 
      faker.sentence(),
      random.nextInt(maxStorage), 
      random.nextInt(locationGenVal)+1)

  private def emailAddress = sampleString + "@gmail.com"
  private def password = sampleString
  private def sampleString = random.nextString(random.nextInt(10) + 10)

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

  lazy private  val universities : List[UniversityInformation] = {
    val jsonFile = Play.application.getFile("public/data/universities.json")
    val filePath = jsonFile.toString()
    val jsonContent = scala.io.Source.fromFile(filePath).mkString
    val universityList: JsValue = Json.parse(jsonContent)
    universityList.as[List[UniversityInformation]]
  }

  def initializeUniversities: Unit = {
    //insert json content into universities table
    universities.map{ university => university match {
      case UniversityInformation(name, website, colors,
          logo, locationID, lat,
          lng, city, state, address, zip) => {
          locationStore.insert(Location(name,lat,lng,city,state,address,zip,None))
          universityStore.insert(University(locationID,name,website,logo,colors,None))
        }
        case _ => logger.error("Invalid JSON formatting")
      } 
    }
  }

}