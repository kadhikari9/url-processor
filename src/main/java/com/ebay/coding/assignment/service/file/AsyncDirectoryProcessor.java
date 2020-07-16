package com.ebay.coding.assignment.service.file;

import com.ebay.coding.assignment.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.concurrent.*;

/**
 * Directory processor that reads the new files on directory and put them for processing
 */
public class AsyncDirectoryProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(AsyncDirectoryProcessor.class);
    private final FileReader fileReader;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Executor worker = Executors.newFixedThreadPool(1);
    private final LinkedBlockingQueue<String> fileProcessingQueue;

    public AsyncDirectoryProcessor(FileReader fileReader, LinkedBlockingQueue<String> fileProcessingQueue) {
        this.fileReader = fileReader;
        this.fileProcessingQueue = fileProcessingQueue;
    }

    @Override
    public void startProcessor() {
        logger.info("Starting directory processor...");
        String pollInterval = PropertyUtil.INSTANCE.getProperty("directory.processor.poll.interval", "60");

        scheduler.scheduleAtFixedRate(() -> {
            String basePath = PropertyUtil.INSTANCE.getProperty("url.files.path");
            String maxFileSize = PropertyUtil.INSTANCE.getProperty("max.file.size", "8192");

            FilenameFilter filenameFilter = (file, name) -> validateFile(Integer.parseInt(maxFileSize), file, name);

            CompletableFuture.supplyAsync(() -> fileReader.listFiles(basePath, filenameFilter), worker).thenApply(val -> {
                addToQueue(val);
                return val;
            });

        }, 10, Integer.parseInt(pollInterval), TimeUnit.SECONDS);
    }

    private boolean validateFile(int maxSize, File file, String name) {
        return file.length() > 0 && file.length() <= maxSize && name.endsWith(".gz");
    }

    private void addToQueue(List<String> urlFiles) {
        try {
            for (String file : urlFiles) {
                fileProcessingQueue.put(file);
            }
        } catch (InterruptedException ex) {
            logger.error("Thread interrupted while processing:{}", ex.getMessage());
        }
    }
}
