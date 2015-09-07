package http

/**
 * Created by pnagarjuna on 06/09/15.
 */
object WS {
  val builder = new com.ning.http.client.AsyncHttpClientConfig.Builder()
  val client = new play.api.libs.ws.ning.NingWSClient(builder.build())
}
