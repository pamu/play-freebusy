package models

import java.sql.Timestamp

/**
 * Created by pnagarjuna on 06/09/15.
 */
case class User(key: String,
                accessToken: String,
                refreshToken: String,
                refreshPeriod: Long,
                lastRefreshTime: Timestamp,
                id: Option[Long] = None)
