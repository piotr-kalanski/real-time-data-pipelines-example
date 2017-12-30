#!/bin/bash

docker run --rm -it --net=host landoop/fast-data-dev kafka-topics --create --topic raw-clickstream --partitions 1 --replication-factor 1 --zookeeper 127.0.0.1:2181