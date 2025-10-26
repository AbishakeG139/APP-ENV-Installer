package in.co.abi.dev.installer.config;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.co.abi.dev.installer.fileHandler.CreateNewFolder;
import in.co.abi.dev.installer.fileHandler.FileDownloader;
import in.co.abi.dev.installer.fileHandler.ZipHandler;
import in.co.abi.dev.installer.popups.LoadingPopup;
import in.co.abi.dev.installer.properties.InstallerProperties;

public class WarConfig {

	private static final Logger logger = LogManager.getLogger(WarConfig.class);

	private int winX;
	private String currentFilePath;
	private String appWarURL = InstallerProperties.getProperty("AppWarURL");
	LoadingPopup loadingPopup;

	public WarConfig(int winX, String currentFilePath) {
		this.winX = winX;
		this.currentFilePath = currentFilePath;
	}

	public void configRestaurantAppWar(String tomcatPath) {
		try {
			logger.info("");
			String tmpPath = currentFilePath + File.separator + "tmp";
			loadingPopup = new LoadingPopup("Downloading App War, please wait...");
			String fileName = new FileDownloader().downloadFile(appWarURL, tmpPath);
			loadingPopup.close();

			loadingPopup = new LoadingPopup("Configuring  Restaurant App War, please wait...");
			String restAppPath = new CreateNewFolder().createFolderWithRetries(
					tomcatPath + File.separator + "webapps" + File.separator + "RestaurantApp");
			copyFileToFolder(tmpPath + File.separator + fileName, tomcatPath + File.separator + "webapps");
			new ZipHandler().unzip(tmpPath + File.separator + fileName, restAppPath);
			WebXmlUpdater(tomcatPath);
			messagesEnUSUpdater(tomcatPath);
			applicationContextUpdater(tomcatPath);
			log4j2Updater(tomcatPath);
			loadingPopup.close();

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}

	private void copyFileToFolder(String sourcePath, String destinationFolder) {
		try {
			logger.info("");
			Path source = Paths.get(sourcePath);
			Path destination = Paths.get(destinationFolder, source.getFileName().toString()); // Append file name

			// Copy the file (overwrite if exists)
			Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);

			logger.info("File copied successfully to " + destination.toString());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	private void WebXmlUpdater(String tomcatPath) {

		try {
			logger.info("");
			String filePath = tomcatPath + File.separator + "webapps\\RestaurantApp\\WEB-INF\\web.xml";

			String content = new String(Files.readAllBytes(Paths.get(filePath)));

			content = content.replaceAll("<param-value>/usr/local/tomcat/ImageRepository/</param-value>",
					"<param-value>" + currentFilePath.replace("\\", "/") + "/ImageRepository/" + "</param-value>");

			Files.write(Paths.get(filePath), content.getBytes());

			logger.info("web.xml updated successfully.");

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}

	private void messagesEnUSUpdater(String tomcatPath) {

		try {
			logger.info("");
			String filePath = tomcatPath + File.separator
					+ "webapps\\RestaurantApp\\WEB-INF\\classes\\messages_en_US.properties";

			String content = new String(Files.readAllBytes(Paths.get(filePath)));

			content = content.replaceAll("(?m)^rs.server.instancetype=.*", "rs.server.instancetype=BRANCH");

			Files.write(Paths.get(filePath), content.getBytes());
			logger.info("messages_en_US.properties updated successfully.");

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}

	private void applicationContextUpdater(String tomcatPath) {
		try {
			logger.info("");
			
			String filePath = tomcatPath + File.separator + "webapps\\RestaurantApp\\WEB-INF\\spring\\applicationContext.xml";
			String dbusername = "root";
			String dbHost = "localhost";
			String dbName = InstallerProperties.getProperty("DBName");
			String dbPwd = InstallerProperties.getProperty("DBRootPWD");
			String usernameProperty = String.format("<property name=\"username\" value=\"%s\"/>", dbusername);
			String passwordProperty = String.format("<property name=\"password\" value=\"%s\"/>", dbPwd);

			String dbBaseURL = String.format(
					"value=\"jdbc:mysql://%s:3306/%s?allowPublicKeyRetrieval=true&amp;generateSimpleParameterMetadata=true&amp;useUnicode=true&amp;characterEncoding=utf8&amp;useSSL=false\"/>",
					dbHost, dbName);

			String content = new String(Files.readAllBytes(Paths.get(filePath)));

			content = content.replaceAll("value=\"jdbc:mysql.*", dbBaseURL);
			content = content.replaceAll("<property name=\"username\".*", usernameProperty);
			content = content.replaceAll("<property name=\"password\".*", passwordProperty);

			Files.write(Paths.get(filePath), content.getBytes());
			logger.info("applicationContext.xml updated successfully.");

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}

	private void log4j2Updater(String tomcatPath) {
		try {
			logger.info("");

			String filePath = tomcatPath + File.separator+ "webapps\\RestaurantApp\\WEB-INF\\classes\\log4j2.properties";
			String logFilePath = currentFilePath.replace("\\", "/") + "/logs";

			String content = new String(Files.readAllBytes(Paths.get(filePath)));

			content = content.replaceAll("property.basePath.*", "property.basePath = " + logFilePath);
			Files.write(Paths.get(filePath), content.getBytes());
			logger.info("log4j2 updated successfully.");

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

}
