# real-time-data-pipelines-example
Example real time data pipelines using Kafka Connect and Kafka Streams

# Run instruction

## Run docker containers

    docker-compose up kafka-cluster elasticsearch kibana postgres

## Run generators

### clickstream-generator

Run main from class ``ClickstreamGenerator``.

## Run import connectors

### clickstream-import

Run script: ```clickstream_import.sh```

## Run stream application

Run main from class ```ApplicationMain``` in project [user-profile-stream-json](user-profile-stream-json/src/main/java/com/github/piotrkalanski/ApplicationMain.java).

## Run export connectors

### user-profile-export

Run script: ```user_profile_export_es_json.sh```

