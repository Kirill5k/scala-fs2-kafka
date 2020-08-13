package io.kirill.kafka

import cats.effect._
import io.kirill.configs.{KafkaConfig, KafkaConsumerConfig}
import io.kirill.event.Event
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.kafka.common.serialization.StringSerializer
import org.scalatest.freespec.{AnyFreeSpec, AsyncFreeSpec}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext

class KafkaMessageConsumerSpec extends AnyWordSpec with Matchers with EmbeddedKafka {
  implicit val ss = new StringSerializer
  val ex: ExecutionContext = ExecutionContext.Implicits.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ex)
  implicit val t: Timer[IO] = IO.timer(ex)
  implicit val embeddedKafkaConfig: EmbeddedKafkaConfig = EmbeddedKafkaConfig(kafkaPort = 29092, zooKeeperPort = 0)

  val topic = "topic.test"

  val config = KafkaConfig("localhost:29092", "app-1", "group-1", "http://localhost:8081", "earliest")

  "A KafkaMessageConsumer" should {

    "get stream of string messages" in {
      withRunningKafkaOnFoundPort(embeddedKafkaConfig) { _ =>
        val messagesToPublish = List("Hello", "World", "and", "kafka")
        messagesToPublish.foreach(m => publishToKafka[String, String](topic, "key", m))

        val consumer = KafkaMessageConsumer[IO, String, String](config)

        val receivedMessages = consumer.streamFrom(topic).evalMap(rec => IO.pure(rec.value)).take(4).compile.toList.unsafeRunSync
        receivedMessages must contain theSameElementsAs messagesToPublish
      }
    }

    "get stream of deserialized events" in {
      import KafkaMessageConsumer._

      withRunningKafkaOnFoundPort(embeddedKafkaConfig) { _ =>
        val eventsToPublish = List(
          """{"id":"e1", "name": "event 1"}""",
          """{"id":"e2", "name": "event 2"}""",
          """{"id":"e3", "name": "event 3"}"""
        )
        eventsToPublish.foreach(m => publishToKafka[String, String](topic, "key", m))

        val consumer = KafkaMessageConsumer[IO, String, Event](config)

        val receivedMessages = consumer.streamFrom(topic).evalMap(rec => IO.pure(rec.value)).take(3).compile.toList.unsafeRunSync()
        receivedMessages must be (List(Event("e1", "event 1"), Event("e2", "event 2"), Event("e3", "event 3")))
      }
    }
  }

}
