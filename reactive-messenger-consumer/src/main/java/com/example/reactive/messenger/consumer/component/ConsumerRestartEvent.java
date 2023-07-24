package com.example.reactive.messenger.consumer.component;

import org.springframework.context.ApplicationEvent;

public class ConsumerRestartEvent extends ApplicationEvent {
  public ConsumerRestartEvent(Object source) {
    super(source);
  }
}
