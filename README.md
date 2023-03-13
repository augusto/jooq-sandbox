# JOOQ / Gradle / Liquibase
Sample project using these 3 technologies to run jooq code gen.

Uses embedded postgres (not ideal) rather than a docker container to run a local database. 

## To run
```
# Start Postgres.
./gradlew startPostgres
# Runs liquibase, jooq and tests. 
./gradlew build
# Stop the gradle daemon, which also stops the DB. 
./gradlew --stop
# Or run, which will run `killall postgres
./gradlew stopPostgres
```

## Jooq Examples
There are 3 modules with different configurations
* record: Examples in Kotlin using mutable records and mutable pojos.
* immutable: Examples in Kotlin with records disabled and immutable pojos.
* java: Examples in Java using mutable records and record classes. 

## Database schema
The schemas are populated with the [Sakila example DB](https://www.jooq.org/sakila) that has been tweaked to work in Postgres.
This creates some hurdles with jooq as PKs and FKs sometimes don't have the same type (e.g. integer vs smallint). 

There are some extra tables to showcase other features (e.g. optimistic locking)