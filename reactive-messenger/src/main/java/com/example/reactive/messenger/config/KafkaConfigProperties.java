package com.example.reactive.messenger.config;

public record KafkaConfigProperties(
    String messengerTopic,
    String nonTransactionalClientId
) {
}
