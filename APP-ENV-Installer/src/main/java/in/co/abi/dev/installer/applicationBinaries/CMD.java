package in.co.abi.dev.installer.applicationBinaries;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CMD {

	private static final Logger logger = LogManager.getLogger(CMD.class);
	
	private String commandPrompt = "cmd.exe";

	public void runCommand(String command) {
		try {
			logger.info("");
			logger.debug(command);
			ProcessBuilder processBuilder = new ProcessBuilder(commandPrompt, "/c", command);
			processBuilder.redirectErrorStream(true);

			// Start the process
			Process process = processBuilder.start();

			// Read the output from the command
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					logger.debug(line.trim());
				}
			}

			// Wait for the process to complete
			process.waitFor();
		} catch (Exception e) {
			logger.error(e);
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}

	public String runCommandAndGetOutput(String command) {
		String output = new String();
		try {
			logger.info("");
			logger.debug(command);
			ProcessBuilder processBuilder = new ProcessBuilder(commandPrompt, "/c", command);
			processBuilder.redirectErrorStream(true);

			// Start the process
			Process process = processBuilder.start();

			// Read the output from the command
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					logger.debug(line.trim());
					output += line.trim();
				}
			}

			// Wait for the process to complete
			process.waitFor();
		} catch (Exception e) {
			logger.error(e);
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
		return output;
	}

	public void executeCommandInDir(String command, File workingDirectory) {
		try {
			logger.info("");
			logger.debug("command : " + command + " workingDirectory : " + workingDirectory);
			ProcessBuilder processBuilder = new ProcessBuilder(commandPrompt, "/c", command);
			processBuilder.directory(workingDirectory); // Set the working directory

			Process process = processBuilder.start();

			// Reading the output of the command
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				logger.info(line);
			}
			
			process.waitFor();
		} catch (Exception e) {
			logger.error(e);
			JOptionPane.showMessageDialog(null, e.getMessage());
		}

	}

	public void waitForMessageAndStopProcess(String command, String stopMessage) {
		try {
			// Create a ProcessBuilder to run the command
			logger.info("");
			logger.debug(command);
			ProcessBuilder processBuilder = new ProcessBuilder(commandPrompt, "/c", command);
			processBuilder.redirectErrorStream(true);

			// Start the process
			Process process = processBuilder.start();

			// Read the output from the command
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					logger.debug(line.trim());
					if (line.contains(stopMessage)) {
						logger.trace("Detected message: " + stopMessage + ". Stopping process...");
						Thread.sleep(500);
						process.destroyForcibly(); // Kill the process
						runCommand("taskkill /f /im mysqld.exe");

						if (!process.isAlive()) {
							logger.debug("Process was successfully stopped.");
						} else {
							logger.debug("Failed to stop the process.");
						}
						break;
					}
				}
			}

			process.waitFor();

		} catch (Exception e) {
			logger.error(e);
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}

}
