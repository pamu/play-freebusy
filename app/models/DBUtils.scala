package models

import scala.concurrent.Future
import slick.driver.PostgresDriver.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Created by pnagarjuna on 06/09/15.
 */
object DBUtils {
  def saveNew(freeBusyUser: FreeBusyUser): Future[Int] = DB.db.run((
    DB.freeBusyUsers.filter(_.key === freeBusyUser.key).exists.result.flatMap { exists => {
      if (! exists) {
        DB.freeBusyUsers += freeBusyUser
      } else {
        DB.freeBusyUsers.filter(_.key === freeBusyUser.key).result.flatMap { fbu =>
          DB.freeBusyUsers.filter(_.key === freeBusyUser.key).update(freeBusyUser.copy(id = fbu.head.id))
        }
      }
    }}).transactionally)
  def getFBU(key: String): Future[FreeBusyUser] = DB.db.run(DB.freeBusyUsers.filter(_.key === key).result.head)
}
