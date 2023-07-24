package com.example.reactive.messenger.consumer;

import com.example.reactive.messenger.consumer.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class ReactiveMessengerConsumer {
  public static void main(String[] args) {
    SpringApplication.run(ReactiveMessengerConsumer.class, args);
  }
}
