package models

import java.sql.Timestamp

import slick.driver.PostgresDriver.api._
/**
 * Created by pnagarjuna on 06/09/15.
 */
class Users(tag: Tag) extends Table[User](tag, "free_busy_users") {
  def key = column[String]("key")
  def accessToken = column[String]("access_token")
  def refreshToken = column[String]("refresh_token")
  def refreshPeriod = column[Long]("refresh_period")
  def lastRefreshTime = column[Timestamp]("last_refresh_time")
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def * = (key, accessToken, refreshToken, refreshPeriod, lastRefreshTime, id.?) <> (User.tupled, User.unapply)
}
