package io.kirill.health

import cats.effect.Sync
import cats.implicits._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes, Status}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class HealthCheckController[F[_]: Sync](
    healthCheckService: HealthCheckService[F]
) extends Http4sDsl[F] {
  import HealthCheckController._

  private val prefixPath = "/health"

  private val httpRoutes: HttpRoutes[F] =
    HttpRoutes.of {
      case GET -> Root / "status" =>
        for {
          health <- healthCheckService.status
          responseBody = HealthCheckResponse.from(health)
          res <- if (health.isUp) Ok(responseBody.asJson) else InternalServerError(responseBody.asJson)
        } yield res
    }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}

object HealthCheckController {
  implicit def deriveEntityEncoder[F[_]: Sync, A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]
  implicit def deriveEntityDecoder[F[_]: Sync, A: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]

  final case class HealthCheckResponse(stream: String)

  object HealthCheckResponse {
    def from(appStatus: AppStatus): HealthCheckResponse =
      HealthCheckResponse(
        if (appStatus.kafkaStreams.value) "up" else "down"
      )
  }

  def make[F[_]: Sync](
      hcs: HealthCheckService[F]
  ): F[HealthCheckController[F]] =
    Sync[F].delay(new HealthCheckController[F](hcs))
}
