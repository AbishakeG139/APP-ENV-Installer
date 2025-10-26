package in.co.abi.dev.installer.applicationBinaries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.co.abi.dev.installer.fileHandler.CreateNewFolder;
import in.co.abi.dev.installer.fileHandler.FileDownloader;
import in.co.abi.dev.installer.fileHandler.ZipHandler;
import in.co.abi.dev.installer.popups.LoadingPopup;
import in.co.abi.dev.installer.properties.InstallerProperties;

public class MySQL {

	private static final Logger logger = LogManager.getLogger(MySQL.class);

	private String defaultMysqlPath = System.getenv("ProgramFiles") + File.separator + "MySQL";
	private String pwdFileName = "mysql-init.txt";
	private String mysqlURL = InstallerProperties.getProperty("mysqlURL");
	private String dbDumpURL = InstallerProperties.getProperty("dbDumpURL");

	private int winX;
	private String currentFilePath;
	private CMD cMD = new CMD();
	LoadingPopup loadingPopup;

	private String dbName = InstallerProperties.getProperty("DBName");
	private String dBbaseURL = InstallerProperties.getProperty("DBbaseURL");
	private String dbusername = "root";
	private String dbPwd = InstallerProperties.getProperty("DBRootPWD");

	private String datadir = "Data";
	private String logErrorPath = "Data/mysql_error.log";

	public MySQL(int winX, String currentFilePath) {
		this.winX = winX;
		this.currentFilePath = currentFilePath;
	}

	public String installMySQL() {
		logger.info("");
		String mysqlBinPath = null;
		try {
			logger.info("");
			String checkPort = "netstat -ano | findstr :3306";
			String checkPortOutput = new String();
			
			loadingPopup = new LoadingPopup("checking mysql server, please wait...");
			logger.info("checking MySQL server");
			checkPortOutput = cMD.runCommandAndGetOutput(checkPort);
			loadingPopup.close();
			
			if (checkPortOutput != null && !StringUtils.isEmpty(checkPortOutput.trim())) {
				logger.info("MySQL already exists skipping new installations");
				return getMysqlPath();
			} else
				logger.info("No MySQL installed");
			
			installVisualCpp();

			loadingPopup = new LoadingPopup("Downloading mysql server, please wait...");
			String fileName = new FileDownloader().downloadFile(mysqlURL, currentFilePath);
			loadingPopup.close();

			loadingPopup = new LoadingPopup("Installing mysql server, please wait...");
			String installPath = new CreateNewFolder().createFolderWithRetries(defaultMysqlPath);
			String mysqlPath = defaultMysqlPath + File.separator + "MySQL-server-8.0.39";
			String folderName = new ZipHandler().unzip(currentFilePath + File.separator + fileName, installPath);
			logger.info("folderName : " + folderName);
			mysqlBinPath = installMySQLSever(mysqlPath);
			loadingPopup.close();
			loadingPopup.popupFor2sec("Testing MySQL server...");

			for (int i = 0; i < 10; i++) {
				loadingPopup.popupFor2sec("Testing MySQL server...");
				checkPortOutput = cMD.runCommandAndGetOutput(checkPort);
				if (checkPortOutput != null && !StringUtils.isEmpty(checkPortOutput.trim()))
					break;
			}

			if (checkPortOutput != null && !StringUtils.isEmpty(checkPortOutput.trim())) {
				logger.info("MySQL server started successfully.");
				loadingPopup.popupFor2sec("MySQL server started successfully.");
			} else {
				logger.error("Failed to start MySQL server.");
				popupMessage("Failed to start MySQL server.");
			}

		} catch (Exception e) {
			logger.error(e);
			popupMessage(e.getMessage());
		}

		return mysqlBinPath;
	}

	public void installVisualCpp() {

		logger.info("");
		String fileURL;

		if (winX == 64)
			fileURL = InstallerProperties.getProperty("vCPPW64URL");
		else
			fileURL = InstallerProperties.getProperty("vCPPW32URL");

		String fileName = new String();
		try {
			loadingPopup = new LoadingPopup("Downloading supporting files for mysql, please wait...");
			fileName = new FileDownloader().downloadFile(fileURL, currentFilePath);
			Thread.sleep(500);
			loadingPopup.close();

			String vCPP = currentFilePath + File.separator + fileName;
			String installCommend = vCPP + " /quiet /install";
			loadingPopup = new LoadingPopup("Installing supporting files for mysql, please wait...");
			cMD.runCommand(installCommend);
			loadingPopup.close();

		} catch (Exception e) {
			logger.error(e);
			popupMessage(e.getMessage());
		}

	}

	public String installMySQLSever(String mysqlPath) {
		logger.info("");
		String mysqlBinPath = "\"" + mysqlPath + File.separator + "bin";
		String mysqlLibPath = "\"" + mysqlPath + File.separator + "lib";
		String initializeCmd = mysqlBinPath + File.separator + "mysqld.exe\" --initialize --console";
		String installCmd = mysqlBinPath + File.separator + "mysqld\" --install mysql8.0";

		String setPwdCmd = mysqlBinPath + File.separator + "mysqld\" --defaults-file=" + mysqlLibPath + File.separator
				+ "my.ini\" --init-file=" + mysqlLibPath + File.separator + pwdFileName + "\" --console";

		configMyini(mysqlPath + File.separator + "lib" + File.separator + "my.ini", mysqlPath);

		cMD.runCommand(initializeCmd);
		cMD.runCommand(installCmd);
		cMD.waitForMessageAndStopProcess(setPwdCmd, "ready for connections. Version: '8.0.39'");

		String startCmd = "net start mysql8.0";
		cMD.runCommand(startCmd);

		return mysqlPath + File.separator + "bin";

	}

	public void popupMessage(String message) {
		JOptionPane.showMessageDialog(null, message);
	}

	public void configMyini(String myiniPath, String mysqlPath) {
		try {
			logger.info("");
			Path path = Paths.get(myiniPath);
			String content = new String(Files.readAllBytes(path));
			String dataDirPath = mysqlPath + File.separator + datadir;
			String errorLogDir = mysqlPath + File.separator + logErrorPath;

			content = content.replaceAll("(?m)^datadir=.*", "datadir=" + dataDirPath.replace("\\", "/"));
			content = content.replaceAll("(?m)^log-error=.*", "log-error=" + errorLogDir.replace("\\", "/"));

			Files.write(path, content.getBytes());
			logger.info("Configuration updated successfully.");
		} catch (IOException e) {
			logger.error("Error updating configuration: " + e.getMessage());
			popupMessage("Error updating configuration: " + e.getMessage());
		}
	}

	public String getMysqlPath(){
		logger.info("");
		loadingPopup = new LoadingPopup("Getting mysql server path, please wait...");
		String query = "select @@basedir;";
		String output = new String();
		try {
			Connection connection = DriverManager.getConnection(dBbaseURL, dbusername, dbPwd);
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			if (resultSet.next()) {
				output = resultSet.getString(1);
			}
			resultSet.close();
			statement.close();
			connection.close();
		} catch (Exception e) {
			logger.error(e.getMessage());
			popupMessage("Unable to get the existing MySQL path. \n Unable to use the ciar default username and password for MySQL. /n Error : " + e.getMessage());
			System.exit(0);
		}
		
		loadingPopup.close();
		return output + "bin";
	}

	public void downloadAndRunVanillaDump() {
		try {
			logger.info("");
			createDatabase();
			loadingPopup = new LoadingPopup("Downloading Vanilla sql file, please wait...");
			String fileName = new FileDownloader().downloadFile(dbDumpURL, currentFilePath);
			loadingPopup.close();
			loadingPopup = new LoadingPopup("Running Vanilla sql file, please wait...");
			String sqlFilePath = currentFilePath + File.separator + fileName;
			String globalQuery = "SET GLOBAL log_bin_trust_function_creators = 1;";
			Connection conn = DriverManager.getConnection(dBbaseURL + dbName, dbusername, dbPwd);
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(globalQuery);
			logger.info("Global setting applied.");
			executeSqlFile(conn, sqlFilePath);
			loadingPopup.close();
			logger.info("SQL file executed successfully.");
		} catch (Exception e) {
			logger.error(e.getMessage());
			popupMessage("Error running Vanilla Dump: " + e.getMessage());
		}
	}
	
	public void createDatabase() {
		try {
			logger.info("");
			Connection conn = DriverManager.getConnection(dBbaseURL, dbusername, dbPwd);
            Statement stmt = conn.createStatement();

            String sql = "CREATE DATABASE IF NOT EXISTS " + dbName;
            stmt.executeUpdate(sql);
            logger.debug("Database checked/created successfully.");

            stmt.close();
            conn.close();
		}catch (Exception e) {
			logger.error(e.getMessage());
			popupMessage("Not able to create Database : " + e.getMessage());
			System.exit(0);
			
		}
		
	}
	
	private static void executeSqlFile(Connection connection, String sqlFilePath) {
		logger.info("");
        try (BufferedReader br = new BufferedReader(new FileReader(sqlFilePath))) {
            StringBuilder sql = new StringBuilder();
            String line;
            int lineNumber = 0;
            boolean isMultiLineStatement = false;
            String customDelimiter = ";"; 
            while ((line = br.readLine()) != null) {
                lineNumber++;
               // logger.debug("Reading line " + lineNumber + ": " + line);

                if (line.trim().toUpperCase().startsWith("DELIMITER")) {
                    customDelimiter = line.split("\\s+")[1];
                    isMultiLineStatement = !customDelimiter.equals(";");
                    continue;
                }

                sql.append(line).append("\n");
                if (isMultiLineStatement && line.trim().endsWith(customDelimiter)) {
                    sql.setLength(sql.length() - customDelimiter.length());
                    try (Statement statement = connection.createStatement()) {
                        statement.execute(sql.toString());
                        logger.debug("Executed multi-line statement ending at line " + lineNumber);
                    }
                    sql = new StringBuilder(); 
                    isMultiLineStatement = false;
                    customDelimiter = ";"; 
                } else if (!isMultiLineStatement && line.trim().endsWith(";")) {
                    try (Statement statement = connection.createStatement()) {
                        statement.execute(sql.toString());
                        logger.debug("Executed line " + lineNumber);
                    }
                    sql = new StringBuilder(); 
                }
            }

            logger.info("SQL file imported successfully.");
        } catch (Exception e) {
        	logger.error(e.getMessage());
        }
    }
	
}
