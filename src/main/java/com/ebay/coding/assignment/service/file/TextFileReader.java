package com.ebay.coding.assignment.service.file;

import com.ebay.coding.assignment.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * File Reader implementation which reads GZip files or can get list of directories
 */
public enum TextFileReader implements FileReader {
    INSTANCE;

    private static final Logger log = LoggerFactory.getLogger(TextFileReader.class);

    @Override
    public List<String> readFile(String path) {

        List<String> result = new ArrayList<>();

        try (InputStream fileInputStream = new FileInputStream(path)) {
            log.info("Reading file from path:{}", path);

            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
            return result;

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
            return Arrays.stream(files).filter(val -> !val.isDirectory()).map(File::getPath).collect(Collectors.toList());
        } else {
            log.error("Path:{} doesn't exists.", path);
        }

        return Collections.emptyList();
    }

    /**
     * Move the files which is read and added for processing so that it wont be read again.
     *
     * @param originalFile original file
     */
    @Override
    public boolean moveFiles(String originalFile) {
        String newFilePath = "";
        try {
            String pathSeperator = File.separator;
            int fileNameIndex = originalFile.lastIndexOf(pathSeperator);
            String fileName = originalFile.substring(fileNameIndex + 1);
            String processDir = PropertyUtil.INSTANCE.getProperty("url.files.processed.path");
            newFilePath = processDir + pathSeperator + fileName;

            Path sourcePath = Paths.get(originalFile);
            Path destinationPath = Paths.get(newFilePath);

            log.info("Moving processed file:{} to new path:{}", originalFile, newFilePath);
            Files.move(sourcePath, destinationPath,
                    StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            log.error("Error moving file from:{} to:{}", originalFile, newFilePath);
        }

        return false;
    }

}
