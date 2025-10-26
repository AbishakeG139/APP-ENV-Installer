package in.co.abi.dev.installer.EnvVariable;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.co.abi.dev.installer.applicationBinaries.CMD;
import in.co.abi.dev.installer.applicationBinaries.MySQL;

public class EnvVariableHandler {

	private static final Logger logger = LogManager.getLogger(MySQL.class);

	public boolean setEnvVariable(String variableName, String variablepath) {
		logger.info("");

		boolean completed = false;
		try {
			// Use the setx command to set the environment variable
			Process process = new ProcessBuilder("cmd.exe", "/c",
					"setx " + variableName + " \"" + variablepath + "\" /M").start();

			// Wait for the process to complete
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				logger.info(variableName + " has been set to: " + variablepath);
				completed = true;
			} else {
				logger.error("Failed to set CATALINA_HOME. Exit code: " + exitCode);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return completed;
	}
	
	public boolean addSystemVariablePath(String newPaths) {
	    logger.info("Adding new system variable paths...");

	    boolean completed = false;
	    String existingPath = getSystemPath();

	    if (!existingPath.endsWith(";")) {
	        existingPath += ";";
	    }

	    String finalPath = existingPath;
	    for (String newPath : newPaths.split(";")) {
	        newPath = newPath.trim();
	        if (!existingPath.contains(newPath)) {  // Avoid duplicates
	            finalPath += newPath + ";";
	        }
	    }

	    String output = new CMD().runCommandAndGetOutput(
	        "reg add \"HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment\" /v Path /t REG_EXPAND_SZ /d \"" 
	        + finalPath + "\" /f");

	    if (output.contains("success")) {
	        completed = true;
	    }

	    return completed;
	}

	
	public String getSystemPath() {
	    String pathOutput = new CMD().runCommandAndGetOutput("reg query \"HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment\" /v Path");
	    
	    logger.trace("Existing ENV path: " + pathOutput);

	    String[] listPath = pathOutput.split("\\s{4,}");
	    return (listPath.length > 1) ? listPath[listPath.length - 1] : "";
	}
	
	public void updateJavaPath(String newJavaPath) {
	    logger.info("Updating Java path...");

	    String existingPath = getSystemPath();
	    String[] pathEntries = existingPath.split(";");

	    StringBuilder updatedPath = new StringBuilder();
	    boolean javaPathReplaced = false;

	    for (String path : pathEntries) {
	        path = path.trim();
	        if (!path.isEmpty()) {
	            // Check if the path contains an existing Java directory
	            if (path.toLowerCase().contains("java") && path.toLowerCase().contains("jdk")) {
	                logger.info("Replacing existing Java path: " + path);
	                updatedPath.append(newJavaPath).append(";"); 
	                javaPathReplaced = true;
	            } else {
	                updatedPath.append(path).append(";");
	            }
	        }
	    }

	    // If no existing Java path was found, just append the new Java path
	    if (!javaPathReplaced) {
	        updatedPath.append(newJavaPath).append(";");
	    }

	    String finalPath = updatedPath.toString();
	    if (finalPath.endsWith(";")) {
	        finalPath = finalPath.substring(0, finalPath.length() - 1); // Remove trailing semicolon
	    }

	    String output = new CMD().runCommandAndGetOutput(
	        "reg add \"HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment\" /v Path /t REG_EXPAND_SZ /d \"" 
	        + finalPath + "\" /f");

	    if (output.contains("success")) {
	        logger.info("Java path updated successfully.");
	    } else {
	        logger.error("Failed to update Java path.");
	    }
	}
	
}
