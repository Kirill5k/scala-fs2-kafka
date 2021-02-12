package io.kirill.kafka.serialization

import org.apache.kafka.common.serialization.{Deserializer, Serializer, StringDeserializer, StringSerializer}

object string extends StringSerialization

trait StringSerialization {
  implicit val stringDeserializer: Deserializer[String] = new StringDeserializer()
  implicit val stringSerializer: Serializer[String]     = new StringSerializer()
}
