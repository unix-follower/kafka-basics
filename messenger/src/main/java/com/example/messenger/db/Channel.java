package com.example.messenger.db;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
public class Channel {
  @Id
  @UuidGenerator
  private UUID channelId;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
  private String name;
  private String description;

  public UUID getChannelId() {
    return channelId;
  }

  public Channel setChannelId(UUID channelId) {
    this.channelId = channelId;
    return this;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public Channel setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public Channel setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  public String getName() {
    return name;
  }

  public Channel setName(String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public Channel setDescription(String description) {
    this.description = description;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Channel channel)) return false;
    return Objects.equals(channelId, channel.channelId) && Objects.equals(createdAt, channel.createdAt) && Objects.equals(updatedAt, channel.updatedAt) && Objects.equals(name, channel.name) && Objects.equals(description, channel.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(channelId, createdAt, updatedAt, name, description);
  }
}
