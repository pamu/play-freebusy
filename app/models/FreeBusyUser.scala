package models

/**
 * Created by pnagarjuna on 06/09/15.
 */
case class FreeBusyUser(key: String, accessToken: String, refreshToken: String, refreshPeriod: Long, id: Option[Long] = None)
