import java.awt.*;
import java.awt.TrayIcon.MessageType;

// taken from here - https://stackoverflow.com/questions/34490218/how-to-make-a-windows-notification-in-java
public class TrayIconDemo {
    private final String imagePath;
    private final String messageTitle;
    private final String message;

    public TrayIconDemo(String imagePath, String messageTitle, String message) {
        this.imagePath = imagePath;
        this.messageTitle = messageTitle;
        this.message = message;
    }

    public void displayTray() throws AWTException {
        //Obtain only one instance of the SystemTray object
        SystemTray tray = SystemTray.getSystemTray();

        //If the icon is a file
        Image image = Toolkit.getDefaultToolkit().createImage(imagePath);
        //Alternative (if the icon is on the classpath):
        //Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));

        TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
        //Let the system resize the image if needed
        trayIcon.setImageAutoSize(true);
        //Set tooltip text for the tray icon
        trayIcon.setToolTip("System tray icon demo");
        tray.add(trayIcon);

        trayIcon.displayMessage(messageTitle, message, MessageType.INFO);
    }
}
