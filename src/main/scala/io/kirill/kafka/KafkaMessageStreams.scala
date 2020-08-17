package io.kirill.kafka

import java.util.Properties

import cats.effect.{Async, Resource, Sync}
import cats.implicits._
import io.kirill.Logging
import io.kirill.configs.KafkaConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.streams.KafkaStreams.State
import org.apache.kafka.streams.errors.{DefaultProductionExceptionHandler, LogAndContinueExceptionHandler}
import org.apache.kafka.streams.kstream.ValueTransformerSupplier
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.{Consumed, KStream, Produced}
import org.apache.kafka.streams.state.{KeyValueStore, StoreBuilder}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

final class KafkaMessageStreams[F[_]: Sync](
    private val streams: KafkaStreams
) {

  def state(): F[State] =
    Sync[F].delay(streams.state())

  def start(): F[Unit] =
    Sync[F].delay[Unit](streams.start())

  def stop(): F[Unit] =
    state().flatMap {
      case s if s.isRunningOrRebalancing => Sync[F].delay(streams.close())
      case _                             => Sync[F].unit
    }
}

final class KafkaMessageStreamsTopology[K1, V1, K2, V2](
    private val builder: StreamsBuilder,
    private val source: KStream[K1, V1],
    private val sink: KStream[K2, V2]
) extends Logging {

  def withStateStore[K, V](store: StoreBuilder[KeyValueStore[K, V]]): KafkaMessageStreamsTopology[K1, V1, K2, V2] = {
    builder.addStateStore(store)
    new KafkaMessageStreamsTopology[K1, V1, K2, V2](builder, source, sink)
  }

  def filterValue(p: V2 => Boolean): KafkaMessageStreamsTopology[K1, V1, K2, V2] =
    new KafkaMessageStreamsTopology[K1, V1, K2, V2](builder, source, sink.filter((_, v) => p(v)))

  def filter(p: (K2, V2) => Boolean): KafkaMessageStreamsTopology[K1, V1, K2, V2] =
    new KafkaMessageStreamsTopology[K1, V1, K2, V2](builder, source, sink.filter(p))

  def map[K22, V22](f: (K2, V2) => (K22, V22)): KafkaMessageStreamsTopology[K1, V1, K22, V22] =
    new KafkaMessageStreamsTopology[K1, V1, K22, V22](builder, source, sink.map(f))

  def flatTransformValues[V22](
      transformer: ValueTransformerSupplier[V2, Iterable[V22]],
      storeName: String
  ): KafkaMessageStreamsTopology[K1, V1, K2, V22] =
    new KafkaMessageStreamsTopology[K1, V1, K2, V22](builder, source, sink.flatTransformValues(transformer, storeName))

  def tap(f: (K2, V2) => Unit): KafkaMessageStreamsTopology[K1, V1, K2, V2] =
    new KafkaMessageStreamsTopology[K1, V1, K2, V2](builder, source, sink.peek(f))

  def to(outputTopic: String)(
      implicit outputSerdes: Produced[K2, V2]
  ): KafkaMessageStreamsTopology[K1, V1, K1, V1] = {
    sink.to(outputTopic)
    new KafkaMessageStreamsTopology[K1, V1, K1, V1](builder, source, source)
  }

  def make[F[_]: Async](implicit config: KafkaConfig): Resource[F, KafkaMessageStreams[F]] = {
    val props                = KafkaMessageStreams.props(config)
    val stream: KafkaStreams = new KafkaStreams(builder.build(), props)
    stream.setUncaughtExceptionHandler((_, e) => logger.error(s"error during stream operation: ${e.getMessage}", e))
    Resource.make(Sync[F].delay(new KafkaMessageStreams[F](stream)))(_.stop())
  }
}

object KafkaMessageStreams {

  private[kafka] def props(config: KafkaConfig): Properties = {
    val p = new Properties()
    p.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.servers)
    p.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, config.appId)
    p.setProperty(StreamsConfig.consumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), config.offset)
    p.setProperty(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE)
    p.setProperty(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")
    p.setProperty(StreamsConfig.REQUEST_TIMEOUT_MS_CONFIG, "305000")
    p.setProperty(StreamsConfig.producerPrefix(ProducerConfig.RETRIES_CONFIG), Int.MaxValue.toString)
    p.setProperty(StreamsConfig.producerPrefix(ProducerConfig.MAX_BLOCK_MS_CONFIG), Int.MaxValue.toString)
    p.setProperty(StreamsConfig.consumerPrefix(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG), Int.MaxValue.toString)
    p.setProperty(
      StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
      classOf[LogAndContinueExceptionHandler].getName
    )
    p.setProperty(
      StreamsConfig.DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG,
      classOf[DefaultProductionExceptionHandler].getName
    )
    p
  }

  def from[K, V](inputTopic: String)(
      implicit inputSerdes: Consumed[K, V]
  ): KafkaMessageStreamsTopology[K, V, K, V] = {
    val builder: StreamsBuilder = new StreamsBuilder
    val source: KStream[K, V]   = builder.stream[K, V](inputTopic)
    new KafkaMessageStreamsTopology[K, V, K, V](builder, source, source)
  }
}
