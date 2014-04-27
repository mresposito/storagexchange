package com.storagexchange.models

import org.mockito.Mockito.{mock, when}
import com.storagexchange.utils._
import org.specs2.mutable._
import org.specs2.execute.AsResult
import play.api.test._
import play.api.test.Helpers._
import play.api.db._
import play.api.Play.{current => curr}
import java.sql.Timestamp
import org.h2.jdbc.JdbcSQLException
import com.storagexchange.controllers.UserTest
import com.storagexchange.controllers.PostTest

class RatingStoreSpec extends Specification with PostTest {
  val postStore: PostStore = new PostDAL

  val transactionStore: TransactionStore = new TransactionDAL
  val transaction1 = Transaction(10,new Timestamp(1397857973), new Timestamp(1397857973),
    1, "buyer@user.com", Some(post1.email), Some(1))
  val transaction2 = Transaction(3, new Timestamp(1397857973), new Timestamp(1397857973), 
    1,  "buyer2@user.com", Some(post1.email), Some(2))

  val ratingStore: RatingStore = new RatingDAL
  val rating1 = Rating(1, 5, "buyer@user.com", post1.email, Some(1))
  val rating2 = Rating(2, 2, "buyer2@user.com", post1.email, Some(2))

  val InsertTransaction = BeforeHook {
    DB.withConnection { implicit conn =>
      locationStore.insert(testLoc)
      postStore.insert(post1).toInt must beEqualTo(1)
      postStore.insert(post2).toInt must beEqualTo(2)
      transactionStore.insert(transaction1).toInt must beEqualTo(1)
      transactionStore.insert(transaction2).toInt must beEqualTo(2)
    }
  }

  val InsertRatings = BeforeHook {
    DB.withConnection { implicit conn =>
      locationStore.insert(testLoc)
      postStore.insert(post1).toInt must beEqualTo(1)
      postStore.insert(post2).toInt must beEqualTo(2)
      transactionStore.insert(transaction1).toInt must beEqualTo(1)
      transactionStore.insert(transaction2).toInt must beEqualTo(2)
      ratingStore.insert(rating1).toInt must beEqualTo(1)
      ratingStore.insert(rating2).toInt must beEqualTo(2)

    }
  }
  
  "Rating Store" should {
    "insert a rating" in InsertTransaction {
      ratingStore.insert(rating1).toInt must beEqualTo(1)
    }
    "find rating by id" in InsertRatings{
      ratingStore.getByID(1) must beSome(rating1)
    }
    "average score of ratee" in InsertRatings{
      ratingStore.getAvgByRatee(post1.email) must beSome(3.5)
    }
    "not find average score of non-ratee" in InsertRatings{
      ratingStore.getAvgByRatee("buyer@user.com") must beNone
    }
  }
}
