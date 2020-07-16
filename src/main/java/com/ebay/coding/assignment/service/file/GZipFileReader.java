package com.ebay.coding.assignment.service.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * File Reader implementation which reads GZip files or can get list of directories
 */
public enum  GZipFileReader implements FileReader {
    INSTANCE;

    private static final Logger log = LoggerFactory.getLogger(GZipFileReader.class);

    @Override
    public List<String> readFile(String path) {

        List<String> result = new ArrayList<>();

        try (InputStream fileInputStream = new FileInputStream(path)) {
            log.info("Reading file from path:{}", path);

            try (GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream)) {

                BufferedReader reader = new BufferedReader(new InputStreamReader(gzipInputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.add(line);
                }
                return result;

            } catch (IOException ex) {
                log.error("Error processing Zip Input Stream:{}", ex.getMessage());
                throw new RuntimeException(ex);
            }
            //shouldn't reach here
        } catch (IOException ioException) {
            log.error("Error reading file from path:{}, error:{}", path, ioException.getMessage());
            throw new RuntimeException(ioException);
        } catch (Exception ex) {
            throw new RuntimeException("Exception reading directory on path:" + path);
        }

    }

    @Override
    public List<String> listFiles(String path, final FilenameFilter filenameFilter) {

        File file = new File(path);

        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles(filenameFilter);
            if (files == null || files.length == 0) {
                log.error("No files found in directory.{}, matching extension: filter.", path);
                return Collections.emptyList();
            }
            return Arrays.stream(files).map(File::getPath).collect(Collectors.toList());
        } else {
            log.error("Path:{} doesn't exists.", path);
        }

        return Collections.emptyList();
    }

}
