package in.co.abi.dev.installer.fileHandler;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class FileDownloader {

	@SuppressWarnings("deprecation")
	public String downloadFile(String fileURL, String saveDir) throws IOException {
		String output = new String();
		URL url = new URL(fileURL);
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setRequestMethod("GET");

		// Get the response code to check if the request was successful
		int responseCode = httpConn.getResponseCode();

		if (responseCode == HttpURLConnection.HTTP_OK) {
			// Get the input stream from the connection
			InputStream inputStream = httpConn.getInputStream();
			String fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1);
			String saveFilePath = saveDir + File.separator + fileName;

			// Open an output stream to save the file locally
			try (FileOutputStream outputStream = new FileOutputStream(saveFilePath)) {

				byte[] buffer = new byte[4096];
				int bytesRead;
				// Read the input stream and write to the output stream
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}

				// Close streams
				outputStream.flush();
				outputStream.close();
				inputStream.close();
			}
			output = Paths.get(url.getPath()).getFileName().toString();
		} else {
			popupMessage(fileURL);
		}

		httpConn.disconnect();
		return output;
	}

	private void popupMessage(String urlText) {
		// Create a label for the static text
		JLabel label = new JLabel(
				"<html>Not able to download file.<br>Download the file from the given URL, keep it with the installer EXE, and then press OK.</html>");
		label.setFont(new Font("Arial", Font.PLAIN, 14));

		// Create a JTextArea for the URL that allows selection and copying
		JTextArea urlArea = new JTextArea(urlText);
		urlArea.setFont(new Font("Arial", Font.PLAIN, 14));
		urlArea.setEditable(false); // Make it non-editable but selectable
		urlArea.setBackground(Color.WHITE); // Background color for the URL area
		urlArea.setForeground(Color.BLUE); // Text color to make the URL stand out
		urlArea.setBorder(null); // Remove border for a cleaner look
		urlArea.setCaretColor(Color.BLUE); // Set the caret color to match the text color

		// Optionally, make the URL look like a hyperlink by underlining
		urlArea.setSelectionColor(Color.BLUE); // Color of selected text (URL)
		urlArea.setSelectedTextColor(Color.WHITE); // Text color when selected
		urlArea.setOpaque(true); // Make the background solid for the text area

		// Wrap the URL area in a JScrollPane to allow for scrolling if needed
		JScrollPane scrollPane = new JScrollPane(urlArea);
		scrollPane.setPreferredSize(new java.awt.Dimension(400, 40)); // Adjust height to fit the URL

		// Create a panel to hold the label and scrollable URL
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(label, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);

		// Display the message in a JOptionPane
		JOptionPane.showMessageDialog(null, panel, "Instructions", JOptionPane.INFORMATION_MESSAGE);
	}
}
