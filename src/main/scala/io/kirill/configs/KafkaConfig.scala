package io.kirill.configs

final case class KafkaProducerConfig(
                                      host: String,
                                      port: Int
                                    )

final case class KafkaConsumerConfig(
                                      host: String,
                                      port: Int,
                                      groupId: String
                                    )

final case class KafkaConfig(producer: KafkaProducerConfig, consumer: KafkaConsumerConfig)

