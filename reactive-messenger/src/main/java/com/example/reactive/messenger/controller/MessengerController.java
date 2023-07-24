package com.example.reactive.messenger.controller;

import com.example.messenger.rest.api.model.MessageRequestDto;
import com.example.reactive.messenger.api.MessengerApi;
import com.example.reactive.messenger.component.MessengerControllerFacade;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@RestController
public class MessengerController implements MessengerApi {
  private final MessengerControllerFacade messengerControllerFacade;

  public MessengerController(MessengerControllerFacade messengerControllerFacade) {
    this.messengerControllerFacade = messengerControllerFacade;
  }

  @Override
  public Mono<ResponseEntity<SenderResult<Void>>> produce(HttpHeaders headers, MessageRequestDto requestDto) {
    return Mono.fromSupplier(() ->
            headers.getOrDefault("Scenario", Collections.emptyList())
                .stream()
                .findFirst()
                .orElse("")
        )
        .flatMap(scenario -> resolveScenario(requestDto, scenario))
        .map(ResponseEntity::ok);
  }

  private Mono<SenderResult<Void>> resolveScenario(MessageRequestDto requestDto, String scenario) {
    return switch (scenario) {
      case "produceWithNoDbTransactionInProgress" ->
          messengerControllerFacade.produceWithNoDbTransactionInProgress(requestDto);
      case "nonTransactionalProduce" -> messengerControllerFacade.nonTransactionalProduce(requestDto);
      case "produceAsync" -> messengerControllerFacade.produceAsync(requestDto);
      case "produceWithTxManager" -> messengerControllerFacade.produceWithTxManager(requestDto);
      case "produceWithRegularSend" -> messengerControllerFacade.produceWithRegularSend(requestDto);
      default -> messengerControllerFacade.produce(requestDto);
    };
  }

  @Override
  public Mono<CompletableFuture<ResponseEntity<SenderResult<Void>>>> produceAsyncWithMonoCompletableFuture(
      MessageRequestDto requestDto
  ) {
    return messengerControllerFacade.produceAsyncWithMonoCompletableFuture(requestDto)
        .map(future -> future.thenApply(ResponseEntity::ok));
  }

  @Override
  public CompletableFuture<ResponseEntity<SenderResult<Void>>> produceAsyncWithCompletableFuture(
      MessageRequestDto requestDto
  ) {
    return messengerControllerFacade.produceAsyncWithCompletableFuture(requestDto)
        .thenApply(ResponseEntity::ok);
  }
}
