package com.ebay.coding.assignment.service.event;

import com.ebay.coding.assignment.dto.Event;

public interface Publisher {

    void publish(Event event);
}
