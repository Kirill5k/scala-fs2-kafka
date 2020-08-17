package io.kirill.health

import cats.effect.{ContextShift, IO}
import io.circe.{Encoder, Json}
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.{Request, Response, Status}
import org.mockito.scalatest.MockitoSugar
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.{AnyWordSpec}
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._

import scala.concurrent.ExecutionContext

class HealthCheckControllerSpec extends AnyWordSpec with Matchers with MockitoSugar {
  import HealthCheckController._

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

  "A HealthCheckController" should {

    "return status of the app" in {
      val healthCheckServiceMock = mock[HealthCheckService[IO]]
      val controller             = new HealthCheckController[IO](healthCheckServiceMock)

      when(healthCheckServiceMock.status).thenReturn(IO.pure(AppStatus(StreamStatus(true))))

      val request                    = Request[IO](uri = uri"/health/status")
      val response: IO[Response[IO]] = controller.routes.orNotFound.run(request)

      verifyResponse[HealthCheckResponse](response, Status.Ok, Some(HealthCheckResponse("up")))
    }
  }

  def verifyResponse[A: Encoder](actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[A] = None): Unit = {
    val actualResp = actual.unsafeRunSync

    actualResp.status must be(expectedStatus)
    expectedBody match {
      case Some(expected) => actualResp.as[Json].unsafeRunSync must be(expected.asJson)
      case None           => actualResp.body.compile.toVector.unsafeRunSync mustBe empty
    }
  }
}
