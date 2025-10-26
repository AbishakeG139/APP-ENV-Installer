package in.co.abi.dev.installer.applicationBinaries;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.co.abi.dev.installer.EnvVariable.EnvVariableHandler;
import in.co.abi.dev.installer.fileHandler.FileDownloader;
import in.co.abi.dev.installer.fileHandler.ZipHandler;
import in.co.abi.dev.installer.popups.LoadingPopup;
import in.co.abi.dev.installer.properties.InstallerProperties;

public class Tomcat {

	private static final Logger logger = LogManager.getLogger(Tomcat.class);
	private int winX;
	private String currentFilePath;

	private LoadingPopup loadingPopup;
	CMD cMD = new CMD();

	public Tomcat(int winX, String currentFilePath) {
		this.winX = winX;
		this.currentFilePath = currentFilePath;
	}

	public String installTomcat() {

		String fileURL;
		
		String checkPort = "netstat -ano | findstr :8080";
		String checkPortOutput = new String();
		
		loadingPopup = new LoadingPopup("checking Tomcat server, please wait...");
		logger.info("checking Tomcat server");
		checkPortOutput = cMD.runCommandAndGetOutput(checkPort);
		loadingPopup.close();
		
		if (checkPortOutput != null && !StringUtils.isEmpty(checkPortOutput.trim())) {
			loadingPopup.popupFor2sec("Tomcat (8080 is already in use) already exists skipping new installations");
			logger.info("Tomcat (8080 is already in use) already exists skipping new installations");
			return null;
		} else
			logger.info("No Tomcat installed");
		
		try {
			logger.info("");
			if (winX == 64)
				fileURL = InstallerProperties.getProperty("tomcatW64URL");
			else
				fileURL = InstallerProperties.getProperty("tomcatW32URL");

			loadingPopup = new LoadingPopup("Downloading tomcat, please wait...");
			String fileName = new FileDownloader().downloadFile(fileURL, currentFilePath + File.separator + "tmp");
			loadingPopup.close();

			loadingPopup = new LoadingPopup("Installing tomcat, please wait...");
			String folderName = new ZipHandler()
					.unzip(currentFilePath + File.separator + "tmp" + File.separator + fileName, currentFilePath);
			
			if(StringUtils.isEmpty(folderName)) {
				loadingPopup.close();
				return null;
			}
			
			String tomcatPath = currentFilePath + File.separator + folderName;
			new EnvVariableHandler().setEnvVariable("CATALINA_HOME", tomcatPath);
			installTomcatService(tomcatPath + File.separator + "bin" + File.separator + "service.bat install");
			loadingPopup.close();
			return tomcatPath;
			
		} catch (Exception e) {
			logger.error(e);
			JOptionPane.showMessageDialog(null, e.getMessage());
			return null;
		}
	}

	private void installTomcatService(String serviceBatPath) {
		try {
			logger.info("");
			// Modify the command to separate the script from its arguments
			logger.debug(serviceBatPath);
			String[] command = { "cmd.exe", "/c", serviceBatPath };
			String autoStartCommand = "sc config Tomcat10 start= auto";
			ProcessBuilder processBuilder = new ProcessBuilder(command);

			// Set the working directory correctly
			processBuilder.directory(new File(serviceBatPath).getParentFile());
			processBuilder.redirectErrorStream(true);

			// Start the process
			Process process = processBuilder.start();

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					logger.info(line);
				}
			}

			int exitCode = process.waitFor();
			if (exitCode == 0) {
				cMD.runCommand(autoStartCommand);
				logger.info("Tomcat service installed successfully.");
			} else {
				logger.error("Failed to install Tomcat service. Exit code: " + exitCode);
			}
		} catch (IOException | InterruptedException e) {
			logger.error("Error installing Tomcat service", e);
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}

	public void startTomcatService(String tomcatPath) {
		logger.info("");
		String checkPort = "netstat -ano | findstr :8080";
		String checkPortOutput = new String();
		
		loadingPopup = new LoadingPopup("checking tomcat server, please wait...");
		logger.info("checking tomcat server");
		checkPortOutput = cMD.runCommandAndGetOutput(checkPort);
		loadingPopup.close();
		
		if (checkPortOutput != null && !StringUtils.isEmpty(checkPortOutput.trim())) {
			logger.info("tomcat already exists skipping new installations");
			JOptionPane.showMessageDialog(null, "Port 8080 is already in use /n stop 8080 port /n Run Tomcat10");
			System.exit(0);
		} else
			logger.info("No tomcat installed");
		
		
		loadingPopup = new LoadingPopup("Starting tomcat, please wait...");
		String startCmd = "net start Tomcat10";
		cMD.runCommand(startCmd);
		loadingPopup.close();

		for (int i = 0; i < 10; i++) {
			loadingPopup.popupFor2sec("Testing Tomcat server...");
			checkPortOutput = cMD.runCommandAndGetOutput(checkPort);
			if (checkPortOutput != null && !StringUtils.isEmpty(checkPortOutput.trim()))
				break;
		}

		if (checkPortOutput != null && !StringUtils.isEmpty(checkPortOutput.trim())) {
			logger.info("Tomcat server started successfully.");
			loadingPopup.popupFor2sec("Tomcat server started successfully.");
		} else {
			logger.error("Failed to start Tomcat server.");
			loadingPopup.popupFor2sec("Failed to start Tomcat server.");
			
		}

	}

}
