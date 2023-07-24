package com.example.messenger.queque.api.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageDto(
    UUID messageId,
    UserDto user,
    String messageContentType,
    String message,
    OffsetDateTime createdAt
) {
}
