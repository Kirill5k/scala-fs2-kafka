#!/usr/bin/env bash

docker run -it --rm --net=host confluentinc/cp-schema-registry:3.1.1 bash

kafka-avro-console-producer \
    --broker-list localhost:29092 \
    --topic test-avro \
    --property schema.registry.url=http://127.0.0.1:8081 \
    --property value.schema='{"type": "record", "name": "testRecord", "fields": [{"name": "foo", type: "string"}, {"name": "bar", type: "string"}]}'

kafka-avro-console-consumer \
    --bootstrap-server localhost:29092 \
    --topic test-avro \
    --property schema.registry.url=http://127.0.0.1:8081 \
    --from-beginning
