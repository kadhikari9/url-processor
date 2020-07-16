package com.ebay.coding.assignment.dto;

import com.ebay.coding.assignment.service.url.SimpleUrlProcessor;

/**
 * Event implementation of Url processing
 */
public class UrlProcessingEvent implements Event {
    private final EventType eventType;

    public UrlProcessingEvent(EventType type) {
        this.eventType = type;
    }

    @Override
    public String getName() {
        return SimpleUrlProcessor.TOPIC_NAME;
    }

    @Override
    public EventType getType() {
        return eventType;
    }
}
