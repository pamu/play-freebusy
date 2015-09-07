package models

import slick.driver.PostgresDriver.api._
/**
 * Created by pnagarjuna on 06/09/15.
 */
class FreeBusyUsers(tag: Tag) extends Table[FreeBusyUser](tag, "free_busy_users") {
  def key = column[String]("key")
  def accessToken = column[String]("access_token")
  def refreshToken = column[String]("refresh_token")
  def refreshPeriod = column[Long]("refresh_period")
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def * = (key, accessToken, refreshToken, refreshPeriod, id.?) <> (FreeBusyUser.tupled, FreeBusyUser.unapply)
}
