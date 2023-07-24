package com.example.messenger.consumer.component;

import com.example.messenger.consumer.config.KafkaConfigProperties;
import com.example.messenger.queque.api.model.ConsumerHintDto;
import com.example.messenger.queque.api.model.MessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@Component
public class KafkaConsumerImpl implements KafkaConsumer<String, MessageDto> {
  private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerImpl.class);

  private static final String MESSENGER_TOPIC_EXPR = "${" + KafkaConfigProperties.MESSENGER_TOPIC + "}";

  private static final String AUTO_ACK_MODE_EXPR =
      "#{!'${spring.kafka.listener.ack-mode}'.equals('MANUAL') && " +
          "!'${spring.kafka.listener.ack-mode}'.equals('MANUAL_IMMEDIATE')}";

  private static final String RETRYABLE_AUTO_ACK_MODE_EXPR =
      "#{" +
          "(!'${spring.kafka.listener.ack-mode}'.equals('MANUAL') && " +
          "!'${spring.kafka.listener.ack-mode}'.equals('MANUAL_IMMEDIATE')) && " +
          "'${spring.kafka.retry.topic.enabled}'" +
          "}";

  private static final String MANUAL_ACK_MODE_EXPR =
      "#{'${spring.kafka.listener.ack-mode}'.equals('MANUAL') || " +
          "'${spring.kafka.listener.ack-mode}'.equals('MANUAL_IMMEDIATE')}";

  private static final String RETRYABLE_MANUAL_ACK_MODE_EXPR =
      "#{" +
          "('${spring.kafka.listener.ack-mode}'.equals('MANUAL') || " +
          "'${spring.kafka.listener.ack-mode}'.equals('MANUAL_IMMEDIATE')) && " +
          "'${spring.kafka.retry.topic.enabled}'" +
          "}";

  private static final String CONSUMER = "consumer";
  private static final String CONTAINER_FACTORY_BEAN_NAME = "concurrentKafkaListenerContainerFactory";

  private final ObjectMapper objectMapper;

  private final Map<UUID, Integer> retryCountdownMap = new ConcurrentHashMap<>();

  public KafkaConsumerImpl(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Component
  @ConditionalOnProperty(value = KafkaConfigProperties.LISTENER_TYPE, havingValue = "SINGLE")
  @ConditionalOnExpression(AUTO_ACK_MODE_EXPR)
  @ConditionalOnMissingBean({RetryableListener.class, RetryableManualAckListener.class})
  private class Listener {
    @KafkaListener(
        id = "listener",
        topics = MESSENGER_TOPIC_EXPR,
        containerFactory = CONTAINER_FACTORY_BEAN_NAME
    )
    public void consume(ConsumerRecord<String, MessageDto> consumerRecord) {
      KafkaConsumerImpl.this.consume(consumerRecord, null);
    }
  }

  @Component
  @ConditionalOnProperty(value = KafkaConfigProperties.LISTENER_TYPE, havingValue = "SINGLE")
  @ConditionalOnExpression(MANUAL_ACK_MODE_EXPR)
  @ConditionalOnMissingBean({RetryableListener.class, RetryableManualAckListener.class})
  private class ManualAckListener {
    @KafkaListener(
        id = "manualAckListener",
        topics = MESSENGER_TOPIC_EXPR,
        containerFactory = CONTAINER_FACTORY_BEAN_NAME
    )
    public void consume(ConsumerRecord<String, MessageDto> consumerRecord, Acknowledgment ack) {
      KafkaConsumerImpl.this.consume(consumerRecord, ack);
    }
  }

  @Component
  @ConditionalOnProperty(value = KafkaConfigProperties.LISTENER_TYPE, havingValue = "SINGLE")
  @ConditionalOnExpression(RETRYABLE_AUTO_ACK_MODE_EXPR)
  private class RetryableListener {
    @KafkaListener(
        id = "retryableListener",
        idIsGroup = false,
        topics = MESSENGER_TOPIC_EXPR,
        containerFactory = CONTAINER_FACTORY_BEAN_NAME
    )
    @RetryableTopic(
        attempts = "${spring.kafka.retry.topic.attempts}",
        backoff = @Backoff(
            delayExpression = "${app.kafka.retry.delay-ms}",
            multiplierExpression = "${spring.kafka.retry.topic.multiplier}"
        ),
        dltStrategy = DltStrategy.FAIL_ON_ERROR,
        exclude = {
            CommitFailedException.class,
        },
        kafkaTemplate = "dltKafkaTemplate"
    )
    public void consume(ConsumerRecord<String, MessageDto> consumerRecord) {
      KafkaConsumerImpl.this.consume(consumerRecord, null);
    }

    @DltHandler
    public void executeDltHandler(ConsumerRecord<String, MessageDto> consumerRecord) {
      logger.info("Execute retry for record with offset = {}", consumerRecord.offset());
      processRecord(consumerRecord);
    }
  }

  @Component
  @ConditionalOnProperty(value = KafkaConfigProperties.LISTENER_TYPE, havingValue = "SINGLE")
  @ConditionalOnExpression(RETRYABLE_MANUAL_ACK_MODE_EXPR)
  private class RetryableManualAckListener {
    @KafkaListener(
        id = "retryableManualAckListener",
        containerFactory = CONTAINER_FACTORY_BEAN_NAME,
        topics = MESSENGER_TOPIC_EXPR
    )
    @RetryableTopic(
        attempts = "${spring.kafka.retry.topic.attempts}",
        backoff = @Backoff(
            delayExpression = "${app.kafka.retry.delay-ms}",
            multiplierExpression = "${spring.kafka.retry.topic.multiplier}"
        ),
        dltStrategy = DltStrategy.FAIL_ON_ERROR,
        exclude = {
            CommitFailedException.class,
        },
        kafkaTemplate = "dltKafkaTemplate"
    )
    public void consume(ConsumerRecord<String, MessageDto> consumerRecord, Acknowledgment ack) {
      KafkaConsumerImpl.this.consume(consumerRecord, ack);
    }

    @DltHandler
    public void executeDltHandler(ConsumerRecord<String, MessageDto> consumerRecord, Acknowledgment ack) {
      logger.info("Execute retry for record with offset = {}", consumerRecord.offset());
      processRecord(consumerRecord);
      ack.acknowledge();
    }
  }

  @Component
  @ConditionalOnProperty(value = KafkaConfigProperties.LISTENER_TYPE, havingValue = "BATCH")
  @ConditionalOnExpression(AUTO_ACK_MODE_EXPR)
  private class BatchListener {
    @KafkaListener(
        id = "batchListener",
        topics = MESSENGER_TOPIC_EXPR,
        containerFactory = CONTAINER_FACTORY_BEAN_NAME
    )
    public void consume(List<ConsumerRecord<String, MessageDto>> records) {
      KafkaConsumerImpl.this.consume(records, null);
    }
  }

  @Component
  @ConditionalOnProperty(value = KafkaConfigProperties.LISTENER_TYPE, havingValue = "BATCH")
  @ConditionalOnExpression(MANUAL_ACK_MODE_EXPR)
  private class ManualAckBatchListener {
    @KafkaListener(
        id = "manualAckBatchListener",
        topics = MESSENGER_TOPIC_EXPR,
        containerFactory = CONTAINER_FACTORY_BEAN_NAME
    )
    public void consume(List<ConsumerRecord<String, MessageDto>> records, Acknowledgment ack) {
      KafkaConsumerImpl.this.consume(records, ack);
    }
  }

  @Override
  public void consume(ConsumerRecord<String, MessageDto> consumerRecord, Acknowledgment ack) {
    consume(Collections.singletonList(consumerRecord), ack);
  }

  @Override
  public void consume(List<ConsumerRecord<String, MessageDto>> records, Acknowledgment ack) {
    records.forEach(this::processRecord);

    if (ack != null) {
      ack.acknowledge();
    }
  }

  private void processRecord(ConsumerRecord<String, MessageDto> consumerRecord) {
    final var messageDto = consumerRecord.value();

    if (messageDto.messageContentType() != null) {
      final var mediaType = MediaType.parseMediaType(messageDto.messageContentType());
      if (MediaType.APPLICATION_JSON.equalsTypeAndSubtype(mediaType)) {
        final var rootNode = parseMessageAsJson(messageDto);
        if (rootNode.has(CONSUMER)) {
          final var consumerNode = rootNode.get(CONSUMER);
          final var consumerHintDto = objectMapper.convertValue(consumerNode, ConsumerHintDto.class);

          maybeEmulateProcessingDelay(consumerHintDto);
          maybeFailConsumption(consumerRecord, consumerHintDto);
          maybeExecLogicBomb(consumerHintDto);
        }
      }
    }
  }

  private JsonNode parseMessageAsJson(MessageDto requestDto) {
    try {
      return objectMapper.readTree(requestDto.message());
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void maybeEmulateProcessingDelay(ConsumerHintDto consumerHintDto) {
    if (consumerHintDto.processingDelay() != null) {
      try {
        final var delay = consumerHintDto.processingDelay();
        logger.info("Sleep for {} seconds", delay);
        Thread.sleep(delay);
        logger.info("Wake up");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private void maybeFailConsumption(ConsumerRecord<String, MessageDto> consumerRecord, ConsumerHintDto consumerHintDto) {
    final var messageId = consumerRecord.value().messageId();
    final var failConsumptionTimes = consumerHintDto.failConsumptionTimes();
    if (failConsumptionTimes != null) {
      final Supplier<IllegalStateException> failSupplier = () -> new IllegalStateException("Retry failed...");
      final var counter = retryCountdownMap.get(messageId);
      if (counter == null) {
        retryCountdownMap.put(messageId, failConsumptionTimes - 1);
        throw failSupplier.get();
      } else if (counter > 0) {
        final var nextCount = counter - 1;
        retryCountdownMap.put(messageId, nextCount);
        throw failSupplier.get();
      } else {
        retryCountdownMap.remove(messageId);
      }
    }
  }

  private void maybeExecLogicBomb(ConsumerHintDto consumerHintDto) {
    final var executeLogicBomb = consumerHintDto.executeLogicBomb();
    if (executeLogicBomb != null && executeLogicBomb) {
      throw new IllegalStateException("Executing logic bomb...");
    }
  }
}
