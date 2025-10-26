package in.co.abi.dev.installer.popups;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.SwingConstants;

public class LoadingPopup {
	
	private static final List<LoadingPopup> popups = new ArrayList<>();
    private JFrame frame;

    public LoadingPopup() {}
    
    public LoadingPopup(String message) {
        frame = new JFrame("Loading");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(350, 100);
        frame.setLocationRelativeTo(null);

        JLabel label = new JLabel(message, SwingConstants.CENTER);
        frame.getContentPane().add(label, BorderLayout.CENTER);

        frame.setUndecorated(true);
        frame.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        frame.setVisible(true);
        
     // Add this instance to the list
        synchronized (popups) {
            popups.add(this);
        }
    }

    public void close() {
        frame.dispose();
        closeAll();
    }
    
    public static void closeAll() {
        synchronized (popups) {
            for (LoadingPopup popup : new ArrayList<>(popups)) {
                popup.frame.dispose();
            }
            popups.clear();
        }
    }
    
    public void popupFor2sec(String message) {
        LoadingPopup loadingPopup = new LoadingPopup(message);
        try {
            Thread.sleep(2000); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        loadingPopup.close();
    }
    
}
