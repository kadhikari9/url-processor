package com.ebay.coding.assignment.service.url;

import com.ebay.coding.assignment.dto.DeadLetter;
import com.ebay.coding.assignment.dto.DeadLetterQueue;
import com.ebay.coding.assignment.service.http.HttpService;
import com.ebay.coding.assignment.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Observable;
import java.util.concurrent.*;

public class SimpleUrlProcessor extends Observable implements UrlProcessor {

    private static final Logger logger = LoggerFactory.getLogger(SimpleUrlProcessor.class);
    private final LinkedBlockingQueue<String> urlProcessingQueue;
    private final DeadLetterQueue deadLetterQueue;
    private final ExecutorService worker = Executors.newFixedThreadPool(1);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final HttpService httpService;

    public SimpleUrlProcessor(final LinkedBlockingQueue<String> urlProcessingQueue, final DeadLetterQueue deadLetterQueue,
                              final HttpService httpService) {
        this.urlProcessingQueue = urlProcessingQueue;
        this.httpService = httpService;
        this.deadLetterQueue = deadLetterQueue;
    }

    @Override
    public void processUrl() {
        logger.info("Starting Url Processor...");
        String pollInterval = PropertyUtil.INSTANCE.getProperty("url.processor.poll.interval", "1");

        scheduler.scheduleAtFixedRate(this::process, 10, Integer.parseInt(pollInterval), TimeUnit.SECONDS);

    }

    private void process() {
        CompletableFuture.supplyAsync(this::callUrl, worker).thenApply(val -> {
            notifyObservers();
            return true;
        });
    }

    private boolean callUrl() {
        try {
            logger.info("Processing url from UrlProcessingQueue");
            String urlFile = urlProcessingQueue.take();

            String resp = httpService.doGet(urlFile, Collections.emptyMap());
            if (resp != null && !resp.isEmpty()) {
                logger.info("Successfully processed url:{}, response:{}", urlFile, resp);
                deadLetterQueue.remove(urlFile);
            } else {
                logger.error("Failed processing url:{}", urlFile);
                if (deadLetterQueue.isFull()) {
                    // queue is full too many failures, need to send alert to developers
                    logger.error("Dead letter queue is full. ");
                }

                DeadLetter deadLetter = deadLetterQueue.getOrDefault(urlFile, new DeadLetter());
                deadLetter.setAttempts(deadLetter.getAttempts() + 1);
                deadLetter.setType(DeadLetter.Type.URL);
                deadLetter.setValue(urlFile);
                deadLetterQueue.put(urlFile, deadLetter);
            }

        } catch (InterruptedException ex) {
            logger.error("Error processing url from the queue:{}", ex.getMessage());
        }

        return false;
    }
}
