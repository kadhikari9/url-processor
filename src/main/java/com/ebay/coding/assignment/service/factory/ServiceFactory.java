package com.ebay.coding.assignment.service.factory;

import com.ebay.coding.assignment.dto.DeadLetterQueue;
import com.ebay.coding.assignment.service.event.SimpleEventReporter;
import com.ebay.coding.assignment.service.file.AsyncDirectoryProcessor;
import com.ebay.coding.assignment.service.file.AsyncFileProcessor;
import com.ebay.coding.assignment.service.file.Processor;
import com.ebay.coding.assignment.service.file.TextFileReader;
import com.ebay.coding.assignment.service.http.SimpleHttpService;
import com.ebay.coding.assignment.service.url.DeadLetterProcessor;
import com.ebay.coding.assignment.service.url.SimpleUrlProcessor;
import com.ebay.coding.assignment.service.url.UrlProcessor;
import com.ebay.coding.assignment.util.PropertyUtil;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Singleton Factory class to create instances of all services used in this application
 */
public enum ServiceFactory {
    INSTANCE;

    private final LinkedBlockingQueue<String> fileProcessingQueue;
    private final LinkedBlockingQueue<String> urlProcessingQueue;
    private final DeadLetterQueue deadLetterQueue = new DeadLetterQueue();

    ServiceFactory() {
        String maxFileQueue = PropertyUtil.INSTANCE.getProperty("max.file.processing.queue.size", "5");
        String maxUrlQueue = PropertyUtil.INSTANCE.getProperty("max.url.processing.queue.size", "1000");
        fileProcessingQueue = new LinkedBlockingQueue<>(Integer.parseInt(maxFileQueue));
        urlProcessingQueue = new LinkedBlockingQueue<>(Integer.parseInt(maxUrlQueue));

    }

    public Processor getFileProcessor(String type) {
        if ("file".equalsIgnoreCase(type)) {
            return new AsyncFileProcessor(TextFileReader.INSTANCE, fileProcessingQueue, urlProcessingQueue, deadLetterQueue);
        }
        if ("directory".equalsIgnoreCase(type)) {
            return new AsyncDirectoryProcessor(TextFileReader.INSTANCE, fileProcessingQueue);
        }

        throw new UnsupportedOperationException("Unsupported file processor type");
    }

    public UrlProcessor getUrlProcessor(String type) {
        if ("http".equalsIgnoreCase(type)) {
            return new SimpleUrlProcessor(urlProcessingQueue, deadLetterQueue, SimpleHttpService.INSTANCE);
        } else {
            throw new UnsupportedOperationException("Unsupported url processor type ");
        }
    }

    public DeadLetterProcessor getDeadLetterProcessor() {
        return new DeadLetterProcessor(deadLetterQueue, urlProcessingQueue, fileProcessingQueue);
    }

    public SimpleEventReporter getEventReporter() {
        return new SimpleEventReporter();
    }
}
