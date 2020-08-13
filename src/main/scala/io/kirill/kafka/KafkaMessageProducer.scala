package io.kirill.kafka

import cats.effect.{ConcurrentEffect, ContextShift, Sync}
import fs2.kafka.{produce, ProducerRecord, ProducerRecords, ProducerSettings, Serializer}
import fs2._
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.configs.KafkaConfig
import io.kirill.event.Event

class KafkaMessageProducer[F[_]: ConcurrentEffect: ContextShift, K, V] private (
    config: KafkaConfig
)(implicit ks: Serializer[F, K], vs: Serializer[F, V]) {

  private val settings: ProducerSettings[F, K, V] =
    ProducerSettings(keySerializer = ks, valueSerializer = vs)
      .withBootstrapServers(config.servers)

  def streamTo(topic: String): Pipe[F, (K, V), Unit] =
    inputStream =>
      inputStream
        .map { case (key, value) => ProducerRecord(topic, key, value) }
        .map(rec => ProducerRecords.one(rec))
        .through(produce(settings))
        .evalMap(_ => Sync[F].pure(()))
}

object KafkaMessageProducer {
  implicit def eventSerializer[F[_]: Sync]: Serializer[F, Event] = Serializer.instance[F, Event] { (_, _, event) =>
    Sync[F].delay(event.asJson.noSpaces.getBytes("UTF-8"))
  }

  def apply[F[_]: ConcurrentEffect: ContextShift, K, V](
      config: KafkaConfig
  )(implicit ks: Serializer[F, K], vs: Serializer[F, V]): KafkaMessageProducer[F, K, V] =
    new KafkaMessageProducer[F, K, V](config)
}
