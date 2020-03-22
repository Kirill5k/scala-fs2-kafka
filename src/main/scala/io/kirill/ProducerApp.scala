package io.kirill

import cats.effect._
import cats.implicits._
import fs2._
import fs2.kafka._

import scala.concurrent.duration._
import scala.language.postfixOps

object ProducerApp extends IOApp {

  val producerSettings: ProducerSettings[IO, String, String] = ProducerSettings(
    keySerializer = Serializer[IO, String],
    valueSerializer = Serializer[IO, String]
  ).withBootstrapServers("localhost:29092")

  override def run(args: List[String]): IO[ExitCode] = {
    fs2.Stream
      .iterate(1)(_ + 1)
      .covary[IO]
      .metered(1 second)
      .map(i => ProducerRecord("topic.test", "key", i.toString))
      .evalMap(rec => IO(println(s"sending rec ${rec.value} to ${rec.topic}")) >> IO(rec))
      .map(rec => ProducerRecords.one(rec))
      .through(simpleProducerStreamPipe)
      .compile.drain.as(ExitCode.Success)
  }

  def simpleProducerStreamPipe: Pipe[IO, ProducerRecords[String, String, Unit], ProducerResult[String, String, Unit]] =
    produce(producerSettings)
}
