package io.kirill.kafka

import java.util.Properties

import cats.effect.{Async, Sync}
import cats.implicits._
import org.apache.kafka.streams.KafkaStreams.State
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.KStream

final class KafkaMessageStreams[F[_]: Async](
    private val streams: KafkaStreams
) {

  def state(): F[State] =
    Async[F].delay(streams.state())

  def start(): F[Unit] =
    Async[F].async[Unit](_ => streams.start())

  def close(): F[Unit] =
    state().flatMap {
      case State.RUNNING => Async[F].delay(streams.close())
      case _             => Async[F].unit
    }
}

final class KafkaStreamsTopology[F[_]: Sync, K, V](
    private val builder: StreamsBuilder,
    private val origin: KStream[K, V]
) {

  def filter(p: (K, V) => F[Boolean]): KafkaStreamsTopology[F, K, V] = ???
}

object KafkaMessageStreams {

  def props: Properties = {
    val p = new Properties()
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams-app")
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092")
    p
  }

}
