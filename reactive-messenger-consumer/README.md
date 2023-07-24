## Consumer REST API
```shell
port=8080
curl -X POST "localhost:${port}/api/v1/admin/listener/start"
curl -X POST "localhost:${port}/api/v1/admin/listener/stop"
curl -X POST "localhost:${port}/api/v1/admin/listener/pause"
curl -X POST "localhost:${port}/api/v1/admin/listener/resume"
```
## Kafka shell commands
### Topics
#### Describe
```shell
./kafka-topics.sh --bootstrap-server localhost:9092 --list
./kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic __transaction_state
./kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic reacitve_messenger
```
#### Delete
```shell
./kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic reacitve_messenger-dlt 
./kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic reacitve_messenger-retry-0
```
### Consumer commands
#### Console consumer
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic reacitve_messenger --isolation-level read_committed
##### Dead letter topic (DLT)
```shell
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --group console --topic reacitve_messenger-dlt --from-beginning
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --group console --topic reacitve_messenger-retry-0 --from-beginning
```
#### Consumer groups
#### List
```shell
./kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
```
#### Consumer group offsets
```shell
./kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe \
  --group reactive-messenger-consumer-group \
  --verbose \
  --offsets
```
### Transactions
#### Describe
```shell
./kafka-transactions.sh --bootstrap-server localhost:9092 describe --transactional-id tx
```
### Offsets
#### Describe
```shell
./kafka-get-offsets.sh --bootstrap-server localhost:9092
```
