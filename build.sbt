ThisBuild / scalaVersion := "2.13.15"

ThisBuild / version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    name := """test_play_framework""",
    libraryDependencies ++= Seq(
      guice,
      javaJpa,
      "org.hibernate" % "hibernate-core" % "6.6.1.Final",
      "mysql" % "mysql-connector-java" % "8.0.33",
      "com.typesafe.play" %% "play-mailer" % "9.1.0",
      "com.typesafe.play" %% "play-mailer-guice" % "9.1.0"

    )
  )

libraryDependencies += guice
libraryDependencies += "jakarta.persistence" % "jakarta.persistence-api" % "3.2.0"
libraryDependencies += "at.favre.lib" % "bcrypt" % "0.10.2"
libraryDependencies += "com.google.zxing" % "javase" % "3.5.3"
libraryDependencies += "com.google.zxing" % "core" % "3.5.3"



