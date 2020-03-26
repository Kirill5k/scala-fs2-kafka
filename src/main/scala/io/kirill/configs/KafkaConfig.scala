package io.kirill.configs

final case class KafkaProducerConfig(bootstrapServers: String)

final case class KafkaConsumerConfig(
                                      bootstrapServers: String,
                                      groupId: String,
                                      autoOffsetReset: String
                                    )

final case class KafkaSchemaConfig(schemaRegistryUrl: String)

final case class KafkaConfig(producer: KafkaProducerConfig, consumer: KafkaConsumerConfig, schema: KafkaSchemaConfig)


