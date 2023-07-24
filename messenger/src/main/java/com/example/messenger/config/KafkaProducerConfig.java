package com.example.messenger.config;

import com.example.messenger.queque.api.model.MessageDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@SuppressWarnings("unused")
@EnableKafka
@Configuration
class KafkaProducerConfig {
  @Autowired
  private KafkaProperties kafkaProperties;

  @Bean
  KafkaTemplate<String, MessageDto> kafkaTemplate(ObjectMapper objectMapper) {
    final var producerFactory = producerFactory(objectMapper, true);
    return new KafkaTemplate<>(producerFactory);
  }

  @Bean
  KafkaTemplate<String, MessageDto> nonTransactionalKafkaTemplate(
      AppProperties appProperties,
      ObjectMapper objectMapper
  ) {
    final var producerFactory = producerFactory(objectMapper, false);
    final var clientId = appProperties.kafka().nonTransactionalClientId();
    final Map<String, Object> overrides = Map.of(ProducerConfig.CLIENT_ID_CONFIG, clientId);
    return new KafkaTemplate<>(producerFactory, overrides);
  }

  private ProducerFactory<String, MessageDto> producerFactory(
      ObjectMapper objectMapper,
      boolean isTransactional
  ) {
    final var props = kafkaProperties.buildProducerProperties();

    final var typeReference = new TypeReference<MessageDto>() {
    };
    final var producerFactory = new DefaultKafkaProducerFactory<>(
        props,
        new StringSerializer(),
        new JsonSerializer<>(typeReference, objectMapper)
    );
    if (isTransactional) {
      producerFactory.setTransactionIdPrefix("tx-");
    }
    return producerFactory;
  }
}
