package com.example.reactive.messenger.service;

import com.example.reactive.messenger.db.Message;
import reactor.core.publisher.Mono;

public interface MessageService {
  Mono<Message> create(Message message);
}
