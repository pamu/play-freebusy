package controllers

import java.time.LocalDateTime
import java.util.{Calendar, Date}

import api.API
import constants.Constants
import http.WS
import models.{DBUtils, FreeBusyUser}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Application extends Controller {

  val form = Form(single("key" -> nonEmptyText(minLength = 4, maxLength = 8)))

  def index = Action {
    Ok(views.html.index(form))
  }

  def key() = Action { implicit request =>
    form.bindFromRequest().fold(
      hasErrors => BadRequest(views.html.index(hasErrors)),
      success => Redirect(routes.Application.oauth2(success))
    )
  }

  implicit class MapConverter(rMap: Map[String, String]) {
    def convert: List[String] = rMap.map(pair => s"${pair._1}=${pair._2}").toList
  }

  def oauth2(userId: String) = Action {
    val params = Map[String, String](
      ("scope" -> "https://www.googleapis.com/auth/calendar"),
      ("state" -> userId.toString),
      ("response_type" -> "code"),
      ("client_id" -> s"${Constants.GoogleOauth.client_id}"),
      ("redirect_uri" -> "http://freebusy.herokuapp.com/oauth2callback"),
      ("access_type" -> "offline"),
      ("approval_prompt" -> "force")
    ).convert.mkString("?", "&", "").toString
    val requestURI = s"${Constants.GoogleOauth.GoogleOauth2}${params}"
    Redirect(requestURI)
  }

  def oauth2callback(state: String, code: Option[String], error: Option[String]) = Action {
    code match {
      case Some(code) => Redirect(routes.Application.onCode(state, code))
      case None => {
        error match {
          case Some(err) => Redirect(routes.Application.index()).flashing("failure" -> s"Google server error, error: $err")
          case None => Redirect(routes.Application.index())
        }
      }
    }
  }

  def onCode(state: String, code: String) = Action.async {
    val body = Map[String, String](
      ("code" -> s"$code"),
      ("client_id" -> s"${Constants.GoogleOauth.client_id}"),
      ("client_secret" -> s"${Constants.GoogleOauth.client_secret}"),
      ("redirect_uri" -> "http://freebusy.herokuapp.com/oauth2callback"),
      ("grant_type" -> "authorization_code")
    )
    WS.client.url(Constants.GoogleOauth.TokenEndpoint)
      .withHeaders("Content-Type" -> "application/x-www-form-urlencoded; charset=utf-8")
      .post(body.convert.mkString("", "&", "")).flatMap { response => {
        val jsonBody = Json.parse(response.body)
        val freeBusyUser = FreeBusyUser(state, (jsonBody \ "access_token").as[String],
          (jsonBody \ "refresh_token").as[String], (jsonBody \ "expires_in").as[Long])
        val dbAction = DBUtils.saveNew(freeBusyUser)
        val result = dbAction.flatMap { result  => Future(Ok(s"Done ${freeBusyUser.toString}")) }
        dbAction.recover {case th => Ok(s"Failed, reason: ${th.getMessage}")}
        result
      }}
  }

  def refreshToken(state: String, refreshToken: String) = Action.async {
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
        val freeBusyUser = FreeBusyUser(state, (jsonBody \ "access_token").as[String],
          refreshToken, (jsonBody \ "expires_in").as[Long])
        val dbAction = DBUtils.saveNew(freeBusyUser)
        dbAction.recover {case th => Ok(s"Database operation failed, reason ${th.getMessage}") }
        dbAction.flatMap { _ => {
          Future(Ok(s"refresh done ${freeBusyUser.toString}"))
        }}
      }}}

  def events(accessToken: String) = Action.async { implicit request =>
    val start = Calendar.getInstance().getTime
    val end = Calendar.getInstance().getTime
    API.events(accessToken, start, end).map { response => {
      Ok(Json.parse(response.body))
    }}
  }
}