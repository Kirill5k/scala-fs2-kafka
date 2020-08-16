package io.kirill.health

import cats.effect.Sync
import cats.implicits._
import io.kirill.kafka.KafkaMessageStreams

sealed trait HealthCheckService[F[_]] {
  def status: F[AppStatus]
}

final private class LiveHealthCheckService[F[_]: Sync](
    stream: KafkaMessageStreams[F]
) extends HealthCheckService[F] {

  private def streamHealth: F[StreamStatus] =
    stream
      .state()
      .map(_.isRunningOrRebalancing)
      .map(StreamStatus)

  override def status: F[AppStatus] =
    streamHealth.map(AppStatus)
}

object HealthCheckService {
  def make[F[_]: Sync](stream: KafkaMessageStreams[F]): F[HealthCheckService[F]] =
    Sync[F].delay(new LiveHealthCheckService[F](stream))
}
