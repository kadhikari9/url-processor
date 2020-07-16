package com.ebay.coding.assignment.file;

import com.ebay.coding.assignment.service.file.FileReader;
import com.ebay.coding.assignment.service.file.TextFileReader;
import com.ebay.coding.assignment.util.PropertyUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.FilenameFilter;
import java.util.List;

public class TextFileReaderTest {

    private FileReader fileReader = TextFileReader.INSTANCE;

    @Test
    public void testReadDirectory() {

        String basePath = PropertyUtil.INSTANCE.getProperty("url.files.path");
        FilenameFilter filenameFilter = (dir, name) -> name.endsWith(".gz") && dir.length() > 0 && dir.lastModified() > 0;

        List<String> result = fileReader.listFiles(basePath, filenameFilter);
        Assert.assertFalse(result.isEmpty());

    }

    @Test
    public void testReadZipFiles() {
        String basePath = PropertyUtil.INSTANCE.getProperty("url.files.path");
        List<String> urls = fileReader.readFile(basePath + "url.0.gz");
        Assert.assertNotNull(urls);
        Assert.assertFalse(urls.isEmpty());
    }
}
