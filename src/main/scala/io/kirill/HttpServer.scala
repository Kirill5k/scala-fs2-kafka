package io.kirill

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, Fiber, Resource, Sync, Timer}
import cats.effect.implicits._
import cats.implicits._
import io.kirill.configs.ServerConfig
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware._

final class HttpServer[F[_]: ConcurrentEffect: Timer: ContextShift](
    private val server: Resource[F, Server[F]]
) extends Http4sDsl[F] {

  def start(): F[Fiber[F, ExitCode]] =
    server
      .use(_ => ConcurrentEffect[F].never[Server[F]])
      .as(ExitCode.Success)
      .start
}
object HttpServer {

  def make[F[_]: ConcurrentEffect: Timer: ContextShift](
      config: ServerConfig,
      routes: HttpRoutes[F]
  ): F[HttpServer[F]] = {
    val server = BlazeServerBuilder[F]
      .bindHttp(config.port, config.host)
      .withHttpApp(CORS(routes).orNotFound)
      .resource

    Sync[F].delay(new HttpServer[F](server))
  }
}
