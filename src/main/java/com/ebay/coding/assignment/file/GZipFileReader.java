package com.ebay.coding.assignment.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class GZipFileReader implements FileReader {

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
            }

        } catch (IOException ex) {
            log.error("Error reading file from path:{}, error:{}", path, ex.getMessage());
        }

        return Collections.emptyList();
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
