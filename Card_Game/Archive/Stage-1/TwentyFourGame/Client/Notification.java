package TwentyFourGame.Client;

import javax.swing.*;
import java.awt.*;

// TODO: Refactor a bit
public class Notification {

    private static final int ICON_SIZE = 32;
    
    public static void showError(String errMsg, JFrame parent) {
        ImageIcon icon = new ImageIcon("TwentyFourGame/icons/error.png");
        Image image = icon.getImage();
        Icon customIcon;
        if (image == null || image.getWidth(null) < 0) {
            customIcon = UIManager.getIcon("OptionPane.errorIcon");
        } else {
            image = image.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
            customIcon = new ImageIcon(image);
        }
        JOptionPane pane = new JOptionPane(errMsg, JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, customIcon);
        JDialog dialog = pane.createDialog(parent, "Error");
        dialog.setModal(true);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
    }

    public static void showConfirm(String errMsg, JFrame parent) {
        ImageIcon icon = new ImageIcon("TwentyFourGame/icons/info.png");
        Image image = icon.getImage();
        Icon customIcon;
        if (image == null || image.getWidth(null) < 0) {
            customIcon = UIManager.getIcon("OptionPane.informationIcon");
        } else {
            image = image.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
            customIcon = new ImageIcon(image);
        }
        JOptionPane pane = new JOptionPane(errMsg, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, customIcon);
        JDialog dialog = pane.createDialog(parent, "Confirm");
        dialog.setModal(true);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
    }
    //     JOptionPane pane = new JOptionPane(errMsg, JOptionPane.INFORMATION_MESSAGE);
    //     JDialog dialog = pane.createDialog(parent, "Confirm");
    //     dialog.setModal(true);
    //     dialog.setAlwaysOnTop(true);
    //     dialog.setVisible(true);
    // }
}
