package in.co.abi.dev.installer.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InstallerProperties {
	
	private static final Logger logger = LogManager.getLogger(InstallerProperties.class);
	private static Properties properties = new Properties();
	
	    static {
	        try (InputStream input = InstallerProperties.class.getClassLoader().getResourceAsStream("installer.properties")) {
	            if (input == null) {
	                throw new RuntimeException("Unable to find config.properties");
	            }
	            properties.load(input);
	        } catch (IOException e) {
	            throw new RuntimeException("Failed to load properties file", e);
	        }
	    }

	    private InstallerProperties() {}

	    public static String getProperty(String key) {
	    	logger.info("");
	        return properties.getProperty(key);
	    }

}
