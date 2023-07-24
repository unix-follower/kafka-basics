package com.example.reactive.messenger.db;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Table
public class Message {
  @Id
  private UUID messageId;
  @CreatedDate
  private OffsetDateTime createdAt;
  @LastModifiedDate
  private OffsetDateTime updatedAt;
  private String data;
  private UUID userId;
  private UUID channelId;
  @Transient
  private AppUser user;
  @Transient
  private Channel channel;

  public UUID getMessageId() {
    return messageId;
  }

  public Message setMessageId(UUID messageId) {
    this.messageId = messageId;
    return this;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public Message setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public Message setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  public String getData() {
    return data;
  }

  public Message setData(String data) {
    this.data = data;
    return this;
  }

  public UUID getChannelId() {
    return channelId;
  }

  public Message setChannelId(UUID channelId) {
    this.channelId = channelId;
    return this;
  }

  public UUID getUserId() {
    return userId;
  }

  public Message setUserId(UUID userId) {
    this.userId = userId;
    return this;
  }

  public AppUser getUser() {
    return user;
  }

  public Message setUser(AppUser user) {
    this.user = user;
    return this;
  }

  public Channel getChannel() {
    return channel;
  }

  public Message setChannel(Channel channel) {
    this.channel = channel;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Message message)) return false;
    return Objects.equals(messageId, message.messageId) && Objects.equals(createdAt, message.createdAt) && Objects.equals(updatedAt, message.updatedAt) && Objects.equals(data, message.data) && Objects.equals(userId, message.userId) && Objects.equals(channelId, message.channelId) && Objects.equals(user, message.user) && Objects.equals(channel, message.channel);
  }

  @Override
  public int hashCode() {
    return Objects.hash(messageId, createdAt, updatedAt, data, channelId, user, channel);
  }
}
