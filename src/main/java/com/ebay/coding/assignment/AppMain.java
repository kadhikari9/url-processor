package com.ebay.coding.assignment;

import com.ebay.coding.assignment.service.event.EventReporter;
import com.ebay.coding.assignment.service.factory.ServiceFactory;
import com.ebay.coding.assignment.service.file.Processor;
import com.ebay.coding.assignment.service.url.DeadLetterProcessor;
import com.ebay.coding.assignment.service.url.UrlProcessor;

public class AppMain {

    public void start() {
        ServiceFactory factory = ServiceFactory.INSTANCE;

        Processor directoryProcessor = factory.getFileProcessor("directory");
        Processor fileProcessor = factory.getFileProcessor("file");
        UrlProcessor urlProcessor = factory.getUrlProcessor("http");
        DeadLetterProcessor deadLetterProcessor = factory.getDeadLetterProcessor();
        EventReporter eventReporter = factory.getEventReporter();

        directoryProcessor.startProcessor();
        fileProcessor.startProcessor();
        urlProcessor.startProcessor();
        deadLetterProcessor.startProcessor();
        eventReporter.reportEvent();
    }


    public static void main(String[] args) {
        new AppMain().start();
    }
}
