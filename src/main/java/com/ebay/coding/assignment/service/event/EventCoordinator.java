package com.ebay.coding.assignment.service.event;

import com.ebay.coding.assignment.dto.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coordinator class that will coordinate between event processor and event reporter.
 * Simple implementation of publish-subscribe design pattern
 */
public enum EventCoordinator {
    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(EventCoordinator.class);
    private Map<String, List<Subscriber>> subscribers = new ConcurrentHashMap<>();

    /**
     * Sends message to all subscribers subscribing the topic
     * @param topic name of topic
     * @param m Event message
     */
    public void sendMessage(String topic, Event m) {
        logger.info("Sending message to all subscribers on topic:{}", topic);
        List<Subscriber> subs = subscribers.get(topic);
        for (Subscriber s : subs) {
            s.receiveMessage(topic, m);
        }
    }

    /**
     * Registers a new subscriber
     * @param s Subscriber
     * @param topic topic name
     */
    public void registerSubscriber(Subscriber s, String topic) {
        logger.info("Registering new subscriber to topic:{}", topic);
        subscribers.computeIfAbsent(topic, s1 -> new ArrayList<>()).add(s);
    }
}
