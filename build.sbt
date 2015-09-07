name := "play-freebusy"

version := "1.0-SNAPSHOT"

scalaVersion := """2.11.6"""

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.4.0",
  "com.typesafe.play" %% "play-ws" % "2.4.0",
  "com.typesafe.slick" %% "slick" % "3.0.1",
  "com.zaxxer" % "HikariCP" % "2.3.8",
  "org.postgresql" % "postgresql" % "9.4-1200-jdbc41"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
