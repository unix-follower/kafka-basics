package com.example.reactive.messenger.consumer.exception;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public class ConsumerRecordProcessingException extends AppException {
  private final transient ConsumerRecord<?, ?> consumerRecord;

  public ConsumerRecordProcessingException(ConsumerRecord<?, ?> consumerRecord, Throwable cause) {
    super(cause);
    this.consumerRecord = consumerRecord;
  }

  @SuppressWarnings("unchecked")
  public <K, V> ConsumerRecord<K, V> getConsumerRecord() {
    return (ConsumerRecord<K, V>) consumerRecord;
  }
}
