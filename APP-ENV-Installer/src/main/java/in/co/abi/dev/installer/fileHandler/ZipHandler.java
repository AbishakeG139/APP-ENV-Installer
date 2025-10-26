package in.co.abi.dev.installer.fileHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZipHandler {

	private static final Logger logger = LogManager.getLogger(ZipHandler.class);
	
	public String unzip(String zipFilePath, String destDir) {
		logger.info("");
		logger.debug("zipFilePath : " + zipFilePath + " destDir : " + destDir);
    	String fileName = new String();
        File dir = new File(destDir);
        boolean firstFolderPrinted = false;
        // Create output directory if it does not exist
        if (!dir.exists()) dir.mkdirs();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(destDir, entry.getName());
                // Create directories for subfolders in the ZIP file
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                    if (!firstFolderPrinted) {
                    	logger.info("First folder created: " + newFile.getName());
                        fileName = newFile.getName();
                        firstFolderPrinted = true;
                    }
                } else {
                    // Ensure parent directories exist
                    new File(newFile.getParent()).mkdirs();

                    // Write file content
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        } catch (Exception e) {
        	logger.error(e.getMessage());
            return null;
        }
		return fileName;
    }
	
}
