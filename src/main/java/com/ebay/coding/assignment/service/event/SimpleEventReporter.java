package com.ebay.coding.assignment.service.event;

import com.ebay.coding.assignment.dto.Event;
import com.ebay.coding.assignment.service.url.SimpleUrlProcessor;
import com.ebay.coding.assignment.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple event reporter that will report how many url processed and how may failed.
 */
public class SimpleEventReporter implements EventReporter, Subscriber {

    private static final Logger logger = LoggerFactory.getLogger(SimpleEventReporter.class);
    private final AtomicLong totalFailed = new AtomicLong(0);
    private final AtomicLong totalSuccess = new AtomicLong(0);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private int interval;

    public SimpleEventReporter() {
        // Register as a subscriber to url_processing_event topic
        EventCoordinator.INSTANCE.registerSubscriber(this, SimpleUrlProcessor.TOPIC_NAME);

        String reportInterval = PropertyUtil.INSTANCE.getProperty("event.reporting.interval", "60");
        interval = Integer.parseInt(reportInterval);
    }

    @Override
    public void reportEvent() {
        scheduler.scheduleAtFixedRate(() -> {
            logger.info("Total Url Successfully Processed during past:{}, secs: {}", interval, totalSuccess.get());
            logger.info("Total Url Failed Processing during past:{}, secs: {}", interval, totalFailed.get());
            logger.info("Total Url Processing attempt during past:{}, secs:{}", interval, totalSuccess.addAndGet(totalFailed.get()));
            logger.info("Resetting counter...");
            totalFailed.set(0);
            totalSuccess.set(0);
        }, 10, interval, TimeUnit.SECONDS);
    }

    @Override
    public void receiveMessage(String topic, Event event) {
        logger.info("Received event:{}, on topic:{}", event.getName(), topic);

        switch (event.getType()) {
            case URL_PROCESSING_FAILED:
                totalFailed.incrementAndGet();
                break;
            case URL_PROCESSING_SUCCESS:
                totalSuccess.incrementAndGet();
                break;
            case TOO_MANY_FAILURES:
                logger.error("Too many failures processing URL. Trigger call to on-call person.");
            default:
                break;
        }
    }
}
