package com.example.reactive.messenger.consumer.controller;

import com.example.reactive.messenger.consumer.api.AdminListenerApi;
import com.example.reactive.messenger.consumer.component.KafkaListenerManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class AdminListenerController implements AdminListenerApi {
  private final KafkaListenerManager kafkaListenerManager;

  public AdminListenerController(KafkaListenerManager kafkaListenerManager) {
    this.kafkaListenerManager = kafkaListenerManager;
  }

  @Override
  public Mono<ResponseEntity<Void>> startListener() {
    return kafkaListenerManager.start()
        .thenReturn(ResponseEntity.noContent().build());
  }

  @Override
  public Mono<ResponseEntity<Void>> stopListener() {
    return kafkaListenerManager.stop()
        .thenReturn(ResponseEntity.noContent().build());
  }

  @Override
  public Mono<ResponseEntity<Void>> pauseListener() {
    return kafkaListenerManager.pause()
        .thenReturn(ResponseEntity.noContent().build());
  }

  @Override
  public Mono<ResponseEntity<Void>> resumeListener() {
    return kafkaListenerManager.resume()
        .thenReturn(ResponseEntity.noContent().build());
  }
}
