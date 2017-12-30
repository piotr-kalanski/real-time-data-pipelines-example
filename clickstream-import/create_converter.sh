#!/bin/bash

curl -s -X POST -H "Content-Type: application/json" --data '@clickstream-import.json' http://127.0.0.1:8083/connectors