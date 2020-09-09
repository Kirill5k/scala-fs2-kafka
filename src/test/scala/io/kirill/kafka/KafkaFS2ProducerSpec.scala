package io.kirill.kafka

import cats.effect._
import io.kirill.configs.{KafkaConfig, KafkaConsumerConfig, KafkaProducerConfig}
import io.kirill.event.Event
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext

class KafkaFS2ProducerSpec extends AnyWordSpec with Matchers with EmbeddedKafka {
  val ex: ExecutionContext = ExecutionContext.Implicits.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ex)
  implicit val t: Timer[IO] = IO.timer(ex)
  implicit val embeddedKafkaConfig: EmbeddedKafkaConfig = EmbeddedKafkaConfig(kafkaPort = 39092, zooKeeperPort = 0)
  implicit val sd: StringDeserializer = new StringDeserializer

  val topic = "topic.test"

  val config = KafkaConfig("localhost:29092", "app-1", "group-1", "http://localhost:8081", "earliest")


  "A KafkaMessageProducer" should {

    "publish stream of string messages" in {
      withRunningKafkaOnFoundPort(embeddedKafkaConfig) { _ =>
        val producer = KafkaMessageProducer[IO, String, String](config)
        val messages = List("Hello", "World", "and", "kafka")

        fs2.Stream
          .emits(messages)
          .covary[IO]
          .map(msg => ("key", msg))
          .through(producer.streamTo(topic))
          .compile
          .toList
          .unsafeRunSync()

        val publishedMessage = consumeNumberMessagesFrom(topic, messages.size)

        publishedMessage must contain theSameElementsAs messages
      }
    }

    "publish stream of event objects" in {
      import KafkaFS2Producer._
      withRunningKafkaOnFoundPort(embeddedKafkaConfig) { _ =>
        val producer = KafkaMessageProducer[IO, String, Event](config)

        fs2.Stream
          .emits(List(Event("e1", "event 1"), Event("e2", "event 2"), Event("e3", "event 3")))
          .covary[IO]
          .map(msg => ("key", msg))
          .through(producer.streamTo(topic))
          .compile
          .toList
          .unsafeRunSync()

        val publishedMessages = consumeNumberMessagesFrom(topic, 3)

        publishedMessages must be (List(
          """{"id":"e1","name":"event 1"}""",
          """{"id":"e2","name":"event 2"}""",
          """{"id":"e3","name":"event 3"}"""
        ))
      }
    }
  }

}
