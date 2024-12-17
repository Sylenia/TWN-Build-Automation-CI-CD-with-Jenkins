#!usr/bin/env bash

export IMAGE=$1
docker-compose -f docker-cpmpose.yaml up --detach
echo "success"