package models

import java.net.URI
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future

/**
 * Created by pnagarjuna on 06/09/15.
 */
object DB {
  lazy val uri = new URI(s"""postgres://dmgoujfbmunolj:0ugoOmt_jcMoRpzcdwFnQE7Ptr@ec2-54-235-162-144.compute-1.amazonaws.com:5432/dc2bfacovjbkvl""")
  lazy val username = uri.getUserInfo.split(":")(0)

  lazy val password = uri.getUserInfo.split(":")(1)

  lazy val db = Database.forURL(
    driver = "org.postgresql.Driver",
    url = "jdbc:postgresql://" + uri.getHost + ":" + uri.getPort + uri.getPath, user = username,
    password = password
  )

  lazy val users = TableQuery[Users]

  def init: Future[Unit] = DB.db.run(DBIO.seq(users.schema.create))

  def clean: Future[Unit] = DB.db.run(DBIO.seq(users.schema.drop))
}
