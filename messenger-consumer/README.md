## Consumer REST API
```shell
port=8080
curl "localhost:${port}/api/v1/admin/listener"
listenerId='retryableListener'
listenerId='retryableListener-retry-10000'
listenerId='retryableListener-retry-20000'
listenerId='retryableListener-dlt'
curl -X POST "localhost:8080/api/v1/admin/listener/${listenerId}/start"
curl -X POST "localhost:8080/api/v1/admin/listener/${listenerId}/stop"
curl -X POST "localhost:8080/api/v1/admin/listener/${listenerId}/pause"
curl -X POST "localhost:8080/api/v1/admin/listener/${listenerId}/resume"
```
## Kafka shell commands
### Topics
#### Describe
```shell
./kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic messenger
```
#### Delete
```shell
./kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic messenger-dlt 
./kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic messenger-retry-0
```
### Consumer commands
#### Console consumer
```shell
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic messenger --isolation-level read_committed
```
##### Dead letter topic (DLT)
```shell
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --group console --topic messenger-dlt --from-beginning
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --group console --topic messenger-retry-0 --from-beginning
```
#### Consumer groups
#### List
```shell
./kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
```
#### Consumer group offsets
```shell
./kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe \
  --group messenger-consumer-group \
  --verbose \
  --offsets
```
