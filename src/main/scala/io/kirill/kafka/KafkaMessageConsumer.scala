package io.kirill.kafka

import cats.effect._
import fs2.kafka._
import io.circe.generic.auto._
import io.circe.parser._
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.kirill.configs.KafkaConfig
import io.kirill.event.Event
import org.apache.avro.generic.GenericRecord

import scala.jdk.CollectionConverters._

class KafkaMessageConsumer[F[_]: Timer: ConcurrentEffect: ContextShift, K, V] private (
    config: KafkaConfig
)(
    implicit kd: Deserializer[F, K],
    vd: Deserializer[F, V]
) {

  private val autoOffsetReset = config.offset match {
    case "Latest" | "latest"     => AutoOffsetReset.Latest
    case "Earliest" | "earliest" => AutoOffsetReset.Earliest
    case _                       => AutoOffsetReset.None
  }

  private val settings =
    ConsumerSettings[F, K, V](keyDeserializer = kd, valueDeserializer = vd)
      .withAutoOffsetReset(autoOffsetReset)
      .withBootstrapServers(config.servers)
      .withGroupId(config.groupId)
      .withEnableAutoCommit(true)

  def streamFrom(topic: String): fs2.Stream[F, ConsumerRecord[K, V]] =
    consumerStream(settings)
      .evalTap(_.subscribeTo(topic))
      .flatMap(_.stream)
      .evalMap(commitable => Sync[F].pure(commitable.record))

  def streamFromPartitioned[V1](topic: String)(processingPipe: fs2.Pipe[F, ConsumerRecord[K, V], V1]): fs2.Stream[F, V1] =
    consumerStream(settings)
      .evalTap(_.subscribeTo(topic))
      .flatMap(_.partitionedStream)
      .map { partitionStream =>
        partitionStream.map(_.record).through(processingPipe)
      }
      .parJoinUnbounded
}

object KafkaMessageConsumer {
  implicit def eventDeserializer[F[_]: Sync]: Deserializer[F, Event] = Deserializer.instance { (_, _, bytes) =>
    Sync[F].fromEither(decode[Event](bytes.map(_.toChar).mkString))
  }

  implicit def genericAvroDeserializer[F[_]: Sync](implicit config: KafkaConfig): Deserializer[F, GenericRecord] =
    Deserializer.delegate[F, GenericRecord](
      new KafkaDeserializer[GenericRecord] {
        private val avroDeserializer = new KafkaAvroDeserializer()
        private val deserializerConf = Map(
          "schema.registry.url"  -> config.schemaRegistryUrl,
          "specific.avro.reader" -> true.asInstanceOf[java.lang.Boolean]
        )
        avroDeserializer.configure(deserializerConf.asJava, false)

        override def deserialize(topic: String, data: Array[Byte]): GenericRecord =
          avroDeserializer.deserialize(topic, data).asInstanceOf[GenericRecord]
      }
    )

  def apply[F[_]: Timer: ConcurrentEffect: ContextShift, K, V](
      config: KafkaConfig
  )(
      implicit kd: Deserializer[F, K],
      vd: Deserializer[F, V]
  ): KafkaMessageConsumer[F, K, V] =
    new KafkaMessageConsumer(config)
}
