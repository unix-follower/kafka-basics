package com.example.messenger.consumer.api.model;

import org.apache.kafka.common.TopicPartition;

import java.util.Collection;

public record ListenerContainerResponseDto(
    String groupId,
    Collection<TopicPartition> assignedPartitions
) {
}
