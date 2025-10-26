package in.co.abi.dev.installer;

import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.co.abi.dev.installer.EnvVariable.EnvVariableHandler;
import in.co.abi.dev.installer.applicationBinaries.MySQL;
import in.co.abi.dev.installer.applicationBinaries.Nodejs;
import in.co.abi.dev.installer.applicationBinaries.Tomcat;
import in.co.abi.dev.installer.config.WarConfig;
import in.co.abi.dev.installer.fileHandler.CreateNewFolder;
import in.co.abi.dev.installer.fileHandler.DeleteFile;
import in.co.abi.dev.installer.popups.LoadingPopup;
import in.co.abi.dev.installer.vct.VCTWorker;

public class Installer {

	private static String activationKey = new String();
	private static CreateNewFolder createNewFolder = new CreateNewFolder();

	private static String tmpPath;
	private static String vctBackupPath;
	private static final Logger logger = LogManager.getLogger(Installer.class);

	public static void main(String[] args) throws Exception {

		logger.info("");
		setJavaPath();
		String arch = System.getProperty("os.arch");
		File jarFile = new File(Installer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		String currentFilePath = jarFile.getParent();

		int winX = 0;

		logger.info(currentFilePath);

		if (arch.contains("64")) {
			logger.info("The system is 64-bit.");
			winX = 64;
		} else {
			JOptionPane.showMessageDialog(null, "Installer not ready to run in 32-bit system");
			logger.info("Installer not ready to run in 32-bit system");
			winX = 32;
			System.exit(0);
		}

		URL iconURL = Installer.class.getResource("/ABI-DEV.png"); // update with png
		ImageIcon icon;
		if (iconURL == null) {
			logger.warn(iconURL + " Icon not found!");
			icon = null;
		} else {
			icon = new ImageIcon(iconURL);
		}
		
		String userInput = (String) JOptionPane.showInputDialog(null, "Enter activationKey:", "ABI-DEV",
				JOptionPane.QUESTION_MESSAGE, icon, null, "");

		if (userInput != null) {
			logger.info("The activation key entered by the user was successful.");
			activationKey = userInput;
		} else {
			logger.error("No input provided.");
			System.exit(0);
		}
		
		// TODO : validate activationKey

		// tmp
		tmpPath = createNewFolder.createFolderWithRetries(currentFilePath + File.separator + "tmp");
		// vct backup
		vctBackupPath = createNewFolder.createFolderWithRetries(currentFilePath + File.separator + "vctBackup");

		createNewFolder.createFolderWithRetries(currentFilePath + File.separator + "ImageRepository");
		createNewFolder.createFolderWithRetries(currentFilePath + File.separator + "logs");

		logger.info("tmpPath : " + tmpPath);
		logger.info("backup : " + vctBackupPath);

		installTools(winX, currentFilePath);

		LoadingPopup.closeAll();

		// clean downloaded files
		new DeleteFile().deleteContents(currentFilePath + File.separator + "tmp");

		new LoadingPopup().popupFor2sec("Clearing unwanted files, please wait...");

		// stop jar
		System.exit(0);
	}

	public static void installTools(int winX, String currentFilePath) {

		logger.info("");
		new EnvVariableHandler().setEnvVariable("ABI_DEV_ENV", currentFilePath); // replace with env name
		String[] setEnvPathList = new String[3];
		setEnvPathList[0] = new Nodejs().installNode(winX, tmpPath);
		MySQL mySQL = new MySQL(winX, tmpPath);
		setEnvPathList[1] = mySQL.installMySQL();
		Tomcat tomcat = new Tomcat(winX, currentFilePath);
		String tomcatPath = tomcat.installTomcat();
		if (!StringUtils.isEmpty(tomcatPath))
			setEnvPathList[2] = tomcatPath + File.separator + "bin";

		setEnvPath(setEnvPathList);

		new VCTWorker(currentFilePath, setEnvPathList[0], vctBackupPath, tomcatPath, tmpPath).installVCTWorker();
		mySQL.downloadAndRunVanillaDump();

		if (!StringUtils.isEmpty(tomcatPath)) {
			new WarConfig(winX, currentFilePath).configRestaurantAppWar(tomcatPath);
			tomcat.startTomcatService(tomcatPath);
		}

	}

	private static void setJavaPath() {
		logger.info("Setting Java Path...");

		String javaHome = System.getProperty("java.home");
		if (javaHome == null || javaHome.isEmpty()) {
			logger.error("Java home directory not found!");
			return;
		}
		javaHome = javaHome + File.separator + "bin";

		EnvVariableHandler envHandler = new EnvVariableHandler();
		envHandler.updateJavaPath(javaHome);
	}

	private static void setEnvPath(String[] setEnvPathList) {
		try {
			logger.info("Setting environment paths...");

			if (setEnvPathList == null || setEnvPathList.length == 0) {
				logger.warn("No paths provided to set.");
				return;
			}

			// Fetch existing system path
			EnvVariableHandler envHandler = new EnvVariableHandler();
			String existingPath = envHandler.getSystemPath();

			// Build new path string, avoiding duplicates
			StringBuilder setEnvPaths = new StringBuilder();
			for (String setEnvPath : setEnvPathList) {
				logger.debug(setEnvPath);
				if (setEnvPath != null && !setEnvPath.trim().isEmpty() && !setEnvPath.trim().equalsIgnoreCase("null")
						&& !existingPath.contains(setEnvPath.trim())) {

					if (!existingPath.contains(setEnvPath.trim())) {
						if (setEnvPaths.length() > 0)
							setEnvPaths.append(";");
						setEnvPaths.append(setEnvPath.trim());
					}
				}
			}

			if (setEnvPaths.length() > 0) {
				logger.debug("New paths to add: " + setEnvPaths);
				envHandler.addSystemVariablePath(setEnvPaths.toString());
			} else 
				logger.info("No new paths to add.");
			

		} catch (Exception e) {
			logger.error("Error setting environment variables: ", e);
			JOptionPane.showMessageDialog(null, "Can't set environment variables path\n" + e.getMessage());
		}
	}
}
