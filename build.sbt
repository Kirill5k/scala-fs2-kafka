import Dependencies._

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "io.kirill"
ThisBuild / organizationName := "kirill"

lazy val root = (project in file("."))
  .settings(
    name := "scala-fs2-kafka",
    libraryDependencies ++= Seq(
      logbackClassic,
      scalaLogging,
      pureConfig, pureConfigCats,
      catsCore, catsEffect,
      circeCore, circeGeneric, circeParser,
      fs2Kafka,
      redisCats,
      scalaTest % Test,
      catsEffectTest % Test,
      mockito % Test, mockitoScalatest % Test,
      kafkaEmbedded % Test
    )
  )

