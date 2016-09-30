# cash-register

A cash register web service

## Examples

### Get items

    curl localhost:8080/items

### New order

    curl -XPOST localhost:8080/orders -d'{"tax":0.09}' -H"Content-Type: application/json"

### Add new item to an order

    curl -XPOST localhost:8080/orders/$ORDER_ID -d'{"item_id":1}' -H"Content-Type: application/json"

### Update quantity of an item

    curl -XPUT localhost:8080/orders/$ORDER_ID -d'{"item_id":1,"amount":2}' -H"Content-Type: application/json"

### Delete an item from an order

    curl -XDELETE localhost:8080/orders/$ORDER_ID -d'{"item_id":1}' -H"Content-Type: application/json"

### Submit order

    curl -XPOST localhost:8080/orders/$ORDER_ID/submit

### Tender payment

    curl -XPOST localhost:8080/tender -d'{"order_id":"'$ORDER_ID'","amount":1899,"method":"CASH"}' -H"Content-Type: application/json"

## Configuration

0. Couchbase connection string: `cb.conn` (default is `couchbase://localhost`)
0. Couchbase bucket name: `cb.bucket` (default is `cash-register`)
0. Couchbase password: `cb.password` (default is `cash-register-123`)
0. Maximum order number: `order.num.max` (default is 100)

Spring looks for the configuration file `application.properties` in the current directory or a `config` directory within the current directory.

## Dependencies

### Couchbase

To run a Couchbase server in a Docker container:

    docker pull couchbase/server
    docker run -d --name db -p 8091-8094:8091-8094 -p 11210:11210 couchbase

The server will be accessible via HTTP at `http://localhost:8091` or from the console via `docker exec -it db sh`.
