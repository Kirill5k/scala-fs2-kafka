import sbt._

object Dependencies {
  private lazy val pureConfigVersion = "0.12.3"
  private lazy val circeVersion = "0.12.3"
  private lazy val mockitoVersion = "1.10.3"
  private lazy val redisCatsVersion = "0.9.6"
  private lazy val kafkaVersion = "2.5.0"
  private lazy val confluentVersion = "5.5.0"
  private lazy val http4sVersion = "0.21.7"

  lazy val logCats = "io.chrisdavenport" %% "log4cats-slf4j" % "1.0.1"

  lazy val pureConfig = "com.github.pureconfig" %% "pureconfig" % pureConfigVersion
  lazy val pureConfigCats = "com.github.pureconfig" %% "pureconfig-cats-effect" % pureConfigVersion

  lazy val circeCore = "io.circe" %% "circe-core" % circeVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  lazy val circeParser = "io.circe" %% "circe-parser" % circeVersion

  lazy val catsCore = "org.typelevel" %% "cats-core" % "2.1.0"
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "2.1.2"

  lazy val kafkaStreams = "org.apache.kafka" %% "kafka-streams-scala" % kafkaVersion
  lazy val kafkaAvro = "io.confluent" % "kafka-avro-serializer" % confluentVersion
  lazy val avro = "org.apache.avro" % "avro" % "1.9.1"

  lazy val fs2Kafka = "com.github.fd4s" %% "fs2-kafka" % "1.0.0"

  lazy val redisCats = "dev.profunktor" %% "redis4cats-effects" % redisCatsVersion
  lazy val redisCatsLogging = "dev.profunktor" %% "redis4cats-log4cats" % redisCatsVersion

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.1.0"
  lazy val catsEffectTest = "com.codecommit" %% "cats-effect-testing-scalatest" % "0.4.0"
  lazy val mockito = "org.mockito" %% "mockito-scala" % mockitoVersion
  lazy val mockitoScalatest = "org.mockito" %% "mockito-scala-scalatest" % mockitoVersion
  lazy val kafkaEmbedded = "io.github.embeddedkafka" %% "embedded-kafka" % kafkaVersion
  lazy val redisEmbedded = "com.github.sebruck" %% "scalatest-embedded-redis" % "0.4.0"

  def http4s(artifact: String): ModuleID = "org.http4s" %% artifact % http4sVersion
  lazy val http4sCore = http4s("http4s-core")
  lazy val http4sDsl = http4s("http4s-dsl")
  lazy val http4sServer = http4s("http4s-blaze-server")
  lazy val http4sClient = http4s("http4s-blaze-client")
  lazy val http4sCirce = http4s("http4s-circe")
}
