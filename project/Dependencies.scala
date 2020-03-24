import sbt._

object Dependencies {
  private lazy val pureConfigVersion = "0.12.3"
  private lazy val circeVersion = "0.12.3"
  private lazy val mockitoVersion = "1.10.3"

  lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"
  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

  lazy val pureConfig = "com.github.pureconfig" %% "pureconfig" % pureConfigVersion
  lazy val pureConfigCats = "com.github.pureconfig" %% "pureconfig-cats-effect" % pureConfigVersion

  lazy val circeCore = "io.circe" %% "circe-core" % circeVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  lazy val circeParser = "io.circe" %% "circe-parser" % circeVersion

  lazy val catsCore = "org.typelevel" %% "cats-core" % "2.1.0"
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "2.1.2"

  lazy val fs2Kafka = "com.github.fd4s" %% "fs2-kafka" % "1.0.0"

  lazy val redisCats = "dev.profunktor" %% "redis4cats-effects" % "0.9.6"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.1.0"
  lazy val catsEffectTest = "com.codecommit" %% "cats-effect-testing-scalatest" % "0.4.0"
  lazy val mockito = "org.mockito" %% "mockito-scala" % mockitoVersion
  lazy val mockitoScalatest = "org.mockito" %% "mockito-scala-scalatest" % mockitoVersion
  lazy val kafkaEmbedded = "io.github.embeddedkafka" %% "embedded-kafka" % "2.4.0"
}
