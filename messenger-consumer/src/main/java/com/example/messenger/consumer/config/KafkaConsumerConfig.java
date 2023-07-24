package com.example.messenger.consumer.config;

import com.example.messenger.queque.api.model.MessageDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.RetryListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

@SuppressWarnings("unused")
@EnableKafka
@Configuration
public class KafkaConsumerConfig {
  private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerConfig.class);

  @Autowired
  private KafkaProperties kafkaProperties;

  @Bean
  ConsumerFactory<String, MessageDto> consumerFactory(ObjectMapper objectMapper) {
    final var props = kafkaProperties.buildConsumerProperties();
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    return new DefaultKafkaConsumerFactory<>(
        props,
        new StringDeserializer(),
        new JsonDeserializer<>(MessageDto.class, objectMapper),
        true
    );
  }

  @Bean
  ConcurrentKafkaListenerContainerFactory<String, MessageDto> concurrentKafkaListenerContainerFactory(
      ConsumerFactory<String, MessageDto> consumerFactory
  ) {
    final var factory = new ConcurrentKafkaListenerContainerFactory<String, MessageDto>();
    factory.setConsumerFactory(consumerFactory);

    final var listener = kafkaProperties.getListener();
    factory.setBatchListener(listener.getType() == KafkaProperties.Listener.Type.BATCH);

    return factory;
  }

  @Bean
  KafkaTemplate<String, MessageDto> dltKafkaTemplate(
      KafkaProperties kafkaProperties,
      ObjectMapper objectMapper
  ) {
    final var props = kafkaProperties.buildProducerProperties();

    final var typeReference = new TypeReference<MessageDto>() {
    };
    final var producerFactory = new DefaultKafkaProducerFactory<>(
        props,
        new StringSerializer(),
        new JsonSerializer<>(typeReference, objectMapper)
    );

    return new KafkaTemplate<>(producerFactory);
  }

  @Bean
  CommonErrorHandler errorHandler(
      AppProperties appProperties,
      KafkaTemplate<String, MessageDto> dltKafkaTemplate
  ) {
    final var interval = appProperties.kafka().errorHandlerInterval().toMillis();
    final int attempts = appProperties.kafka().errorHandlerAttempts();

    final var recoverer = new DeadLetterPublishingRecoverer(dltKafkaTemplate);
    final var backOff = new FixedBackOff(interval, attempts);

    final var errorHandler = new DefaultErrorHandler(recoverer, backOff);
    errorHandler.setRetryListeners(new LoggingRetryListener());
    errorHandler.addNotRetryableExceptions(CommitFailedException.class);
    return errorHandler;
  }

  private static class LoggingRetryListener implements RetryListener {
    @Override
    public void failedDelivery(ConsumerRecord<?, ?> consumerRecord, Exception ex, int deliveryAttempt) {
      logger.warn("ConsumerRecord delivery failed. Attempt = {} for record offset = {}", deliveryAttempt, consumerRecord.offset());
    }

    @Override
    public void failedDelivery(ConsumerRecords<?, ?> records, Exception ex, int deliveryAttempt) {
      logger.warn(
          "ConsumerRecords delivery failed. Attempt = {}. Records count = {}",
          deliveryAttempt,
          records.count()
      );
    }
  }
}
