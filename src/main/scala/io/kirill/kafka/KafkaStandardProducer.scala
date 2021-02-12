package io.kirill.kafka

import java.util.Properties

import cats.effect.{Async, Resource, Sync}
import io.kirill.configs.KafkaConfig
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.Serializer

class KafkaStandardProducer[F[_]: Async, K, V](
    private val producer: KafkaProducer[K, V]
) {

  def send(topic: String, key: K, value: V): F[Unit] = {
    val record = new ProducerRecord[K, V](topic, key, value)
    sendRecord(record)
  }

  private def sendRecord(record: ProducerRecord[K, V]): F[Unit] =
    Async[F].async { k =>
      val _ = producer.send(record, new Callback {
        override def onCompletion(metadata: RecordMetadata, e: Exception): Unit =
          if (metadata != null)
            k(Right(()))
          else
            k(Left(e))
      })
    }
}

object KafkaStandardProducer {

  def make[F[_]: Async, K, V](config: KafkaConfig)(
      implicit kd: Serializer[K],
      vd: Serializer[V]
  ): Resource[F, KafkaStandardProducer[F, K, V]] = {
    val acquireProducer = Sync[F].delay {
      val props = new Properties()
      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.servers)
      props.put(ProducerConfig.ACKS_CONFIG, "all")
      props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4")
      new KafkaProducer[K, V](props)
    }

    Resource
      .fromAutoCloseable(acquireProducer)
      .map(p => new KafkaStandardProducer[F, K, V](p))
  }
}
