package com.example.messenger.component;

import com.example.messenger.db.AppUser;
import com.example.messenger.mapper.MessageMapper;
import com.example.messenger.mapper.QueueMessageDtoMapper;
import com.example.messenger.service.MessageService;
import com.example.messenger.service.UserService;
import com.example.messenger.rest.api.model.MessageRequestDto;
import com.example.messenger.config.AppProperties;
import com.example.messenger.queque.api.model.MessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@Transactional
public class MessengerControllerFacade {
  @Autowired
  private AppProperties appProperties;

  @Autowired
  private KafkaTemplate<String, MessageDto> kafkaTemplate;

  @Autowired
  private KafkaTemplate<String, MessageDto> nonTransactionalKafkaTemplate;

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

  public SendResult<String, MessageDto> produce(MessageRequestDto requestDto) {
    final var sendResult = doProduce(kafkaTemplate, requestDto);

    if (requestDto.messageContentType() != null) {
      final var mediaType = MediaType.parseMediaType(requestDto.messageContentType());
      if (MediaType.APPLICATION_JSON.equalsTypeAndSubtype(mediaType)) {
        final var rootNode = parseMessageAsJson(requestDto);
        maybeExecLogicBomb(rootNode);
      }
    }
    return sendResult;
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

  public SendResult<String, MessageDto> nonTransactionalProduce(MessageRequestDto requestDto) {
    return doProduce(nonTransactionalKafkaTemplate, requestDto);
  }

  private SendResult<String, MessageDto> doProduce(
      KafkaTemplate<String, MessageDto> kafkaTemplate,
      MessageRequestDto requestDto
  ) {
    final var user = userService.findById(requestDto.userId());

    final var message = messageMapper.toEntity(requestDto);
    final var savedMessage = messageService.create(message);
    final var producerRecord = createRecord(requestDto, savedMessage.getMessageId(), user);
    return kafkaTemplate.send(producerRecord).join();
  }

  public CompletableFuture<SendResult<String, MessageDto>> produceAsync(MessageRequestDto requestDto) {
    return CompletableFuture.supplyAsync(() -> {
          final var user = userService.findById(requestDto.userId());
          final var message = messageMapper.toEntity(requestDto);
          final var savedMessage = messageService.create(message);
          return createRecord(requestDto, savedMessage.getMessageId(), user);
        })
        .thenCompose(producerRecord ->
            // should fail with IllegalStateException because no transaction is in process
            kafkaTemplate.send(producerRecord)
        );
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
  public SendResult<String, MessageDto> produceWithNoDbTransactionInProgress(MessageRequestDto requestDto) {
    final var producerRecord = createRecord(requestDto, UUID.randomUUID(), null);
    // should fail with IllegalStateException because no transaction is in process
    return kafkaTemplate.send(producerRecord).join();
  }

  @Transactional(propagation = Propagation.NEVER)
  public SendResult<String, MessageDto> produceWithNoDbTransactionInProgressAndManualTxMgmt(MessageRequestDto requestDto) {
    final var producerRecord = createRecord(requestDto, UUID.randomUUID(), null);
    return Objects.requireNonNull(
        kafkaTemplate.executeInTransaction(operations -> operations.send(producerRecord))
    ).join();
  }
}
