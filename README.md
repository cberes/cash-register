# cash-register

A cash register web service.

## Examples

### Get items

    curl localhost:8080/items

### New order

    ORDER_ID=$(curl -XPUT localhost:8080/orders)

### Add new item to an order

    curl -XPOST localhost:8080/orders/$ORDER_ID -d'{"item_id":1}' -H"Content-Type: application/json"

### Update quantity of an item

    curl -XPUT localhost:8080/orders/$ORDER_ID -d'{"item_id":1,"amount":2}' -H"Content-Type: application/json"

### Delete an item from an order

    curl -XDELETE localhost:8080/orders/$ORDER_ID -d'{"item_id":1}' -H"Content-Type: application/json"

### Submit order

    curl -XPOST localhost:8080/orders/$ORDER_ID/submit

### Tender paymanet

    curl -XPOST localhost:8080/tender -d'{"order_id":"'$ORDER_ID'","amount":1899,"method":"CASH"}' -H"Content-Type: application/json"

## Configuration

0. Couchbase URL
0. Tax?

## Dependencies

### Couchbase

To run a Couchbase server in a Docker container:

    docker pull couchbase/server
    docker run -d --name db -p 8091-8094:8091-8094 -p 11210:11210 couchbase

The server will be accessible via HTTP at `http://localhost:8091` or from the console via `docker exec -it db sh`.