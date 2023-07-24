package com.example.reactive.messenger.consumer.config;

import com.example.messenger.queque.api.model.MessageDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.retry.support.RetryTemplate;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.Arrays;
import java.util.Collections;

@SuppressWarnings("unused")
@EnableKafka
@Configuration
public class KafkaConsumerConfig {
  @SuppressWarnings({"unchecked", "java:S2386"})
  public static final Class<? extends Throwable>[] EXCLUDE_RETRY_ERRORS = new Class[]{
      CommitFailedException.class
  };

  @Autowired
  private KafkaProperties kafkaProperties;

  @Bean
  ReactiveKafkaConsumerTemplate<String, MessageDto> kafkaConsumerTemplate(
      AppProperties appProperties,
      ObjectMapper objectMapper
  ) {
    final var topic = appProperties.kafka().messengerTopic();
    final var props = kafkaProperties.buildConsumerProperties();

    final var jsonDeserializer = new JsonDeserializer<>(MessageDto.class, objectMapper);
    final var receiverOptions = ReceiverOptions.<String, MessageDto>create(props)
        .withKeyDeserializer(new StringDeserializer())
        .withValueDeserializer(jsonDeserializer)
        .subscription(Collections.singleton(topic));

    final var consumer = kafkaProperties.getConsumer();
    if (Boolean.TRUE.equals(consumer.getEnableAutoCommit())) {
      receiverOptions.commitInterval(consumer.getAutoCommitInterval());
    }

    final var receiver = KafkaReceiver.create(receiverOptions);
    return new ReactiveKafkaConsumerTemplate<>(receiver);
  }

  @Bean
  ReactiveKafkaProducerTemplate<String, MessageDto> dltKafkaProducerTemplate(ObjectMapper objectMapper) {
    final var props = kafkaProperties.buildProducerProperties();

    final var typeReference = new TypeReference<MessageDto>() {
    };
    final var jsonSerializer = new JsonSerializer<>(typeReference, objectMapper);
    final var senderOptions = SenderOptions.<String, MessageDto>create(props)
        .withValueSerializer(jsonSerializer);

    final var sender = KafkaSender.create(senderOptions);
    return new ReactiveKafkaProducerTemplate<>(sender);
  }

  @Bean
  @ConditionalOnProperty(value = "app.kafka.retry.strategy", havingValue = KafkaRetryProperties.SPRING_RETRY_TEMPLATE)
  RetryTemplate retryTemplate() {
    final KafkaProperties.Retry.Topic topic = kafkaProperties.getRetry().getTopic();
    return RetryTemplate
        .builder()
        .maxAttempts(topic.getAttempts())
        .fixedBackoff(topic.getDelay())
        .notRetryOn(Arrays.stream(EXCLUDE_RETRY_ERRORS).toList())
        .build();
  }
}
