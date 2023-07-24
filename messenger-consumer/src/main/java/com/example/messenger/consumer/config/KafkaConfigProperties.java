package com.example.messenger.consumer.config;

import java.time.Duration;

public record KafkaConfigProperties(
    String messengerTopic,
    int errorHandlerAttempts,

    Duration errorHandlerInterval
) {
  public static final String LISTENER_TYPE = "spring.kafka.listener.type";

  public static final String MESSENGER_TOPIC = "app.kafka.messenger-topic";
}
