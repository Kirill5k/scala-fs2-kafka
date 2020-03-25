package io.kirill.redis

import cats.implicits._
import cats.effect.{ConcurrentEffect, ContextShift, Resource}
import dev.profunktor.redis4cats.algebra.StringCommands
import dev.profunktor.redis4cats.connection.{RedisClient, RedisURI}
import dev.profunktor.redis4cats.domain.RedisCodec
import dev.profunktor.redis4cats.log4cats._
import io.chrisdavenport.log4cats.Logger
import dev.profunktor.redis4cats.interpreter.Redis
import io.kirill.configs.RedisConfig

class RedisStringsClient[F[_]: ConcurrentEffect: ContextShift: Logger] private (config: RedisConfig) {

  private val stringCodec: RedisCodec[String, String] = RedisCodec.Utf8

  private val commandsApi: Resource[F, StringCommands[F, String, String]] =
    for {
      uri    <- Resource.liftF(RedisURI.make[F](s"redis://${config.host}:${config.port}"))
      client <- RedisClient[F](uri)
      redis  <- Redis[F, String, String](client, stringCodec)
    } yield redis


  def put(key: String, value: String): F[Unit] =
    commandsApi.use { cmd =>
      for {
        _ <- cmd.set(key, value)
      } yield ()
    }

  def get(key: String): F[Option[String]] =
    commandsApi.use { cmd =>
      for {
        value <- cmd.get(key)
      } yield value
    }
}

object RedisStringsClient {
  def apply[F[_]: ConcurrentEffect: ContextShift: Logger](config: RedisConfig): RedisStringsClient[F] =
    new RedisStringsClient[F](config)
}
