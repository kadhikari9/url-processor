package com.ebay.coding.assignment.service.url;

import com.ebay.coding.assignment.dto.DeadLetter;
import com.ebay.coding.assignment.dto.DeadLetterQueue;
import com.ebay.coding.assignment.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Processor that processes failed urls or files that couldn't be read and add back them to processing
 */
public class DeadLetterProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DeadLetterProcessor.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService worker = Executors.newFixedThreadPool(1);
    private final DeadLetterQueue deadLetterQueue;
    private final int maxAttempts;
    private final LinkedBlockingQueue<String> urlProcessingQueue;
    private final LinkedBlockingQueue<String> fileProcessingQueue;

    public DeadLetterProcessor(final DeadLetterQueue deadLetterQueue, LinkedBlockingQueue<String> urlProcessingQueue,
                               LinkedBlockingQueue<String> fileProcessingQueue) {
        this.deadLetterQueue = deadLetterQueue;
        String attempts = PropertyUtil.INSTANCE.getProperty("deadlettter.max.attempts", "3");
        maxAttempts = Integer.parseInt(attempts);
        this.urlProcessingQueue = urlProcessingQueue;
        this.fileProcessingQueue = fileProcessingQueue;
    }

    public void startProcessor() {

        logger.info("Starting Url Processor...");
        String pollInterval = PropertyUtil.INSTANCE.getProperty("deadlettter.processor.poll.interval", "60");

        scheduler.scheduleAtFixedRate(this::run, 5, Integer.parseInt(pollInterval), TimeUnit.SECONDS);
    }

    private void run() {
        CompletableFuture.supplyAsync(this::process, worker);
    }

    private boolean process() {
        try {
            Set<String> removeList = new HashSet<>();

            for (Map.Entry<String, DeadLetter> deadLetterEntry : deadLetterQueue.entrySet()) {
                DeadLetter value = deadLetterEntry.getValue();

                logger.info("Reprocessing failed {} :{} ", value.getType(), value.getValue());

                if (value.getAttempts() >= maxAttempts) {
                    logger.info("Max attempts reached for {} :{}, adding for removal", value.getType(), value.getValue());
                    removeList.add(deadLetterEntry.getKey());
                } else {
                    if (value.getType().equals(DeadLetter.Type.FILE)) {
                        fileProcessingQueue.put(value.getValue());
                    } else {
                        urlProcessingQueue.put(value.getValue());
                    }
                }
            }

            removeList.forEach(deadLetterQueue::remove);

            return true;
        } catch (InterruptedException ex) {
            logger.info("Thread interuptted while execution:{}", ex.getMessage());
        }

        return false;
    }
}
