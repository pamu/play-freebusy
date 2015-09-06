name := "play-freebusy"

version := "1.0-SNAPSHOT"

scalaVersion := """2.11.6"""

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.4.0"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
