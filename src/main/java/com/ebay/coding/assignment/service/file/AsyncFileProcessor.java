package com.ebay.coding.assignment.service.file;

import com.ebay.coding.assignment.dto.DeadLetter;
import com.ebay.coding.assignment.dto.DeadLetterQueue;
import com.ebay.coding.assignment.service.url.SimpleUrlProcessor;
import com.ebay.coding.assignment.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

public class AsyncFileProcessor implements FileProcessor {

    private static final Logger logger = LoggerFactory.getLogger(SimpleUrlProcessor.class);
    private final FileReader fileReader;
    private final LinkedBlockingQueue<String> fileProcessingQueue;
    private final LinkedBlockingQueue<String> urlProcessingQueue;
    private final DeadLetterQueue deadLetterQueue;
    private final ExecutorService worker = Executors.newFixedThreadPool(1);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public AsyncFileProcessor(final FileReader fileReader, final LinkedBlockingQueue<String> fileProcessingQueue,
                              final LinkedBlockingQueue<String> urlProcessingQueue, final DeadLetterQueue deadLetterQueue) {
        this.fileReader = fileReader;
        this.fileProcessingQueue = fileProcessingQueue;
        this.urlProcessingQueue = urlProcessingQueue;
        this.deadLetterQueue = deadLetterQueue;
    }

    @Override
    public void startProcessor() {
        logger.info("Starting File Processor...");
        String pollInterval = PropertyUtil.INSTANCE.getProperty("file.processor.poll.interval", "1");

        scheduler.scheduleAtFixedRate(this::process, 10, Integer.parseInt(pollInterval), TimeUnit.SECONDS);
    }

    private void process() {
        try {
            logger.info("Processing files from fileProcessingQueue");
            String filePath = fileProcessingQueue.take();

            CompletableFuture.supplyAsync(() -> fileReader.readFile(filePath), worker).thenApply(val -> {
                addToQueue(val);
                return val;
            }).exceptionally(throwable -> {
                logger.error("Exception occured while reading file:{}, adding to deadletter.", filePath);
                DeadLetter deadLetter = deadLetterQueue.getOrDefault(filePath, new DeadLetter());
                deadLetter.setAttempts(deadLetter.getAttempts() + 1);
                deadLetter.setType(DeadLetter.Type.FILE);
                deadLetter.setValue(filePath);
                deadLetterQueue.put(filePath, deadLetter);

                return null;
            });

        } catch (InterruptedException ex) {
            logger.error("Error processing url from the queue:{}", ex.getMessage());
        }
    }

    private void addToQueue(List<String> urls) {
        try {
            for (String file : urls) {
                urlProcessingQueue.put(file);
            }
        } catch (InterruptedException ex) {
            logger.error("Thread interrupted while processing:{}", ex.getMessage());
        }
    }

}
