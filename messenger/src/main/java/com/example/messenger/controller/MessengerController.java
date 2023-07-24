package com.example.messenger.controller;

import com.example.messenger.api.MessengerApi;
import com.example.messenger.component.MessengerControllerFacade;
import com.example.messenger.queque.api.model.MessageDto;
import com.example.messenger.rest.api.model.MessageRequestDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@RestController
public class MessengerController implements MessengerApi {
  private final MessengerControllerFacade messengerControllerFacade;

  public MessengerController(MessengerControllerFacade messengerControllerFacade) {
    this.messengerControllerFacade = messengerControllerFacade;
  }

  @Override
  public ResponseEntity<SendResult<String, MessageDto>> produce(
      HttpHeaders headers,
      MessageRequestDto requestDto
  ) {
    final var scenario = headers.getOrDefault("Scenario", Collections.emptyList())
        .stream()
        .findFirst()
        .orElse("");

    final var sendResult = switch (scenario) {
      case "produceWithNoDbTransactionInProgress" ->
          messengerControllerFacade.produceWithNoDbTransactionInProgress(requestDto);
      case "produceWithNoDbTransactionInProgressAndManualTxMgmt" ->
          messengerControllerFacade.produceWithNoDbTransactionInProgressAndManualTxMgmt(requestDto);
      case "nonTransactionalProduce" -> messengerControllerFacade.nonTransactionalProduce(requestDto);
      default -> messengerControllerFacade.produce(requestDto);
    };
    return ResponseEntity.ok(sendResult);
  }

  @Override
  public CompletableFuture<ResponseEntity<SendResult<String, MessageDto>>> produceAsync(MessageRequestDto requestDto) {
    return messengerControllerFacade.produceAsync(requestDto)
        .thenApply(ResponseEntity::ok);
  }
}
