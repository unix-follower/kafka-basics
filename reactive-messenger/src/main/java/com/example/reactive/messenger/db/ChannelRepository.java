package com.example.reactive.messenger.db;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChannelRepository extends ReactiveCrudRepository<Channel, UUID> {
}
