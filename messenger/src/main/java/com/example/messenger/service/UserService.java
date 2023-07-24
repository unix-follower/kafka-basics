package com.example.messenger.service;

import com.example.messenger.db.AppUser;

import java.util.UUID;

public interface UserService {
  AppUser findById(UUID userId);
}
