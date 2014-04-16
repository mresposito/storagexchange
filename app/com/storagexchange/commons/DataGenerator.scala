package com.storagexchange.commons

import com.storagexchange.models._
import com.github.javafaker.Faker
import scala.util.Random
import javax.inject.Singleton
import javax.inject.Inject
import scala.slick.session.{Database, Session}
import play.api.db.DB
import play.api.Play.current
import com.storagexchange.utils.PasswordHelper
import com.storagexchange.search.DataSearch
import java.math.BigDecimal

trait DataGenerator {
  def createFakeData: Unit
}

@Singleton
class JavaFakerDataGenerator @Inject()(userStore: UserStore,
    postStore: PostStore, search: DataSearch,
    passwordHasher: PasswordHelper, locationStore: LocationStore) extends DataGenerator {
  
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
	  search.insertPost(post.copy(postID = Some(id)))
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
}
