# real-time-data-pipelines-example
Example real time data pipelines using Kafka Connect and Kafka Streams

# Data flow diagram

![](images/real_data_pipeline_example_diagram.png)

# Run instruction

## Build Java applications

    mvn package

## Run docker containers

    docker-compose up

This will run all services:
- Kafka cluster with related services (Zookeeper, Avro schema registry, landoop UI)
- Elasticsearch
- Kibana
- Postgres
- All Java generators

## Run import connectors

### clickstream-import

Run script [clickstream_import.sh](clickstream-import/clickstream_import.sh).

### users-import

Run script [users_import.sh](users-import/users_import.sh).

### listings-import

Run script [listings_import.sh](listings-import/listings_import.sh).

## Run stream application

Run main from class [ApplicationMain](user-profile-stream/src/main/java/com/github/piotrkalanski/ApplicationMain.java).

## Run export connectors

### user-profile-export

Run script [user_profile_export_es_avro.sh](user-profile-es-avro/user_profile_export_es_avro.sh)
