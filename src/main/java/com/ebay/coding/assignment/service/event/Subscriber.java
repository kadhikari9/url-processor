package com.ebay.coding.assignment.service.event;

import com.ebay.coding.assignment.dto.Event;

public interface Subscriber {

     /**
      * Method to call when subscriber receives message
      * @param topic Topic of subscription
      * @param event Message
      */
     void receiveMessage(String  topic, Event event);
}
