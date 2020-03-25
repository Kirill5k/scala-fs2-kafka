package io.kirill.redis

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits._
import com.github.sebruck.EmbeddedRedis
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.kirill.configs.RedisConfig
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

class RedisStringsClientSpec extends AsyncFreeSpec with EmbeddedRedis with AsyncIOSpec with Matchers {
  implicit val ec = scala.concurrent.ExecutionContext.global
  implicit val cs = IO.contextShift(ec)
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]


  "A RedisStringsClient" - {

    "should return empty option if key not found" in {
      withRedisAsync() { port =>
        val config = RedisConfig("localhost", port)
        val redisStringsClient = RedisStringsClient[IO](config)

        val result = redisStringsClient.get("key")
        result.asserting(_ must be (None))
      }
    }

    "should return value if key exists" in {
      withRedisAsync() { port =>
        val config = RedisConfig("localhost", port)
        val redisStringsClient = RedisStringsClient[IO](config)

        val result = redisStringsClient.put("key", "test-value") *> redisStringsClient.get("key")
        result.asserting(_ must be (Some("test-value")))
      }
    }

    "should update value and return updated" in {
      withRedisAsync() { port =>
        val config = RedisConfig("localhost", port)
        val redisStringsClient = RedisStringsClient[IO](config)

        val result = redisStringsClient.put("key", "test-value") *> redisStringsClient.put("key", "updated-test-value") *> redisStringsClient.get("key")
        result.asserting(_ must be (Some("updated-test-value")))
      }
    }
  }
}
