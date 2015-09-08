package controllers

import java.sql.Timestamp
import java.util.Date

import api.API
import constants.Constants
import http.WS
import models.{User, DBUtils}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

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
      ("redirect_uri" -> s"${Constants.GoogleOauth.redirectURI}"),
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
      ("redirect_uri" -> s"${Constants.GoogleOauth.redirectURI}"),
      ("grant_type" -> "authorization_code")
    )
    WS.client.url(Constants.GoogleOauth.TokenEndpoint)
      .withHeaders("Content-Type" -> "application/x-www-form-urlencoded; charset=utf-8")
      .post(body.convert.mkString("", "&", "")).flatMap { response => {
        val jsonBody = Json.parse(response.body)
        val user = User(state, (jsonBody \ "access_token").as[String],
          (jsonBody \ "refresh_token").as[String], (jsonBody \ "expires_in").as[Long], new Timestamp(new Date().getTime))
        val dbAction = DBUtils.saveNew(user)
        val result = dbAction.flatMap { result  => Future(Ok(s"Done ${user.toString}")) }
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
        val user = User(state, (jsonBody \ "access_token").as[String],
          refreshToken, (jsonBody \ "expires_in").as[Long], new Timestamp(new Date().getTime))
        val dbAction = DBUtils.saveNew(user)
        dbAction.recover {case th => Ok(s"Database operation failed, reason ${th.getMessage}") }
        dbAction.flatMap { _ => {
          Future(Ok(s"refresh done ${user.toString}"))
        }}
      }}}


  case class EventQuery(key: String, timeMin: String, timeMax: String)

  implicit val eqReads: Reads[EventQuery] = (
    (JsPath \ "key").read[String] and
    (JsPath \ "timeMin").read[String] and (JsPath \ "timeMax").read[String]
  )(EventQuery.apply _)

  def events() = Action.async(parse.json) { implicit request =>
    request.body.validate[EventQuery] match {
      case success: JsSuccess[EventQuery] => {
        val eventQuery = success.value
        val dbAction = DBUtils.getUser(eventQuery.key)
        val response = dbAction.flatMap { user => {
          DBUtils.checkRefreshRequired(user.key).flatMap { status => {
            if (status) {
              val events = API.events(user.accessToken, eventQuery.timeMin, eventQuery.timeMax).map { response => {
                Ok(Json.parse(response.body))
              }}
              events.recover { case th => BadRequest(Json.obj("errors" -> th.getMessage)) }
              events
            } else {
              val refresh = API.refresh(user.refreshToken, user.key).flatMap { status =>
                val dbActionAfterRefresh = DBUtils.getUser(eventQuery.key).flatMap { user => {
                  val events = API.events(user.accessToken, eventQuery.timeMin, eventQuery.timeMax).map { response => {
                    Ok(Json.parse(response.body))
                  }}
                  events.recover { case th => BadRequest(Json.obj("errors" -> th.getMessage)) }
                  events
                }}
                dbActionAfterRefresh.recover { case th => BadRequest(Json.obj("errors" -> "DB action after refresh failed"))}
                dbActionAfterRefresh
              }
              refresh.recover {case th => BadRequest(Json.obj("errors" -> "Refresh action failed"))}
              refresh
            }
          }}

        }}
        dbAction.recover { case th => BadRequest(Json.obj("errors" -> s"No user with the key: ${eventQuery.key}")) }
        response
      }
      case error: JsError => Future(BadRequest(Json.obj("errors" -> error.errors.mkString(","))))
    }
  }

  def check(key: String) = Action.async {
    DBUtils.checkRefreshRequired(key).map { status =>
      Ok(status.toString)
    }
  }

}