package com.example.messenger.api;

import com.example.messenger.queque.api.model.MessageDto;
import com.example.messenger.rest.api.model.MessageRequestDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.CompletableFuture;

@RequestMapping("/api/v1/messenger")
public interface MessengerApi {
  @PostMapping
  ResponseEntity<SendResult<String, MessageDto>> produce(
      @RequestHeader HttpHeaders headers,
      @RequestBody MessageRequestDto requestDto
  );

  @PostMapping(headers = {"Scenario=produceAsync"})
  CompletableFuture<ResponseEntity<SendResult<String, MessageDto>>> produceAsync(
      @RequestBody MessageRequestDto requestDto
  );
}
