package com.example.messenger.db;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChannelRepository extends CrudRepository<Channel, UUID> {
}
