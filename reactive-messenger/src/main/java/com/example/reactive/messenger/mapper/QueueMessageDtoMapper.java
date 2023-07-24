package com.example.reactive.messenger.mapper;

import com.example.messenger.rest.api.model.MessageRequestDto;
import com.example.messenger.queque.api.model.MessageDto;
import com.example.reactive.messenger.db.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface QueueMessageDtoMapper {
  @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC))")
  MessageDto toDto(MessageRequestDto dto, UUID messageId, AppUser user);
}
