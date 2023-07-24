package com.example.reactive.messenger;

import com.example.reactive.messenger.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AppProperties.class})
public class ReactiveMessenger {
  public static void main(String[] args) {
    SpringApplication.run(ReactiveMessenger.class, args);
  }
}
