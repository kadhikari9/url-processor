package com.ebay.coding.assignment.service.url;

import com.ebay.coding.assignment.dto.*;
import com.ebay.coding.assignment.service.event.EventCoordinator;
import com.ebay.coding.assignment.service.event.Publisher;
import com.ebay.coding.assignment.service.http.HttpService;
import com.ebay.coding.assignment.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.*;

/**
 * Url processor that will make http call to the url
 */
public class SimpleUrlProcessor implements UrlProcessor, Publisher {

    public static final String TOPIC_NAME = "url_processing_event";

    private static final Logger logger = LoggerFactory.getLogger(SimpleUrlProcessor.class);
    private final LinkedBlockingQueue<String> urlProcessingQueue;
    private final DeadLetterQueue deadLetterQueue;
    private final ExecutorService worker = Executors.newFixedThreadPool(2);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final HttpService httpService;

    public SimpleUrlProcessor(final LinkedBlockingQueue<String> urlProcessingQueue, final DeadLetterQueue deadLetterQueue,
                              final HttpService httpService) {
        this.urlProcessingQueue = urlProcessingQueue;
        this.httpService = httpService;
        this.deadLetterQueue = deadLetterQueue;
    }

    @Override
    public void startProcessor() {
        logger.info("Starting Url Processor...");
        String pollInterval = PropertyUtil.INSTANCE.getProperty("url.processor.poll.interval", "1");

        scheduler.scheduleAtFixedRate(this::processUrl, 5, Integer.parseInt(pollInterval), TimeUnit.SECONDS);

    }

    @Override
    public void processUrl() {
        CompletableFuture.supplyAsync(this::process, worker).thenApply(val -> {
            if (val) {
                publish(new UrlProcessingEvent(EventType.URL_PROCESSING_SUCCESS));
            } else {
                publish(new UrlProcessingEvent(EventType.URL_PROCESSING_FAILED));
            }
            return val;
        });
    }

    private boolean process() {
        try {
            logger.info("Processing url from UrlProcessingQueue");
            String url = urlProcessingQueue.take();

            String resp = httpService.doGet(url, Collections.emptyMap());
            if (resp != null && !resp.isEmpty()) {
                logger.info("Successfully processed url:{}, response:{}", url, resp);
                deadLetterQueue.remove(url);

                return true;
            } else {
                logger.error("Failed processing url:{}", url);
                if (deadLetterQueue.isFull()) {
                    // queue is full too many failures, need to send alert to developers
                    logger.error("Dead letter queue is full. ");
                }

                DeadLetter deadLetter = deadLetterQueue.getOrDefault(url, new DeadLetter());
                deadLetter.setAttempts(deadLetter.getAttempts() + 1);
                deadLetter.setType(DeadLetter.Type.URL);
                deadLetter.setValue(url);
                logger.info("Adding url:{} to dead Letter for re-processing", url);
                deadLetterQueue.put(url, deadLetter);

                return false;
            }

        } catch (InterruptedException ex) {
            logger.error("Error processing url from the queue:{}", ex.getMessage());
        }

        return false;
    }

    @Override
    public void publish(Event event) {
        logger.info("Publishing Url processing event:{}", event.getType());
        EventCoordinator.INSTANCE.sendMessage(TOPIC_NAME, event);
    }
}
