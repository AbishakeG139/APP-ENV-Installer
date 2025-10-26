package in.co.abi.dev.installer.applicationBinaries;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.co.abi.dev.installer.fileHandler.FileDownloader;
import in.co.abi.dev.installer.fileHandler.ZipHandler;
import in.co.abi.dev.installer.popups.LoadingPopup;
import in.co.abi.dev.installer.properties.InstallerProperties;

public class Nodejs {

	private static final Logger logger = LogManager.getLogger(Nodejs.class);
	LoadingPopup loadingPopup;
	private String nodeErrorMsg = "unable to install nodejs";

	public String installNode(int winX, String currentFilePath) {

		logger.info("");
		
		String  existingNode = getNodeVersion();
		loadingPopup = new LoadingPopup("checking node js, please wait...");
		if(!StringUtils.isEmpty(existingNode)) {
			String[] nodeVersion = existingNode.replaceAll("[^0-9.]", "").split("\\.");
			int nodeversionInt = Integer.parseInt(nodeVersion[0]);
			if(nodeversionInt > 15 && nodeversionInt < 19) {
				String nodePath = new CMD().runCommandAndGetOutput("where node");
				loadingPopup.close();
				loadingPopup.popupFor2sec("Node already exists skipping new installations");
				logger.info("Node already exists skipping new installations");
				return nodePath.replace("\\node.exe", "");
			}else {
				JOptionPane.showMessageDialog(null, "unable to install nodejs \n needed version 16 \n existing node version " + existingNode);
				logger.error("unable to install nodejs \n needed version 16 \n existing node version " + existingNode);
				System.exit(0);
			}			
		}
		
		loadingPopup.close();

		// URL of the file you want to download
		String fileURL;
		String nodejsPath = new String();
		if (winX == 64)
			fileURL = InstallerProperties.getProperty("nodeJSW64URL");
		else
			fileURL = InstallerProperties.getProperty("nodeJSW32URL");

		String fileName = new String();
		String filePath = new String();

		try {
			loadingPopup = new LoadingPopup("Downloading node js, please wait...");
			fileName = new FileDownloader().downloadFile(fileURL, currentFilePath);
			Thread.sleep(1000);
			loadingPopup.close();

			loadingPopup = new LoadingPopup("Installing node js, please wait...");
			filePath = currentFilePath + File.separator + fileName;

			String folderName = new ZipHandler().unzip(filePath, System.getenv("ProgramFiles"));
			if (StringUtils.isEmpty(folderName)) {
				logger.error(nodeErrorMsg);
				JOptionPane.showMessageDialog(null, nodeErrorMsg);
				return null;
			}
			logger.info("folderName : " + folderName);
			nodejsPath = System.getenv("ProgramFiles") + File.separator + folderName;

			loadingPopup.close();
			Thread.sleep(1000);
			loadingPopup.popupFor2sec("Node successfully installed");
			return nodejsPath;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage());
			JOptionPane.showMessageDialog(null, nodeErrorMsg);
			System.exit(0);
		}

		return nodejsPath;

	}
	
	public static String getNodeVersion() {
		String version = null;
		try {
			Process process = Runtime.getRuntime().exec("node -v");
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			version = reader.readLine();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return version;
	}
	
	
	public static void main(String[] args) {
		System.out.println(
		new Nodejs().installNode(0, ""));
	}

}
