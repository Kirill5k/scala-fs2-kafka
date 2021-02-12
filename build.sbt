import Dependencies._

ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "io.github.kirill5k"
ThisBuild / organizationName := "kirill5k"

lazy val root = (project in file("."))
  .settings(
    name := "scala-fs2-kafka",
    libraryDependencies ++= Seq(
      logCats,
      pureConfig, pureConfigCats,
      catsCore, catsEffect,
      circeCore, circeGeneric, circeParser,
      avro, kafkaAvro,
      fs2Kafka,
      kafkaStreams,
      redisCats, redisCatsLogging,
      http4sCore,
      http4sDsl,
      http4sServer,
      http4sClient,
      http4sCirce,
      scalaTest % Test,
      catsEffectTest % Test,
      mockito % Test, mockitoScalatest % Test,
      kafkaEmbedded % Test,
      redisEmbedded % Test
    ),
    resolvers ++= Seq(
      "io.confluent" at "https://packages.confluent.io/maven/"
    )
  )

