package in.co.abi.dev.installer.fileHandler;

import java.io.File;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CreateNewFolder {
	
	private static final Logger logger = LogManager.getLogger(CreateNewFolder.class);
	
	public String createFolderWithRetries(String folderPath) {
		logger.info("");
		String output = null;
		int retryCount = 0;
		while( StringUtils.isEmpty(output) && retryCount < 3) {
			output = checkAndCreate(folderPath);
			retryCount++;
		}
		
		if(StringUtils.isEmpty(output)) {
			JOptionPane.showMessageDialog(null, "Failed to create folder program stoped retry");
			logger.error("Failed to create folder program stoped retry");
			System.exit(0);
		}
		
		return output;
		
	}
	
	public String checkAndCreate(String folderPath) {
		logger.info("");
        // Create a File object
        File folder = new File(folderPath);

        // Check if the folder exists
        if (!folder.exists()) {
            // Try to create the directory
            if (folder.mkdirs()) {
            	logger.info("Folder created successfully: " + folderPath);
            } else {
            	logger.info("Failed to create folder: " + folderPath);
                JOptionPane.showMessageDialog(null, "Failed to create folder: " + folderPath +"\n create folder then press ok");
                return null;
            }
        } else {
        	logger.info("Folder already exists: " + folderPath);
        }
        return folderPath;
    }

}
