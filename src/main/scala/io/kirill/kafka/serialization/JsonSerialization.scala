package io.kirill.kafka.serialization

import io.circe._
import io.circe.parser._
import io.circe.syntax._

import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}

object json extends JsonSerialization

trait JsonSerialization extends StringSerialization {
  implicit def jsonDeserializer[T: Decoder]: Deserializer[T] = new Deserializer[T] {
    override def deserialize(topic: String, data: Array[Byte]): T =
      decode[T](stringDeserializer.deserialize(topic, data)).getOrElse[T](null.asInstanceOf[T])
  }

  implicit def jsonSerializer[T: Encoder]: Serializer[T] = new Serializer[T] {
    override def serialize(topic: String, data: T): Array[Byte] =
      stringSerializer.serialize(topic, data.asJson.noSpaces)
  }

  implicit def jsonSerdes[T: Encoder: Decoder](implicit ser: Serializer[T], des: Deserializer[T]): Serde[T] =
    new Serde[T] {
      override def serializer(): Serializer[T]     = ser
      override def deserializer(): Deserializer[T] = des
    }
}
