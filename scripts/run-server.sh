#!/bin/bash

./scripts/build.sh

. .env

cd server
    mvn exec:java -Dexec.args="127.0.0.1 10000" \
        -Dhibernate_username=$POSTGRESQL_USERNAME \
        -Dhibernate_password=$POSTGRESQL_PASSWORD \
        -Dhibernate_db_port=$POSTGRESQL_PORT
cd ..
