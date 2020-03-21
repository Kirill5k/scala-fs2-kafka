package io.kirill

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import fs2.kafka._
import scala.concurrent.duration._

object ConsumerApp extends IOApp {

  val consumerSettings =
    ConsumerSettings(keyDeserializer = Deserializer[IO, String], valueDeserializer = Deserializer[IO, String])
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("localhost:29092")
      .withGroupId("group")

  def processRecord(record: ConsumerRecord[String, String]): IO[Unit] =
    IO(println(s"Processing record: ${record.key} -> ${record.value}"))

  override def run(args: List[String]): IO[ExitCode] = {
    simpleConsumer("topic.test").compile.drain.as(ExitCode.Success)
  }

  def simpleConsumer(topic: String): fs2.Stream[IO, Unit] =
    consumerStream(consumerSettings)
      .evalTap(_.subscribeTo(topic))
      .flatMap(_.stream)
      .evalMap(commitable => processRecord(commitable.record))

  /**
   * Separates records per topic-partition
   * The partitionStream in the example below is a Stream of records for a single topic-partition.
   * Runs processRecord on every record, one-at-a-time in-order per topic-partition
   */
  def partitionedStream(topic: String): fs2.Stream[IO, Unit] =
    consumerStream(consumerSettings)
      .evalTap(_.subscribeTo(topic))
      .flatMap(_.partitionedStream)
      .map { partitionStream => partitionStream.evalMap(committable =>processRecord(committable.record)) }
      .parJoinUnbounded

  /**
   * Use when processing of records is independent of each other
   */
  def asyncProcessingConsumer(topic: String): fs2.Stream[IO, Unit] =
    consumerStream(consumerSettings)
      .evalTap(_.subscribeTo(topic))
      .flatMap(_.stream)
      .mapAsync(25)(committable => processRecord(committable.record))
}
