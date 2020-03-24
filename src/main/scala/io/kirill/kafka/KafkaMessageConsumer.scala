package io.kirill.kafka

import cats.effect._
import fs2.kafka.{AutoOffsetReset, ConsumerRecord, ConsumerSettings, Deserializer, consumerStream}
import io.kirill.configs.KafkaConsumerConfig
import io.kirill.event.Event
import io.circe.generic.auto._
import io.circe.parser._


class KafkaMessageConsumer[F[_]: Timer: ConcurrentEffect: ContextShift, K, V] private (
      config: KafkaConsumerConfig
    )(
      implicit kd: Deserializer[F, K], vd: Deserializer[F, V]
    ) {

  private val autoOffsetReset = config.autoOffsetReset match {
    case "Latest" => AutoOffsetReset.Latest
    case "Earliest" => AutoOffsetReset.Earliest
    case _ => AutoOffsetReset.None
  }

  private val settings =
    ConsumerSettings[F, K, V](keyDeserializer = kd, valueDeserializer = vd)
      .withAutoOffsetReset(autoOffsetReset)
      .withBootstrapServers(s"${config.host}:${config.port}")
      .withGroupId(config.groupId)

  def streamFrom(topic: String): fs2.Stream[F, ConsumerRecord[K, V]] =
    consumerStream(settings)
      .evalTap(_.subscribeTo(topic))
      .flatMap(_.stream)
      .evalMap(commitable => Sync[F].pure(commitable.record))
}

object KafkaMessageConsumer {
  implicit def eventDeserializer[F[_]](implicit s: Sync[F]): Deserializer[F, Event] = Deserializer.instance {
    (_, _, bytes) => s.fromEither(decode[Event](bytes.map(_.toChar).mkString))
  }

  def apply[F[_]: Timer: ConcurrentEffect: ContextShift, K, V](config: KafkaConsumerConfig)(implicit kd: Deserializer[F, K], vd: Deserializer[F, V]): KafkaMessageConsumer[F, K, V] =
    new KafkaMessageConsumer(config)
}
