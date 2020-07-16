package com.ebay.coding.assignment.service.factory;

import com.ebay.coding.assignment.dto.DeadLetterQueue;
import com.ebay.coding.assignment.service.event.SimpleEventReporter;
import com.ebay.coding.assignment.service.file.AsyncDirectoryProcessor;
import com.ebay.coding.assignment.service.file.AsyncProcessor;
import com.ebay.coding.assignment.service.file.Processor;
import com.ebay.coding.assignment.service.file.GZipFileReader;
import com.ebay.coding.assignment.service.http.SimpleHttpService;
import com.ebay.coding.assignment.service.url.DeadLetterProcessor;
import com.ebay.coding.assignment.service.url.SimpleUrlProcessor;
import com.ebay.coding.assignment.service.url.UrlProcessor;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Singleton Factory class to create instances of all services used in this application
 */
public enum ServiceFactory {
    INSTANCE;

    LinkedBlockingQueue<String> fileProcessingQueue = new LinkedBlockingQueue<>();
    LinkedBlockingQueue<String> urlProcessingQueue = new LinkedBlockingQueue<>();
    DeadLetterQueue deadLetterQueue = new DeadLetterQueue();

    public Processor getFileProcessor(String type) {
        if (type.equalsIgnoreCase("file")) {
            return new AsyncProcessor(GZipFileReader.INSTANCE, fileProcessingQueue, urlProcessingQueue, deadLetterQueue);
        }
        if (type.equalsIgnoreCase("directory")) {
            return new AsyncDirectoryProcessor(GZipFileReader.INSTANCE, fileProcessingQueue);
        }

        throw new UnsupportedOperationException("Unsupported file processor type");
    }

    public UrlProcessor getUrlProcessor(String type) {
        if (type.equalsIgnoreCase("http")) {
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
