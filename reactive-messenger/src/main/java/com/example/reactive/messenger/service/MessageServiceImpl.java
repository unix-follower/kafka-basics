package com.example.reactive.messenger.service;

import com.example.reactive.messenger.db.Message;
import com.example.reactive.messenger.db.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {
  private final MessageRepository messageRepository;

  public MessageServiceImpl(MessageRepository messageRepository) {
    this.messageRepository = messageRepository;
  }

  @Override
  public Mono<Message> create(Message message) {
    return messageRepository.insert(
        message.getMessageId(),
        message.getCreatedAt(),
        message.getUpdatedAt(),
        message.getData(),
        message.getUserId(),
        message.getChannelId()
    )
        .thenReturn(message);
  }
}
