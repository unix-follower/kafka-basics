package com.example.reactive.messenger.consumer.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@RequestMapping("/api/v1/admin/listener")
public interface AdminListenerApi {
  @PostMapping("/start")
  Mono<ResponseEntity<Void>> startListener();

  @PostMapping("/stop")
  Mono<ResponseEntity<Void>> stopListener();

  @PostMapping("/pause")
  Mono<ResponseEntity<Void>> pauseListener();

  @PostMapping("/resume")
  Mono<ResponseEntity<Void>> resumeListener();
}
