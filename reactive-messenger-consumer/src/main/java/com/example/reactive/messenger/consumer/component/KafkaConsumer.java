package com.example.reactive.messenger.consumer.component;

import reactor.core.Disposable;

public interface KafkaConsumer {
  Disposable listen();
}
