## Kafka shell commands
### Topics
#### List
```shell
./kafka-topics.sh --bootstrap-server localhost:9092 --list
```
#### Describe
```shell
./kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic __transaction_state
```
### Consumer commands
#### Consumer groups
#### List
```shell
./kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
```
### Transactions
#### List
```shell
./kafka-transactions.sh --bootstrap-server localhost:9092 list
```
#### Describe
```shell
./kafka-transactions.sh --bootstrap-server localhost:9092 describe --transactional-id tx-0
```
### Offsets
#### Describe
```shell
./kafka-get-offsets.sh --bootstrap-server localhost:9092
```
