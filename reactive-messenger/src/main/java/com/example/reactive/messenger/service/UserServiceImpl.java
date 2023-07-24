package com.example.reactive.messenger.service;

import com.example.reactive.messenger.db.AppUser;
import com.example.reactive.messenger.db.UserRepository;
import com.example.reactive.messenger.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;

  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Mono<AppUser> findById(UUID userId) {
    return userRepository.findById(userId)
        .switchIfEmpty(Mono.error(UserNotFoundException::new));
  }
}
