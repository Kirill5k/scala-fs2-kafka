package io.kirill.configs

import cats.effect.{Blocker, ContextShift, IO}
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

import scala.concurrent.ExecutionContext

class AppConfigSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

  "A MainConfig" - {
    "should be parsed from application.conf" in {
      val config = Blocker[IO].use(b => AppConfig.load(b))

      config.asserting(_ mustBe a [AppConfig])
    }
  }
}
