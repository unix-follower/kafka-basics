package com.example.reactive.messenger.service;

import com.example.reactive.messenger.db.AppUser;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserService {
  Mono<AppUser> findById(UUID userId);
}
