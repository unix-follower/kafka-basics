package com.example.messenger.config;

public record KafkaConfigProperties(
    String messengerTopic,
    String nonTransactionalClientId
) {
}
