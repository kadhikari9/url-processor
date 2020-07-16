package com.ebay.coding.assignment.service.event;

import com.ebay.coding.assignment.dto.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum EventCoordinator {
    INSTANCE;

    private Map<String, List<Subscriber>> subscribers = new ConcurrentHashMap<>();

    public void sendMessage(String topic, Event m) {
        List<Subscriber> subs = subscribers.get(topic);
        for (Subscriber s : subs) {
            s.receiveMessage(topic, m);
        }
    }

    public void registerSubscriber(Subscriber s, String topic) {
        subscribers.computeIfAbsent(topic, s1 -> new ArrayList<>()).add(s);
    }
}
