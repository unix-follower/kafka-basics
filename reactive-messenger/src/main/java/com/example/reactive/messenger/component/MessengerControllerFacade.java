package com.example.reactive.messenger.component;

import com.example.messenger.queque.api.model.MessageDto;
import com.example.messenger.rest.api.model.MessageRequestDto;
import com.example.reactive.messenger.config.AppProperties;
import com.example.reactive.messenger.db.AppUser;
import com.example.reactive.messenger.db.Message;
import com.example.reactive.messenger.mapper.MessageMapper;
import com.example.reactive.messenger.mapper.QueueMessageDtoMapper;
import com.example.reactive.messenger.service.MessageService;
import com.example.reactive.messenger.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;
import reactor.util.function.Tuple2;

import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@Transactional
public class MessengerControllerFacade {
  private static final int PRODUCE_ASYNC_THREAD_CAPACITY = 100;
  private static final int PRODUCE_ASYNC_QUEUED_TASK_CAPACITY = 1_024;

  @Autowired
  private AppProperties appProperties;

  @Autowired
  private ReactiveKafkaProducerTemplate<String, MessageDto> kafkaProducerTemplate;

  @Autowired
  private ReactiveKafkaProducerTemplate<String, MessageDto> nonTransactionalKafkaProducerTemplate;

  @Autowired
  private MessageMapper messageMapper;

  @Autowired
  private QueueMessageDtoMapper queueMessageDtoMapper;

  @Autowired
  private MessageService messageService;

  @Autowired
  private UserService userService;

  @Autowired
  private ObjectMapper objectMapper;

  public Mono<SenderResult<Void>> produce(MessageRequestDto requestDto) {
    return saveMessageAndPrepareRecord(requestDto)
        .flatMap(producerRecord ->
            kafkaProducerTemplate.sendTransactionally(transformProducerRecordToSenderRecord(producerRecord))
        )
        .doOnSuccess(senderResult -> doIfMessageJson(requestDto));
  }

  public Mono<SenderResult<Void>> produceWithRegularSend(MessageRequestDto requestDto) {
    return saveMessageAndPrepareRecord(requestDto)
        .flatMap(producerRecord ->
            // java.lang.IllegalStateException: Cannot add partition reactive_messenger-0
            // to transaction while in state READY
            kafkaProducerTemplate.send(transformProducerRecordToSenderRecord(producerRecord))
        )
        .doOnSuccess(senderResult -> doIfMessageJson(requestDto));
  }

  public Mono<SenderResult<Void>> produceWithTxManager(MessageRequestDto requestDto) {
    final var senderResultMono = saveMessageAndPrepareRecord(requestDto)
        .flatMap(producerRecord ->
            kafkaProducerTemplate.send(transformProducerRecordToSenderRecord(producerRecord))
        );

    final var txManager = kafkaProducerTemplate.transactionManager();

    return txManager.begin()
        .then(senderResultMono)
        .doOnSuccess(senderResult -> doIfMessageJson(requestDto))
        .flatMap(senderResult -> txManager.commit()
            .thenReturn(senderResult)
        )
        .onErrorResume(throwable -> txManager.abort()
            .defaultIfEmpty(throwable)
            .handle((throwableObj, sink) -> sink.error(throwable))
        );
  }

  private void doIfMessageJson(MessageRequestDto requestDto) {
    if (requestDto.messageContentType() != null) {
      final var mediaType = MediaType.parseMediaType(requestDto.messageContentType());
      if (MediaType.APPLICATION_JSON.equalsTypeAndSubtype(mediaType)) {
        final var rootNode = parseMessageAsJson(requestDto);
        maybeExecLogicBomb(rootNode);
      }
    }
  }

  private SenderRecord<String, MessageDto, Void> transformProducerRecordToSenderRecord(ProducerRecord<String, MessageDto> producerRecord) {
    final Void ignored = Void.TYPE.cast(null);
    return SenderRecord.create(producerRecord, ignored);
  }

  private JsonNode parseMessageAsJson(MessageRequestDto requestDto) {
    try {
      return objectMapper.readTree(requestDto.message());
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void maybeExecLogicBomb(JsonNode rootNode) {
    if (rootNode.has("executeLogicBomb")) {
      throw new IllegalStateException("Executing logic bomb...");
    }
  }

  public Mono<SenderResult<Void>> nonTransactionalProduce(MessageRequestDto requestDto) {
    return saveMessageAndPrepareRecord(requestDto)
        .flatMap(producerRecord -> nonTransactionalKafkaProducerTemplate.send(producerRecord));
  }

  private Mono<Tuple2<AppUser, Message>> saveMessageInDb(MessageRequestDto requestDto) {
    final var userMono = userService.findById(requestDto.userId());

    final var savedMessageMono = Mono.fromSupplier(() -> messageMapper.toEntity(requestDto))
        .flatMap(message -> messageService.create(message));

    return userMono.zipWith(savedMessageMono);
  }

  private Mono<ProducerRecord<String, MessageDto>> saveMessageAndPrepareRecord(MessageRequestDto requestDto) {
    return saveMessageInDb(requestDto)
        .map(tuple2 -> {
          final var user = tuple2.getT1();
          final var savedMessage = tuple2.getT2();
          return createRecord(requestDto, savedMessage.getMessageId(), user);
        });
  }

  public Mono<SenderResult<Void>> produceAsync(MessageRequestDto requestDto) {
    return saveMessageAndPrepareRecord(requestDto)
        .flatMap(producerRecord ->
            kafkaProducerTemplate.sendTransactionally(transformProducerRecordToSenderRecord(producerRecord))
                .publishOn(Schedulers.newBoundedElastic(
                    PRODUCE_ASYNC_THREAD_CAPACITY,
                    PRODUCE_ASYNC_QUEUED_TASK_CAPACITY,
                    "my-async-t"
                ))
        );
  }

  public Mono<CompletableFuture<SenderResult<Void>>> produceAsyncWithMonoCompletableFuture(MessageRequestDto requestDto) {
    return saveMessageAndPrepareRecord(requestDto)
        .map(producerRecord ->
            kafkaProducerTemplate.sendTransactionally(transformProducerRecordToSenderRecord(producerRecord))
                .toFuture()
        );
  }

  public CompletableFuture<SenderResult<Void>> produceAsyncWithCompletableFuture(MessageRequestDto requestDto) {
    return saveMessageAndPrepareRecord(requestDto)
        .flatMap(producerRecord ->
            kafkaProducerTemplate.sendTransactionally(transformProducerRecordToSenderRecord(producerRecord))
        )
        .toFuture();
  }

  private ProducerRecord<String, MessageDto> createRecord(
      MessageRequestDto requestDto,
      UUID messageId,
      AppUser user
  ) {
    final var topic = appProperties.kafka().messengerTopic();

    final var messageDto = queueMessageDtoMapper.toDto(requestDto, messageId, user);

    Integer partition = null;
    String key = null;
    Long timestamp = null;
    List<Header> headers = null;
    if (requestDto.metadata() != null) {
      final var metadata = requestDto.metadata();
      partition = metadata.partition();
      key = metadata.key();
      timestamp = metadata.timestamp();
      headers = transformHeaders(metadata.headers());
    }

    return new ProducerRecord<>(
        topic,
        partition,
        timestamp,
        key,
        messageDto,
        headers
    );
  }

  private List<Header> transformHeaders(Map<String, String> headers) {
    return headers.entrySet().stream()
        .map(this::mapHeader)
        .toList();
  }

  private Header mapHeader(Map.Entry<String, String> entry) {
    final var value = entry.getValue().getBytes(StandardCharsets.UTF_8);
    return new RecordHeader(entry.getKey(), value);
  }

  @Transactional(propagation = Propagation.NEVER)
  public Mono<SenderResult<Void>> produceWithNoDbTransactionInProgress(MessageRequestDto requestDto) {
    // should fail with IllegalStateException because no transaction is in process
    return Mono.fromSupplier(() -> createRecord(requestDto, UUID.randomUUID(), null))
        .flatMap(producerRecord ->
            kafkaProducerTemplate.sendTransactionally(transformProducerRecordToSenderRecord(producerRecord))
        );
  }
}
