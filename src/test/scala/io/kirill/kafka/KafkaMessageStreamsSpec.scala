package io.kirill.kafka

import cats.effect.IO
import org.apache.kafka.streams.KafkaStreams
import org.mockito.scalatest.AsyncMockitoSugar
import org.scalatest.freespec.{AnyFreeSpec, AsyncFreeSpec}
import org.scalatest.matchers.must.Matchers

class KafkaMessageStreamsSpec extends AsyncFreeSpec with Matchers with AsyncMockitoSugar {

  "A KafkaMessageStreams" - {

    "close" - {
      "should close if stream is running" in {
        val kafkaStreams = mock[KafkaStreams]
        val streams = new KafkaMessageStreams[IO](kafkaStreams)

        when(kafkaStreams.state()).thenReturn(KafkaStreams.State.RUNNING)

        streams.close().unsafeToFuture().map { res =>
          verify(kafkaStreams).close()
          res must be (())
        }
      }

      "should not do anything if stream is not running" in {
        val kafkaStreams = mock[KafkaStreams]
        val streams = new KafkaMessageStreams[IO](kafkaStreams)

        when(kafkaStreams.state()).thenReturn(KafkaStreams.State.REBALANCING)

        streams.close().unsafeToFuture().map { res =>
          verify(kafkaStreams, never).close()
          res must be (())
        }
      }
    }
  }
}
