package Utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigReader.class);
    private static final String CONFIG = "/serenity.properties";

    private Properties config;
    private static final ConfigReader SINGLETON = new ConfigReader();

    private ConfigReader() {
        this.config = new Properties();
        try {
            config.load(getClass().getResourceAsStream(CONFIG));
        } catch (IOException e) {
            LOGGER.error("Broken configuration. Don't you hate it?", e);
        }
    }

    private Properties getConfig() {
        return config;
    }

    /**
     * A key which is defined in config.properties
     * @param key
     * @return
     */
    public static String getProperty(String key) {
        return SINGLETON.getConfig().getProperty(key);
    }

}
