package io.kirill.health

import cats.effect.IO
import io.kirill.kafka.KafkaMessageStreams
import org.apache.kafka.streams.KafkaStreams.State
import org.mockito.scalatest.MockitoSugar
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class HealthCheckServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar {

  "A HealthCheckService" should {
    "return status of kafka streams" in {
      val stream = mock[KafkaMessageStreams[IO]]
      when(stream.state()).thenReturn(IO.pure(State.RUNNING))

      val result = for {
        service <- HealthCheckService.make(stream)
        status <- service.status
      } yield status

      result.unsafeToFuture().map { s =>
        s must be (AppStatus(StreamStatus(true)))
      }
    }
  }
}
