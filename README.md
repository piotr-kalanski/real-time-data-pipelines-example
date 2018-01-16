# real-time-data-pipelines-example
Example real time data pipelines using Kafka Connect and Kafka Streams

# Data flow diagram

![](images/real_data_pipeline_example_diagram.png)

# Run instruction

## Run docker containers

    docker-compose up kafka-cluster elasticsearch kibana postgres

## Run generators

### clickstream-generator

Run main from class [ClickstreamGenerator](clickstream-generator/src/main/java/com/datawizards/generator/ClickstreamGenerator.java).

### users-generator

Run main from class [UsersGenerator](users-generator/src/main/java/com/datawizards/generator/UsersGenerator.java).

## Run import connectors

### clickstream-import

Run script [clickstream_import.sh](clickstream-import/clickstream_import.sh).

### users-import

Run script [users_import.sh](users-import/users_import.sh).

## Run stream application

Run main from class [ApplicationMain](user-profile-stream/src/main/java/com/github/piotrkalanski/ApplicationMain.java).

## Run export connectors

### user-profile-export

Run script [user_profile_export_es_avro.sh](user-profile-es-avro/user_profile_export_es_avro.sh)
