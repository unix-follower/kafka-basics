package com.example.messenger.consumer.controller;

import com.example.messenger.consumer.api.AdminListenerApi;
import com.example.messenger.consumer.api.model.ListenerContainerResponseDto;
import com.example.messenger.consumer.component.KafkaListenerManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AdminListenerController implements AdminListenerApi {
  private final KafkaListenerManager kafkaListenerManager;

  public AdminListenerController(KafkaListenerManager kafkaListenerManager) {
    this.kafkaListenerManager = kafkaListenerManager;
  }

  @Override
  public ResponseEntity<List<ListenerContainerResponseDto>> getAllListenerContainers() {
    final var containers = kafkaListenerManager.getAllListenerContainers();
    return ResponseEntity.ok(containers);
  }

  @Override
  public ResponseEntity<Void> startListener(String listenerId) {
    kafkaListenerManager.start(listenerId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> stopListener(String listenerId) {
    kafkaListenerManager.stop(listenerId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> pauseListener(String listenerId) {
    kafkaListenerManager.pause(listenerId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> resumeListener(String listenerId) {
    kafkaListenerManager.resume(listenerId);
    return ResponseEntity.noContent().build();
  }
}
