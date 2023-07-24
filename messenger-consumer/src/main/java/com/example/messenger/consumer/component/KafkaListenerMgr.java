package com.example.messenger.consumer.component;

import com.example.messenger.consumer.api.model.ListenerContainerResponseDto;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component
public class KafkaListenerMgr implements KafkaListenerManager {
  private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

  public KafkaListenerMgr(KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry) {
    this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
  }

  @Override
  public List<ListenerContainerResponseDto> getAllListenerContainers() {
    return kafkaListenerEndpointRegistry.getAllListenerContainers().stream()
        .map(container -> {
          final var groupId = container.getGroupId();
          final var assignedPartitions = container.getAssignedPartitions();
          return new ListenerContainerResponseDto(
              groupId,
              assignedPartitions
          );
        })
        .toList();
  }

  @Override
  public void start(String listenerId) {
    doWithListener(listenerId, MessageListenerContainer::start);
  }

  private void doWithListener(String listenerId, Consumer<MessageListenerContainer> operation) {
    final var container = kafkaListenerEndpointRegistry.getListenerContainer(listenerId);
    if (container != null) {
      operation.accept(container);
    }
  }

  @Override
  public void stop(String listenerId) {
    doWithListener(listenerId, MessageListenerContainer::stop);
  }

  @Override
  public void pause(String listenerId) {
    doWithListener(listenerId, MessageListenerContainer::pause);
  }

  @Override
  public void resume(String listenerId) {
    doWithListener(listenerId, MessageListenerContainer::resume);
  }
}
