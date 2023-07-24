package com.example.reactive.messenger.config;

import com.example.messenger.queque.api.model.MessageDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

@SuppressWarnings("unused")
@EnableKafka
@Configuration
class KafkaProducerConfig {
  @Autowired
  private AppProperties appProperties;

  @Autowired
  private KafkaProperties kafkaProperties;

  @Bean
  ReactiveKafkaProducerTemplate<String, MessageDto> kafkaProducerTemplate(ObjectMapper objectMapper) {
    return createKafkaProducerTemplate(objectMapper, true);
  }

  @Bean
  ReactiveKafkaProducerTemplate<String, MessageDto> nonTransactionalKafkaProducerTemplate(ObjectMapper objectMapper) {
    return createKafkaProducerTemplate(objectMapper, false);
  }

  private ReactiveKafkaProducerTemplate<String, MessageDto> createKafkaProducerTemplate(
      ObjectMapper objectMapper,
      boolean isTransactional
  ) {
    final var props = kafkaProperties.buildProducerProperties();

    if (!isTransactional) {
      props.remove(ProducerConfig.TRANSACTIONAL_ID_CONFIG);

      final var clientId = appProperties.kafka().nonTransactionalClientId();
      props.replace(ProducerConfig.CLIENT_ID_CONFIG, clientId);
    }

    final var typeReference = new TypeReference<MessageDto>() {
    };
    final var jsonSerializer = new JsonSerializer<>(typeReference, objectMapper);
    final var senderOptions = SenderOptions.<String, MessageDto>create(props)
        .withValueSerializer(jsonSerializer);

    final var sender = KafkaSender.create(senderOptions);
    return new ReactiveKafkaProducerTemplate<>(sender);
  }
}
