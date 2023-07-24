package com.example.messenger.consumer;

import com.example.messenger.consumer.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class MessengerConsumer {
  public static void main(String[] args) {
    SpringApplication.run(MessengerConsumer.class, args);
  }
}
