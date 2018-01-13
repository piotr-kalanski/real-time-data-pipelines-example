#!/bin/bash

# Run infrastructure services: Kafka, Zookeeper, PostgreSQL, Elasticsearch, Kibana
docker-compose -f infrastructure-docker-compose.yml up -d

# Build applications
mvn clean package

# Deploy Kafka Connect connectors and create Kafka topics
bash ./clickstream-import/clickstream_import.sh
bash ./users-import/users_import.sh
bash ./listings-import/listings_import.sh
bash ./user-profile-es-avro/user_profile_export_es_avro.sh

# Run Java applications: data generators and Kafka Streams
docker-compose -f applications-docker-compose.yml up -d