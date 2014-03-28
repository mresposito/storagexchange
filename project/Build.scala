import sbt._
import Keys._
import play.Project._
import org.scalastyle.sbt.ScalastylePlugin.{Settings => ScalaStyleSettings}

object ApplicationBuild extends Build {

  val appName         = "storagexchange"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
    "mysql" % "mysql-connector-java" % "5.1.6",
    "com.typesafe.play" %% "play-slick" % "0.5.0.8",
    "com.google.inject" % "guice" % "3.0",
    "com.tzavellas" % "sse-guice" % "0.7.1",
    "org.mindrot" % "jbcrypt" % "0.3m",
    "com.sksamuel.elastic4s" %% "elastic4s" % "0.90.10.0",
    "com.github.javafaker" % "javafaker" % "0.3",
    // test libraries
    "org.specs2" %% "specs2" % "2.3.3" % "test",
    "org.scalacheck" %% "scalacheck" % "1.11.0" % "test")

  val main = play.Project(appName, appVersion, appDependencies).
    settings(templatesImport += "com.storagexchange.controllers").
    settings(templatesImport += "com.storagexchange.models").
    settings(templatesImport += "com.storagexchange.views").
    settings(ScalaStyleSettings :_ *)
}
