package io.kirill.kafka.serialization

import io.confluent.kafka.serializers.{KafkaAvroDeserializer, KafkaAvroSerializer}
import io.kirill.kafka.serialization.avro.SchemaRegistryUrl
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}

import scala.jdk.CollectionConverters._

object avro extends AvroSerialization {

  final case class SchemaRegistryUrl(value: String) extends AnyVal
}

trait AvroSerialization {

  private def avroSerdesConf(implicit config: SchemaRegistryUrl) = Map(
    "schema.registry.url"  -> config.value,
    "specific.avro.reader" -> java.lang.Boolean.TRUE
  )

  implicit def specificRecordSerializer[T <: SpecificRecord](implicit config: SchemaRegistryUrl): Serializer[T] =
    new Serializer[T] {
      private val avroSerializer = new KafkaAvroSerializer
      avroSerializer.configure(avroSerdesConf.asJava, false)

      override def serialize(topic: String, data: T): Array[Byte] =
        avroSerializer.serialize(topic, data)
    }

  implicit def specificRecordDeserializer[T <: SpecificRecord](implicit config: SchemaRegistryUrl): Deserializer[T] =
    new Deserializer[T] {
      private val avroDeserializer = new KafkaAvroDeserializer()
      avroDeserializer.configure(avroSerdesConf.asJava, false)

      override def deserialize(topic: String, data: Array[Byte]): T =
        avroDeserializer.deserialize(topic, data).asInstanceOf[T]
    }

  implicit def specificRecordSerde[T <: SpecificRecord](implicit ser: Serializer[T], des: Deserializer[T]): Serde[T] =
    new Serde[T] {
      override def serializer(): Serializer[T]     = ser
      override def deserializer(): Deserializer[T] = des
    }
}
