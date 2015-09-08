package constants

/**
 * Created by pnagarjuna on 06/09/15.
 */
object Constants {
  object GoogleOauth {
    val client_id = "1022260014178-3j2a1eh106anem74t2hon3ve5bcntrs6.apps.googleusercontent.com"
    val client_secret = "e8ATC6oCAVUQZx8aby9oroSl"
    val GoogleOauth2 = "https://accounts.google.com/o/oauth2/auth"
    val TokenEndpoint = "https://www.googleapis.com/oauth2/v3/token"
  }
  object CalendarAPI {
    def events(calendarId: String) =  s"https://www.googleapis.com/calendar/v3/calendars/${calendarId}/events"
  }
}
