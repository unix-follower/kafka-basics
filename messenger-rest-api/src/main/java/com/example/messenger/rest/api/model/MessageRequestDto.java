package com.example.messenger.rest.api.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record MessageRequestDto(
    @NotNull
    UUID userId,
    @NotNull
    UUID channelId,
    @NotEmpty
    String messageContentType,
    @NotNull
    @NotEmpty
    String message,
    Metadata metadata
) {
  public record Metadata(
      Integer partition,
      String key,
      Long timestamp,
      Map<String, String> headers
  ) {
  }
}
