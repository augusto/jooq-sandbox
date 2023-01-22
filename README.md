# JOOQ / Gradle / Liquibase
Sample project using these 3 technologies to run jooq code gen.

Uses embeded postgres (not ideal) rather than a docker container to run a local database. 

## To run
```
# Start Postgres
./gradlew startPostgres
# Update the schema using liquibase
./gradlew update
# Run application (which also runs jooq code generation)
./gradlew run
# Stop the DB (won't clean the data in /tmp)
./gradlew stopPostgres
```

This launches an http4k app listening on port 9000.

The app has 5 endpoints
1. POST http://localhost:9000/customers : creates a new customer.
3. GET http://localhost:9000/customers : gets all customers.
3. GET http://localhost:9000/customers/{customerId} : gets one customers.
4. PUT http://localhost:9000/customers/{customerId} : updates an existing customer.
5. DELETE http://localhost:9000/customers/{customerId} : deletes an existing customer.

The customer resource is
```
{
  "id": <Int>, 
  "firstName": <String>, 
  "lastName": <String>, 
  "active": <Boolean>
}
```

## Example requests

### POST
```
curl -v -X POST \
     -H "Content-Type: application/json" \
     -d '{"id": "10", "firstName": "Jane", "lastName":"Blogs", "active": true}' \
     http://localhost:9000/customers
```

### GET all
```
curl -v \
     http://localhost:9000/customers
```

### GET one
```
curl -v \
     http://localhost:9000/customers/10
```

### PUT
The payload doesn't contain the customer id.
```
curl -v -X PUT \
     -H "Content-Type: application/json" \
     -d '{"firstName": "newFirstName", "lastName":"newLastName", "active": false}' \
     http://localhost:9000/customers/10
```

### DELETE
```
curl -v -X DELETE \
     http://localhost:9000/customers/10
```
