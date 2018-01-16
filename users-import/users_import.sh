#!/bin/bash

# Create Kafka topic for users data
docker run --rm -it --net=host landoop/fast-data-dev kafka-topics --create --topic raw-avro-users --partitions 1 --replication-factor 1 --zookeeper 127.0.0.1:2181

# Create converter
curl -s -X POST -H "Content-Type: application/json" --data '@users-import.json' http://127.0.0.1:8083/connectors