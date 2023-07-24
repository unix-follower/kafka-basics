package com.example.reactive.messenger.db;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface MessageRepository extends ReactiveCrudRepository<Message, UUID> {
  @Modifying
  @Query("""
      INSERT INTO message (
        message_id,
        created_at,
        updated_at,
        data,
        user_id,
        channel_id
      ) VALUES
      (
        :messageId,
        :createdAt,
        :updatedAt,
        :data,
        :userId,
        :channelId
      )
      """)
  Mono<Integer> insert(
      UUID messageId,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt,
      String data,
      UUID userId,
      UUID channelId
  );
}
