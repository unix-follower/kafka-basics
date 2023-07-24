### Request examples
#### Define variable
```shell
port=8081
```
#### Non-transactional message production
```shell
curl -v "localhost:${port}/api/v1/messenger" \
  --header 'Content-Type: application/json' \
  --header 'Scenario: nonTransactionalProduce' \
  --data-raw '{
    "channelId": "8aabe24e-f1e9-4d6a-a795-e2d991b1002e",
    "userId": "ef5ef8d3-082c-4459-ada7-f9f207674c3f",
    "message": "Hello, world"
  }'
```
Non-transactional message production with metadata. 
```shell
timestamp=$(date +%s)
curl -v -X POST "localhost:${port}/api/v1/messenger" \
  --header 'Content-Type: application/json' \
  --header 'Scenario: nonTransactionalProduce' \
  --data-raw "{
    \"channelId\": \"8aabe24e-f1e9-4d6a-a795-e2d991b1002e\",
    \"userId\": \"ef5ef8d3-082c-4459-ada7-f9f207674c3f\",
    \"message\": \"Hello, world\",
    \"metadata\": {
      \"partition\": 0,
      \"key\": \"test\",
      \"timestamp\": $timestamp,
      \"headers\": {
        \"sampleHeader\": \"value\"
      }
    }
  }"
```
---
#### Transactional message production
```shell
curl -v "localhost:${port}/api/v1/messenger" \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "channelId": "8aabe24e-f1e9-4d6a-a795-e2d991b1002e",
    "userId": "ef5ef8d3-082c-4459-ada7-f9f207674c3f",
    "message": "Hello, world"
  }'
```
##### Scenario: produceWithNoDbTransactionInProgressAndManualTxMgmt
```shell
curl -v "localhost:${port}/api/v1/messenger" \
  --header 'Content-Type: application/json' \
  --header 'Scenario: produceWithNoDbTransactionInProgressAndManualTxMgmt' \
  --data-raw '{
    "channelId": "8aabe24e-f1e9-4d6a-a795-e2d991b1002e",
    "userId": "ef5ef8d3-082c-4459-ada7-f9f207674c3f",
    "message": "Hello, world"
  }'
```
Messages that break producer.

This request should fail with 500 status code.
```shell
curl -v "localhost:${port}/api/v1/messenger" \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "channelId": "8aabe24e-f1e9-4d6a-a795-e2d991b1002e",
    "userId": "ef5ef8d3-082c-4459-ada7-f9f207674c3f",
    "messageContentType": "application/json",
    "message": "{\"message\": \"You have been hacked!\", \"executeLogicBomb\": true}"
  }'
```
###### Scenario: produceAsync
This request should fail with 500 status code.
```shell
curl -v "localhost:${port}/api/v1/messenger" \
  --header 'Content-Type: application/json' \
  --header 'Scenario: produceAsync' \
  --data-raw '{
    "channelId": "8aabe24e-f1e9-4d6a-a795-e2d991b1002e",
    "userId": "ef5ef8d3-082c-4459-ada7-f9f207674c3f",
    "message": "Hello, world"
  }'
```
###### Scenario: produceWithNoDbTransactionInProgress
This request should fail with 500 status code.
```shell
curl -v "localhost:${port}/api/v1/messenger" \
  --header 'Content-Type: application/json' \
  --header 'Scenario: produceWithNoDbTransactionInProgress' \
  --data-raw '{
    "channelId": "8aabe24e-f1e9-4d6a-a795-e2d991b1002e",
    "userId": "ef5ef8d3-082c-4459-ada7-f9f207674c3f",
    "message": "Hello, world"
  }'
```
##### Messages that break consumer
Fail on the first read and on 1 subsequent retry.  
```shell
curl -v "localhost:${port}/api/v1/messenger" \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "channelId": "8aabe24e-f1e9-4d6a-a795-e2d991b1002e",
    "userId": "ef5ef8d3-082c-4459-ada7-f9f207674c3f",
    "messageContentType": "application/json",
    "message": "{\"message\": \"You have been hacked!\", \"consumer\": {\"failConsumptionTimes\": 2}}"
  }'
```
Fail on first read and on 2 subsequent retries.
```shell
curl -v "localhost:${port}/api/v1/messenger" \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "channelId": "8aabe24e-f1e9-4d6a-a795-e2d991b1002e",
    "userId": "ef5ef8d3-082c-4459-ada7-f9f207674c3f",
    "messageContentType": "application/json",
    "message": "{\"message\": \"You have been hacked!\", \"consumer\": {\"failConsumptionTimes\": 3}}"
  }'
```
Consumer always fails to read the message.  
```shell
curl -v "localhost:${port}/api/v1/messenger" \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "channelId": "8aabe24e-f1e9-4d6a-a795-e2d991b1002e",
    "userId": "ef5ef8d3-082c-4459-ada7-f9f207674c3f",
    "messageContentType": "application/json",
    "message": "{\"message\": \"You have been hacked!\", \"consumer\": {\"executeLogicBomb\": true}}"
  }'
```
Simulate processing delay of 30 seconds.
```shell
curl -v "localhost:${port}/api/v1/messenger" \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "channelId": "8aabe24e-f1e9-4d6a-a795-e2d991b1002e",
    "userId": "ef5ef8d3-082c-4459-ada7-f9f207674c3f",
    "messageContentType": "application/json",
    "message": "{\"message\": \"delay of 30 seconds\", \"consumer\": {\"processingDelay\": 30000}}"
  }'
```
