package com.ebay.coding.assignment.service.file;

import java.io.FilenameFilter;
import java.util.List;

public interface FileReader {

    /**
     * Reads file from a path
     * @param path file path
     * @return List of file contents on each line
     */
    List<String> readFile(String path);

    /**
     * Lists all the files in given path
     * @param path - directory path
     * @return list of files that matches the filter specified
     */
    List<String> listFiles(String path, FilenameFilter filenameFilter);

    /**
     * Move the files which is read and added for processing so that it wont be read again.
     * @param originalFile original file
     */
    boolean moveFiles(String originalFile);
}
