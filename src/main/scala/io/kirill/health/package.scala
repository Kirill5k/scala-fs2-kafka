package io.kirill

import cats.effect.Sync
import cats.implicits._
import io.kirill.kafka.KafkaMessageStreams

package object health {

  final case class StreamStatus(value: Boolean) extends AnyVal

  final case class AppStatus(
      kafkaStreams: StreamStatus
  ) {
    def isUp: Boolean = kafkaStreams.value
  }

  final class Health[F[_]: Sync](
      val healthCheckController: HealthCheckController[F]
  )

  object Health {
    def make[F[_]: Sync](stream: KafkaMessageStreams[F]): F[Health[F]] =
      for {
        service    <- HealthCheckService.make(stream)
        controller <- HealthCheckController.make(service)
      } yield new Health[F](controller)
  }
}
