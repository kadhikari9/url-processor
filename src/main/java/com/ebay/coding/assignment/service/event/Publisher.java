package com.ebay.coding.assignment.service.event;

import com.ebay.coding.assignment.dto.Event;

/**
 * Publisher interface
 */
public interface Publisher {

    void publish(Event event);
}
