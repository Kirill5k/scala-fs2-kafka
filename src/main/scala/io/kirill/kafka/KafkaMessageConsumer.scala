package io.kirill.kafka

import cats.effect._
import fs2.kafka.{AutoOffsetReset, ConsumerRecord, ConsumerSettings, Deserializer, consumerStream}
import io.kirill.configs.KafkaConsumerConfig

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

  private val consumer = consumerStream(settings)

  def stream: fs2.Stream[F, ConsumerRecord[K, V]] =
    consumer
      .evalTap(_.subscribeTo(config.topic))
      .flatMap(_.stream)
      .evalMap(_.record)
}

object KafkaMessageConsumer {

  def apply[F[_]: Timer: ConcurrentEffect: ContextShift, K, V](config: KafkaConsumerConfig)(implicit kd: Deserializer[F, K], vd: Deserializer[F, V]): KafkaMessageConsumer[F, K, V] =
    new KafkaMessageConsumer(config)
}
