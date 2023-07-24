package com.example.reactive.messenger.api;

import com.example.messenger.rest.api.model.MessageRequestDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

import java.util.concurrent.CompletableFuture;

@RequestMapping("/api/v1/messenger")
public interface MessengerApi {
  @PostMapping
  Mono<ResponseEntity<SenderResult<Void>>> produce(
      @RequestHeader HttpHeaders headers,
      @RequestBody MessageRequestDto requestDto
  );

  @PostMapping(headers = {"Scenario=produceAsyncWithMonoCompletableFuture"})
  Mono<CompletableFuture<ResponseEntity<SenderResult<Void>>>> produceAsyncWithMonoCompletableFuture(
      @RequestBody MessageRequestDto requestDto
  );

  @PostMapping(headers = {"Scenario=produceAsyncWithCompletableFuture"})
  CompletableFuture<ResponseEntity<SenderResult<Void>>> produceAsyncWithCompletableFuture(
      @RequestBody MessageRequestDto requestDto
  );
}
