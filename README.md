# JOOQ / Gradle / Liquibase
Sample project using these 3 technologies to run jooq code gen.

Uses embedded postgres (not ideal) rather than a docker container to run a local database. 

## To run
```
# Start Postgres
./gradlew startPostgres
# Update the schema using liquibase
./gradlew update
# Run tests (which executes the task generateJooq) 
./gradlew build  
# Stop the DB (won't clean the data in /tmp)
./gradlew stopPostgres
```

## Jooq Examples
Check the tests for examples of how to use Jooq.