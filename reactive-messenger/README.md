### Request examples
#### Define variable
```shell
port=8081
```
#### Transactional message production
```shell
curl -v "localhost:${port}/api/v1/messenger" \
  --header 'Content-Type: application/json' \
  --header 'Scenario: produceWithTxManager' \
  --data-raw '{
    "channelId": "8aabe24e-f1e9-4d6a-a795-e2d991b1002e",
    "userId": "ef5ef8d3-082c-4459-ada7-f9f207674c3f",
    "message": "Hello, world"
  }'

# This request should fail with 500 status code
curl -v "localhost:${port}/api/v1/messenger" \
  --header 'Content-Type: application/json' \
  --header 'Scenario: produceWithRegularSend' \
  --data-raw '{
    "channelId": "8aabe24e-f1e9-4d6a-a795-e2d991b1002e",
    "userId": "ef5ef8d3-082c-4459-ada7-f9f207674c3f",
    "message": "Hello, world"
  }'

# This request should fail with 500 status code
curl -v "localhost:${port}/api/v1/messenger" \
  --header 'Content-Type: application/json' \
  --header 'Scenario: produceWithTxManager' \
  --data-raw '{
    "channelId": "8aabe24e-f1e9-4d6a-a795-e2d991b1002e",
    "userId": "ef5ef8d3-082c-4459-ada7-f9f207674c3f",
    "messageContentType": "application/json",
    "message": "{\"message\": \"You have been hacked!\", \"executeLogicBomb\": true}"
  }'

# This request should fail with 500 status code
curl -v "localhost:${port}/api/v1/messenger" \
  --header 'Content-Type: application/json' \
  --header 'Scenario: produceWithRegularSend' \
  --data-raw '{
    "channelId": "8aabe24e-f1e9-4d6a-a795-e2d991b1002e",
    "userId": "ef5ef8d3-082c-4459-ada7-f9f207674c3f",
    "message": "Hello, world"
  }'

curl -v "localhost:${port}/api/v1/messenger" \
  --header 'Content-Type: application/json' \
  --header 'Scenario: produceAsyncWithMonoCompletableFuture' \
  --data-raw '{
    "channelId": "8aabe24e-f1e9-4d6a-a795-e2d991b1002e",
    "userId": "ef5ef8d3-082c-4459-ada7-f9f207674c3f",
    "message": "Hello, world"
  }'

# Returns 200 OK, but rolls back db transaction
curl -v "localhost:${port}/api/v1/messenger" \
  --header 'Content-Type: application/json' \
  --header 'Scenario: produceAsyncWithCompletableFuture' \
  --data-raw '{
    "channelId": "8aabe24e-f1e9-4d6a-a795-e2d991b1002e",
    "userId": "ef5ef8d3-082c-4459-ada7-f9f207674c3f",
    "message": "Hello, world"
  }'
```
