package com.example.reactive.messenger.consumer.config;

public record KafkaConfigProperties(
    String messengerTopic,
    KafkaRetryProperties retry
) {
}
