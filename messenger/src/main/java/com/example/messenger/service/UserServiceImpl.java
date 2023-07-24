package com.example.messenger.service;

import com.example.messenger.db.UserRepository;
import com.example.messenger.exception.UserNotFoundException;
import com.example.messenger.db.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;

  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public AppUser findById(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);
  }
}
