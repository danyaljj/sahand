import sbt._
import sbt.Keys._


lazy val root = project in file(".")

lazy val envUser = System.getenv("COGCOMP_USER")
lazy val user = if (envUser == null) System.getProperty("user.name") else envUser
lazy val keyFile = new java.io.File(Path.userHome.absolutePath + "/.ssh/id_rsa")

lazy val commonSettings = Seq(
  organization := "github.sahand",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.11.8", // this is necessary because Play doesn't work in earlier versions of scala
  javaOptions ++= Seq("-Xmx25G", "-XX:MaxMetaspaceSize=5g"),
  publishTo := Some(
    Resolver.ssh(
      "CogcompSoftwareRepo", "bilbo.cs.illinois.edu",
      "/mounts/bilbo/disks/0/www/cogcomp/html/m2repo/") as (user, keyFile)
    ),
  resolvers ++= Seq(
    Resolver.mavenLocal,
    "CogcompSoftware" at "http://cogcomp.cs.illinois.edu/m2repo/"
  )
)

lazy val client = (project in file("client")).
  settings(commonSettings: _*).
  settings(
    name:= "sahand-client",
    libraryDependencies ++= Seq(
      "com.typesafe.play" % "play-json_2.11" % "2.5.10"
    )
  )

lazy val server = (project in file("server")).
  dependsOn(client).
  aggregate(client).
  settings(commonSettings: _*).
  enablePlugins(PlayScala).
  disablePlugins(PlayLogback).
  settings(
    name:= "sahand-server",
    libraryDependencies ++= Seq(
      filters,
      "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test,
      "com.typesafe.play" % "play_2.11" % "2.5.10",
      "com.medallia.word2vec" % "Word2VecJava" % "0.10.3",
      "org.cogcomp" % "cogcomp-datastore" % "1.9.7",
      "edu.illinois.cs.cogcomp" % "illinois-wnsim" % "2.2.1",
      "edu.illinois.cs.cogcomp" % "illinois-entity-similarity" % "2.0.4",
      "edu.illinois.cs.cogcomp" % "illinois-phrasesim" % "1.1",
      "de.jungblut.glove" % "glove" % "0.1"
    )
  )

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "github.sahand.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "github.sahand.binders._"
