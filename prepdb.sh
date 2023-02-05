#!/usr/bin/env bash
set -ex

./gradlew stopPostgres startPostgres update
cat src/main/resources/sakila/postgres-sakila-insert-data.sql |  psql -h localhost -p 5432 -U postgres jooqliquibase