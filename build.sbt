name := "storagexchange"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  // Add your project dependencies here,
  jdbc,
  anorm,
  cache,
  "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
  "mysql" % "mysql-connector-java" % "5.1.6",
  "com.typesafe.play" %% "play-slick" % "0.5.0.8",
  "com.google.inject" % "guice" % "3.0",
  "com.tzavellas" % "sse-guice" % "0.7.1",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.sksamuel.elastic4s" %% "elastic4s" % "1.0.1.1",
  "com.github.javafaker" % "javafaker" % "0.3",
  // test libraries
  "org.specs2" %% "specs2" % "2.3.3" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.0" % "test"
)

play.Project.playScalaSettings

templatesImport ++= Seq(
  "com.storagexchange.controllers",
  "com.storagexchange.models",
  "com.storagexchange.views"
)

seq(jasmineSettings : _*)

// Jasmine settings
(test in Test) <<= (test in Test) dependsOn (jasmine)

appJsDir <+= baseDirectory { src => src / "app" / "assets" / "js" }

appJsLibDir <+= baseDirectory { src => src / "public" / "js" }

jasmineTestDir <+= baseDirectory { src => src / "test" / "assets" / "js" }

jasmineConfFile <+= baseDirectory { src => src / "test" / "assets" / "js" / "test.dependencies.js" }
