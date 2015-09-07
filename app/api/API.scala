package api

import java.text.SimpleDateFormat
import java.util.Date

import constants.Constants
import constants.Constants.GoogleOauth
import http.WS
import models.{FreeBusyUser, DBUtils}
import play.api.Logger
import play.api.libs.json.Json

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Created by pnagarjuna on 06/09/15.
 */
object API {

  implicit class MapConverter(rMap: Map[String, String]) {
    def convert: List[String] = rMap.map(pair => s"${pair._1}=${pair._2}").toList
  }

  def refresh(refreshToken: String, key: String): Future[Int] = {
    val body = Map[String, String](
      ("client_id" -> Constants.GoogleOauth.client_id),
      ("client_secret" -> Constants.GoogleOauth.client_secret),
      ("refresh_token" -> refreshToken),
      ("grant_type" -> "refresh_token")
    )

    WS.client.url(Constants.GoogleOauth.TokenEndpoint)
      .withHeaders("Content-Type" -> "application/x-www-form-urlencoded; charset=utf-8")
      .post(body.convert.mkString("", "&", "")).flatMap {
      response => {
        val jsonBody = Json.parse(response.body)
        val freeBusyUser = FreeBusyUser(key, (jsonBody \ "access_token").as[String],
          refreshToken, (jsonBody \ "expires_in").as[Long])
        val dbAction = DBUtils.saveNew(freeBusyUser)
        dbAction
      }
    }
  }

  def freebusy(accessToken: String, timeMin: Date, timeMax: Date) = {
    val simpleDateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss")
    val body = Json.obj(
      "timeMin" -> simpleDateFormat.format(timeMin),
      "timeMax" -> simpleDateFormat.format(timeMax),
      "items" -> Json.arr(Json.obj("id" -> "nagarjuna.pamu@gmail.com"))
    )
    WS.client.url(Constants.CalendarAPI.freeBusy).post(body.toString())
  }
}