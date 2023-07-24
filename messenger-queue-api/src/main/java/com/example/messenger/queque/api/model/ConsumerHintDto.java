package com.example.messenger.queque.api.model;

public record ConsumerHintDto(
    Boolean executeLogicBomb,
    Long processingDelay,
    Integer failConsumptionTimes
) {
}
