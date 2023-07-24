package com.example.reactive.messenger.db;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Table
public class AppUser {
  @Id
  private UUID userId;
  @CreatedDate
  private OffsetDateTime createdAt;
  @LastModifiedDate
  private OffsetDateTime updatedAt;
  private String firstName;
  private String lastName;
  private String email;

  public UUID getUserId() {
    return userId;
  }

  public AppUser setUserId(UUID userId) {
    this.userId = userId;
    return this;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public AppUser setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public AppUser setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  public String getFirstName() {
    return firstName;
  }

  public AppUser setFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public String getLastName() {
    return lastName;
  }

  public AppUser setLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public String getEmail() {
    return email;
  }

  public AppUser setEmail(String email) {
    this.email = email;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AppUser that)) return false;
    return Objects.equals(userId, that.userId) && Objects.equals(createdAt, that.createdAt) && Objects.equals(updatedAt, that.updatedAt) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(email, that.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, createdAt, updatedAt, firstName, lastName, email);
  }
}
