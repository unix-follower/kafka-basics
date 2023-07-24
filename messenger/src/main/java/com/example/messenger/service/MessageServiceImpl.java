package com.example.messenger.service;

import com.example.messenger.db.Message;
import com.example.messenger.db.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {
  private final MessageRepository messageRepository;

  public MessageServiceImpl(MessageRepository messageRepository) {
    this.messageRepository = messageRepository;
  }

  @Override
  public Message create(Message message) {
    return messageRepository.save(message);
  }
}
