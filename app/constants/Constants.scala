package constants

/**
 * Created by pnagarjuna on 06/09/15.
 */
object Constants {
  object GoogleOauth {
    val client_id = "954456399684-5tvd9glj085o0ehg8fp907ja85dm3hv5.apps.googleusercontent.com"
    val client_secret = "_ohIYxY4azxWSE5ntFf1cQts"
    val GoogleOauth2 = "https://accounts.google.com/o/oauth2/auth"
    val TokenEndpoint = "https://www.googleapis.com/oauth2/v3/token"
    val redirectURI = "http://busyboy.herokuapp.com/oauth2callback"
  }
  object CalendarAPI {
    def events(calendarId: String) =  s"https://www.googleapis.com/calendar/v3/calendars/${calendarId}/events"
  }
}
