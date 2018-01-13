#!/bin/bash

# Create Kafka topic for clickstream data
docker run --rm --net=host landoop/fast-data-dev kafka-topics --create --topic raw-avro-clickstream --partitions 1 --replication-factor 1 --zookeeper 127.0.0.1:2181

# Create converter
curl -s -X POST -H "Content-Type: application/json" --data '@clickstream-import.json' http://127.0.0.1:8083/connectors