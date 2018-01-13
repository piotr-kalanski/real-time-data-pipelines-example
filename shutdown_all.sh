#!/bin/bash

# Shutdown infrastructure services: Kafka, Zookeeper, PostgreSQL, Elasticsearch, Kibana
docker-compose -f infrastructure-docker-compose.yml down

# Shutdown Java applications: data generators and Kafka Streams
docker-compose -f applications-docker-compose.yml down