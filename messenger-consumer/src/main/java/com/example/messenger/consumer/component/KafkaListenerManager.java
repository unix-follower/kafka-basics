package com.example.messenger.consumer.component;

import com.example.messenger.consumer.api.model.ListenerContainerResponseDto;

import java.util.List;

public interface KafkaListenerManager {
  List<ListenerContainerResponseDto> getAllListenerContainers();

  void start(String listenerId);

  void stop(String listenerId);

  void pause(String listenerId);

  void resume(String listenerId);
}
