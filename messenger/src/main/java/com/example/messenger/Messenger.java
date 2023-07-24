package com.example.messenger;

import com.example.messenger.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AppProperties.class})
public class Messenger {
  public static void main(String[] args) {
    SpringApplication.run(Messenger.class, args);
  }
}
