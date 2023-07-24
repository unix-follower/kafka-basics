package com.example.messenger.mapper;

import com.example.messenger.db.Message;
import com.example.messenger.rest.api.model.MessageRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MessageMapper {
  @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC))")
  @Mapping(target = "updatedAt", expression = "java(message.getCreatedAt())", dependsOn = "createdAt")
  @Mapping(target = "data", source = "message")
  @Mapping(target = "messageId", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "channel", ignore = true)
  Message toEntity(MessageRequestDto dto);
}
