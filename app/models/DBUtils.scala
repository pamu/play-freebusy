package models

import java.sql.Timestamp
import java.util.Date

import scala.concurrent.Future
import slick.driver.PostgresDriver.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Created by pnagarjuna on 06/09/15.
 */
object DBUtils {
  def saveNew(user: User): Future[Int] = DB.db.run((
    DB.users.filter(_.key === user.key).exists.result.flatMap { exists => {
      if (! exists) {
        DB.users += user
      } else {
        DB.users.filter(_.key === user.key).result.flatMap { fbu =>
          DB.users.filter(_.key === user.key).update(user.copy(id = fbu.head.id))
        }
      }
    }}).transactionally)

  def getFBU(key: String): Future[User] = DB.db.run(DB.users.filter(_.key === key).result.head)

  def checkRefreshRequired(key: String): Future[Boolean] = DB.db.run((
    DB.users.filter(_.key === key).result.flatMap { users => {
      val user = users.head
      val current = System.currentTimeMillis()
      DB.users.filter(_.key === key).filter(_.lastRefreshTime < (current - ((user.refreshPeriod - 60) * 1000))).exists.result
    }}).transactionally)
}
