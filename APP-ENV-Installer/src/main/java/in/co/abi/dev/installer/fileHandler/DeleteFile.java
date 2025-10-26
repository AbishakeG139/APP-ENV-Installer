package in.co.abi.dev.installer.fileHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeleteFile {
	
	private static final Logger logger = LogManager.getLogger(DeleteFile.class);
	
	public void checkAndDelete(String filePath) {
		logger.info("");
        File file = new File(filePath);
        if (file.exists()) {
            if (file.delete()) {
            	logger.info("File deleted successfully.");
            } else {
            	logger.error("Failed to delete the file.");
            }
        } else {
        	logger.info("File does not exist.");
        }
    }
	
	 public void deleteContents(String path) {
		 logger.info("path : " + path);
		 Path directoryPath = Paths.get(path);
	        if (!Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
	        	logger.error("Directory does not exist: " + directoryPath);
	            return;
	        }

	        try {
	            Files.walkFileTree(directoryPath, new SimpleFileVisitor<Path>() {
	                @Override
	                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	                    Files.delete(file); // Delete each file
	                    return FileVisitResult.CONTINUE;
	                }

	                @Override
	                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
	                    if (!dir.equals(directoryPath)) { // Avoid deleting the root folder
	                        Files.delete(dir);
	                    }
	                    return FileVisitResult.CONTINUE;
	                }
	            });
	            logger.trace("All contents deleted inside: " + directoryPath);
	        } catch (IOException e) {
	        	logger.error("Failed to delete contents: " + e.getMessage());
	        }
	    }

}
