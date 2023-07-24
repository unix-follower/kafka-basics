package com.example.reactive.messenger.consumer.component;

import com.example.messenger.queque.api.model.MessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class KafkaListenerManager {
  @Autowired
  private ReactiveKafkaConsumerTemplate<String, MessageDto> kafkaConsumerTemplate;

  @Autowired
  private KafkaConsumer kafkaConsumer;

  private final Lock lock = new ReentrantLock();
  private Disposable disposableConsumer;

  @EventListener({
      ApplicationStartedEvent.class,
      ConsumerRestartEvent.class
  })
  public Mono<Void> start() {
    if (lock.tryLock()) {
      return Mono.fromRunnable(() -> {
            if (disposableConsumer == null || disposableConsumer.isDisposed()) {
              disposableConsumer = kafkaConsumer.listen();
            }
          })
          .doFinally(signalType -> lock.unlock())
          .then();
    } else {
      return Mono.empty();
    }
  }

  public Mono<Void> stop() {
    if (lock.tryLock()) {
      return Mono.fromRunnable(() -> disposableConsumer.dispose())
          .doFinally(signalType -> lock.unlock())
          .then();
    } else {
      return Mono.empty();
    }
  }

  public Mono<Void> pause() {
    return kafkaConsumerTemplate.assignment()
        .flatMap(kafkaConsumerTemplate::pause)
        .then();
  }

  public Mono<Void> resume() {
    return kafkaConsumerTemplate.assignment()
        .flatMap(kafkaConsumerTemplate::resume)
        .then();
  }
}
