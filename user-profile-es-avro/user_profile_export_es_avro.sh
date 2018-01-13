#!/bin/bash

# Create Kafka topic for users profiles data
docker run --rm --net=host landoop/fast-data-dev kafka-topics --create --topic user-profile --partitions 1 --replication-factor 1 --zookeeper 127.0.0.1:2181

# Create Elasticsearch index mapping
curl -s -X PUT -H "Content-Type: application/json" --data '@es_index_mapping.json' http://localhost:9200/user-profile-avro

# Create converter
curl -s -X POST -H "Content-Type: application/json" --data '@user-profile-export-avro.json' http://127.0.0.1:8083/connectors