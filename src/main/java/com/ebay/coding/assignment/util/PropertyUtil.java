package com.ebay.coding.assignment.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public enum PropertyUtil {
    INSTANCE;

    private static final String PROP_FILENAME = "application.properties";
    private final Logger log = LoggerFactory.getLogger(PropertyUtil.class);
    private final Properties properties = new Properties();

    PropertyUtil() {
        load();
    }

    private void load() {
        try (InputStream input = PropertyUtil.class.getClassLoader().getResourceAsStream(PROP_FILENAME)) {
            log.info("Loading property file:{}", PROP_FILENAME);
            properties.putAll(System.getProperties());
            properties.load(input);
        } catch (IOException e) {
            log.error("Error reading property file:{} Make sure the property file exists on classpath. error:{}",
                    PROP_FILENAME, e.getMessage());
        }
    }

    public String getProperty(String key, String def) {
        if (properties.get(key) != null) {
            return properties.getProperty(key);
        } else {
            log.trace("Property with key:{} doesn't exists, returning default val:{}", key, def);
            return def;
        }
    }
    public String getProperty(String key){
        return properties.getProperty(key);
    }
}
