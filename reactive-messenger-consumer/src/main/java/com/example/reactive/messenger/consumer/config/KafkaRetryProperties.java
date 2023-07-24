package com.example.reactive.messenger.consumer.config;

public record KafkaRetryProperties(String strategy) {
  public static final String SPRING_RETRY_TEMPLATE = "spring-retry-template";
}
