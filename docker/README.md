### Prerequisite
#### Create docker network
```shell script
docker network create \
  --driver=bridge \
  --subnet=192.168.9.0/24 \
  --gateway=192.168.9.254 \
  kafka-basics-bridge
```
### Start container
```shell
docker-compose -f kafka/docker-compose.yml up -d
docker-compose -f postgres/docker-compose.yml up -d
```
### Useful Docker commands
```shell
docker logs zookeeper
docker exec -it zookeeper /bin/bash

docker logs kafka
docker exec -it kafka /bin/bash
```
### Useful commands inside Kafka Docker container
```shell
printenv
ps aux | grep java
whereis java
ls -l /etc/kafka/
ls -l /var/lib/kafka/data/
ls -l /usr/bin/kafka-*
ls -l /usr/share/java/kafka/

cat /etc/kafka/server.properties
cat /etc/kafka/zookeeper.properties
cat /etc/kafka/kafka.properties
```
