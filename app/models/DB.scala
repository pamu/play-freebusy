package models

import java.net.URI
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future

/**
 * Created by pnagarjuna on 06/09/15.
 */
object DB {
  lazy val uri = new URI(s"""postgres://juldrwenibiidu:Aggx36AXCmEe6S5u1QD7gC0NWN@ec2-23-21-76-246.compute-1.amazonaws.com:5432/d97u5v3qubtr9v""")
  lazy val username = uri.getUserInfo.split(":")(0)

  lazy val password = uri.getUserInfo.split(":")(1)

  lazy val db = Database.forURL(
    driver = "org.postgresql.Driver",
    url = "jdbc:postgresql://" + uri.getHost + ":" + uri.getPort + uri.getPath, user = username,
    password = password
  )

  lazy val freeBusyUsers = TableQuery[FreeBusyUsers]

  def init: Future[Unit] = DB.db.run(DBIO.seq(freeBusyUsers.schema.create))

  def clean: Future[Unit] = DB.db.run(DBIO.seq(freeBusyUsers.schema.drop))
}
