package com.example.messenger.service;

import com.example.messenger.db.Message;

public interface MessageService {
  Message create(Message message);
}
