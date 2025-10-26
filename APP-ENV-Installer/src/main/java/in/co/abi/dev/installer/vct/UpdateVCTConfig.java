package in.co.abi.dev.installer.vct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UpdateVCTConfig {

	private static final Logger logger = LogManager.getLogger(UpdateVCTConfig.class);

	public void env(String filePath, String backupPath, String tomcatPath, String sqlTmpPath) {

		logger.info("filePath : " + filePath 
				+ "\n" + "backupPath : " + backupPath 
				+ "\n" + "tomcatPath : " + tomcatPath
				+ "\n" + "sqlTmpPath : " + sqlTmpPath);

		try {
			Path path = Paths.get(filePath);
			String content = new String(Files.readAllBytes(path));

			content = content.replaceAll("(?m)^BACKUP_PATH=.*", "BACKUP_PATH=" + backupPath);
			content = content.replaceAll("(?m)^TOMCAT_PATH=.*", "TOMCAT_PATH=" + tomcatPath);
			content = content.replaceAll("(?m)^SQL_DOWNLOAD_PATH=.*", "SQL_DOWNLOAD_PATH=" + sqlTmpPath);

			Files.write(path, content.getBytes());
			logger.info("Configuration updated successfully.");
		} catch (IOException e) {
			logger.error("Error updating configuration: " + e.getMessage());
		}
	}

}
