package in.co.abi.dev.installer.vct;

import java.io.File;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.co.abi.dev.installer.applicationBinaries.CMD;
import in.co.abi.dev.installer.fileHandler.FileDownloader;
import in.co.abi.dev.installer.fileHandler.ZipHandler;
import in.co.abi.dev.installer.popups.LoadingPopup;
import in.co.abi.dev.installer.properties.InstallerProperties;

public class VCTWorker {

	private static final Logger logger = LogManager.getLogger(VCTWorker.class);

	private String currentFilePath;
	private String sqlTmpPath;
	private String tomcatPath;
	private String backupPath;
	private String nodejsPath;
	private String vctWorkerURL = InstallerProperties.getProperty("vctWorkerURL");

	public VCTWorker(String currentFilePath, String nodejsPath, String backupPath, String tomcatPath,
			String sqlTmpPath) {
		this.currentFilePath = currentFilePath;
		this.sqlTmpPath = sqlTmpPath;
		this.tomcatPath = tomcatPath;
		this.backupPath = backupPath;
		this.nodejsPath = nodejsPath;

	}

	public void installVCTWorker() {
		try {
			logger.info("");
			CMD cMD = new CMD();
			if(StringUtils.isEmpty(tomcatPath)) {
				tomcatPath = System.getenv("CATALINA_HOME");
				logger.info(tomcatPath);
				if(StringUtils.isEmpty(tomcatPath)) {
					JOptionPane.showMessageDialog(null, "CATALINA_HOME environmental variable is not defined correctly");
					return;
				}
			}
			LoadingPopup loadingPopup = new LoadingPopup("Downloading VCT-Worker, please wait...");
			String fileName = new FileDownloader().downloadFile(vctWorkerURL, currentFilePath + File.separator + "tmp");
			Thread.sleep(500);
			loadingPopup.close();

			loadingPopup = new LoadingPopup("Installing VCT-Worker, please wait...");
			String folderName = new ZipHandler().unzip(currentFilePath + File.separator + "tmp" + File.separator + fileName, currentFilePath);
			logger.info("folderName : " + folderName);
			new UpdateVCTConfig().env(currentFilePath + File.separator + folderName + File.separator + ".env",
					backupPath.replace("\\", "\\\\"), tomcatPath.replace("\\", "\\\\"),
					sqlTmpPath.replace("\\", "\\\\"));

			File workingDirectory = new File(currentFilePath + File.separator + folderName);

			String installWinService = "\"" + nodejsPath + "\\node.exe\" " + ".\\VCT-worker-service.js";
			
			cMD.executeCommandInDir(installWinService, workingDirectory);
			loadingPopup.close();

			String checkPort = "netstat -ano | findstr :3000";
			String checkPortOutput = new String();

			for (int i = 0; i < 60; i++) {
				loadingPopup.popupFor2sec("test VCT worker port");
				checkPortOutput = cMD.runCommandAndGetOutput(checkPort);
				if (checkPortOutput != null && !StringUtils.isEmpty(checkPortOutput.trim()))
					break;
			}

			if (checkPortOutput != null && !StringUtils.isEmpty(checkPortOutput.trim())) {
				logger.info("VCT-worker service started successfully.");
				loadingPopup.popupFor2sec("VCT-worker service started successfully.");
			} else {
				logger.error("Failed to start VCT-worker service.");
				JOptionPane.showMessageDialog(null, "Failed to start VCT-worker service.");
			}

		} catch (Exception e) {
			logger.error(e);
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}

}
