package com.example.reactive.messenger.consumer.component;

import com.example.messenger.queque.api.model.ConsumerHintDto;
import com.example.messenger.queque.api.model.MessageDto;
import com.example.reactive.messenger.consumer.config.AppProperties;
import com.example.reactive.messenger.consumer.config.KafkaConsumerConfig;
import com.example.reactive.messenger.consumer.config.KafkaRetryProperties;
import com.example.reactive.messenger.consumer.exception.ConsumerRecordProcessingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.retry.ExhaustedRetryException;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
public class KafkaConsumerImpl implements KafkaConsumer {
  private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerImpl.class);

  private static final String CONSUMER = "consumer";

  private final Map<UUID, Integer> retryCountdownMap = new ConcurrentHashMap<>();

  @Autowired
  private ReactiveKafkaConsumerTemplate<String, MessageDto> kafkaConsumerTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ReactiveKafkaProducerTemplate<String, MessageDto> dltKafkaProducerTemplate;

  @Autowired
  private AppProperties appProperties;

  @Autowired
  private KafkaProperties kafkaProperties;

  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;

  private RetryTemplate retryTemplate;

  @Autowired(required = false)
  @ConditionalOnBean
  public KafkaConsumerImpl setRetryTemplate(RetryTemplate retryTemplate) {
    this.retryTemplate = retryTemplate;
    return this;
  }

  private Disposable listenWithSpringRetryStrategy() {
    return kafkaConsumerTemplate.receive()
        .doOnNext(this::processRecordCatching)
        .onErrorContinue((throwable, o) -> {
          @SuppressWarnings("unchecked") final var receiverRecord = (ReceiverRecord<String, MessageDto>) o;

          retryTemplate.execute(context -> {
                processRecordCatching(receiverRecord);
                acknowledgeRecord(receiverRecord);
                return receiverRecord;
              },
              context -> {
                if (throwable instanceof ConsumerRecordProcessingException) {
                  dispatchRecordToDLT(receiverRecord);
                  return receiverRecord;
                } else {
                  throw new ExhaustedRetryException(null, throwable);
                }
              }
          );
        })
        .subscribe(KafkaConsumerImpl::acknowledgeRecord, this::handleFatalError);
  }

  private void dispatchRecordToDLT(ReceiverRecord<String, MessageDto> receiverRecord) {
    final var producerRecord = createDltRecord(receiverRecord);
    dltKafkaProducerTemplate.send(producerRecord)
        .toFuture()
        .join();
    acknowledgeRecord(receiverRecord);
  }

  private Disposable listenWithReactorRetryStrategy() {
    return kafkaConsumerTemplate.receive()
        .doOnNext(this::processRecordCatching)
        .retryWhen(retrySpec())
        .onErrorResume(Exceptions::isRetryExhausted, this::handleRetryExhaustedException)
        .subscribe(KafkaConsumerImpl::acknowledgeRecord, this::handleFatalError);
  }

  @Override
  public Disposable listen() {
    final var retryStrategy = appProperties.kafka().retry().strategy();
    if (KafkaRetryProperties.SPRING_RETRY_TEMPLATE.equalsIgnoreCase(retryStrategy)) {
      return listenWithSpringRetryStrategy();
    } else {
      return listenWithReactorRetryStrategy();
    }
  }

  private static void acknowledgeRecord(ReceiverRecord<String, MessageDto> receiverRecord) {
    final var receiverOffset = receiverRecord.receiverOffset();
    receiverOffset.acknowledge();
  }

  private void handleFatalError(Throwable throwable) {
    logger.error("Fatal error occurred. Restart...", throwable);
    applicationEventPublisher.publishEvent(new ConsumerRestartEvent(throwable));
  }

  private Mono<ReceiverRecord<String, MessageDto>> handleRetryExhaustedException(Throwable throwable) {
    final var e = (ConsumerRecordProcessingException) throwable.getCause();
    final var consumerRecord = e.<String, MessageDto>getConsumerRecord();
    final var receiverRecord = (ReceiverRecord<String, MessageDto>) consumerRecord;
    return Mono.just(receiverRecord);
  }

  private ProducerRecord<String, MessageDto> createDltRecord(ConsumerRecord<String, MessageDto> consumerRecord) {
    final var topic = appProperties.kafka().messengerTopic() + "-dlt";
    final var headers = transformOriginalHeaders(consumerRecord);

    return new ProducerRecord<>(
        topic,
        consumerRecord.partition(),
        null,
        consumerRecord.key(),
        consumerRecord.value(),
        headers
    );
  }

  private List<Header> transformOriginalHeaders(ConsumerRecord<String, MessageDto> consumerRecord) {
    return Arrays.stream(consumerRecord.headers().toArray())
        .<Header>map(header -> {
          final var originalKey = "original-" + header.key();
          return new RecordHeader(originalKey, header.value());
        })
        .toList();
  }

  private RetryBackoffSpec retrySpec() {
    final var topic = kafkaProperties.getRetry().getTopic();

    return Retry.backoff(topic.getAttempts(), topic.getDelay())
        .doBeforeRetry(retrySignal -> {
          final long retryCounter = retrySignal.totalRetriesInARow();
          logger.info("Execute retry #{}", retryCounter);
        })
        .filter(throwable -> Arrays.stream(KafkaConsumerConfig.EXCLUDE_RETRY_ERRORS)
            .noneMatch(clazz -> throwable.getClass() == clazz)
        )
        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
          final var e = (ConsumerRecordProcessingException) retrySignal.failure();
          final var consumerRecord = e.<String, MessageDto>getConsumerRecord();
          final var receiverRecord = (ReceiverRecord<String, MessageDto>) consumerRecord;
          dispatchRecordToDLT(receiverRecord);

          return Exceptions.retryExhausted(e.getMessage(), e);
        });
  }

  private void processRecordCatching(ConsumerRecord<String, MessageDto> consumerRecord) {
    try {
      processRecord(consumerRecord);
    } catch (Exception e) {
      throw new ConsumerRecordProcessingException(consumerRecord, e);
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
