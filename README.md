# JOOQ / Gradle / Liquibase
Sample project using these 3 technologies to run jooq code gen.

Uses embedded postgres (not ideal) rather than a docker container to run a local database. 

## To run
```
# Start Postgres
./gradlew startPostgres
# Runs liquibase, jooq and tests 
./gradlew build  
# Stop the DB - will run `killall postgres`! 
./gradlew stopPostgres
```

## Jooq Examples
There are 3 modules with different configurations
* activerecord: Has active record and mutable pojos enabled.
* record: Has active record and mutable pojos disabled.
* sandbox: Extra module to try configurations and settings - play with it!