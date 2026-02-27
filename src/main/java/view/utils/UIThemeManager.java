package view.utils;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;

/**
 * Manages UI theme and styling for the application.
 * Configures FlatLaf with Stormy Morning color palette.
 */
public class UIThemeManager {

    // Stormy Morning Color Palette
    public static final Color COLOR_PRIMARY = new Color(136, 189, 242);      // #88BDF2
    public static final Color COLOR_DARK_ACCENT = new Color(56, 73, 89);     // #384959
    public static final Color COLOR_SECONDARY = new Color(106, 137, 167);    // #6A89A7
    public static final Color COLOR_BACKGROUND = new Color(189, 221, 252);   // #BDDDFC
    public static final Color COLOR_TEXT = new Color(56, 73, 89);            // #384959
    public static final Color COLOR_SUCCESS = new Color(76, 175, 80);        // #4CAF50
    public static final Color COLOR_WARNING = new Color(255, 152, 0);        // #FF9800
    public static final Color COLOR_ERROR = new Color(244, 67, 54);          // #F44336

    /**
     * Initialize and apply the FlatLaf theme with Stormy Morning colors.
     */
    public static void initializeTheme() {
        try {
            // Set FlatLaf as the look and feel
            UIManager.setLookAndFeel(new FlatLightLaf());

            // Apply Stormy Morning color scheme
            UIManager.put("Component.focusColor", COLOR_PRIMARY);
            UIManager.put("Button.background", COLOR_PRIMARY);
            UIManager.put("Button.focusedBackground", COLOR_SECONDARY);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.border", new javax.swing.border.LineBorder(COLOR_SECONDARY, 1, true));

            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("TextField.foreground", COLOR_TEXT);
            UIManager.put("TextField.caretForeground", COLOR_PRIMARY);
            UIManager.put("TextField.borderColor", COLOR_SECONDARY);
            UIManager.put("TextField.focusedBorderColor", COLOR_PRIMARY);

            UIManager.put("Table.background", Color.WHITE);
            UIManager.put("Table.foreground", COLOR_TEXT);
            UIManager.put("Table.alternateRowColor", COLOR_BACKGROUND);
            UIManager.put("Table.selectionBackground", COLOR_PRIMARY);
            UIManager.put("Table.selectionForeground", Color.WHITE);
            UIManager.put("Table.gridColor", COLOR_SECONDARY);

            UIManager.put("TabbedPane.selectedBackground", COLOR_PRIMARY);
            UIManager.put("TabbedPane.selectedForeground", Color.WHITE);
            UIManager.put("TabbedPane.background", COLOR_BACKGROUND);
            UIManager.put("TabbedPane.foreground", COLOR_TEXT);

            UIManager.put("Panel.background", COLOR_BACKGROUND);
            UIManager.put("Label.foreground", COLOR_TEXT);

            UIManager.put("ComboBox.background", Color.WHITE);
            UIManager.put("ComboBox.foreground", COLOR_TEXT);
            UIManager.put("ComboBox.buttonBackground", COLOR_SECONDARY);

        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Failed to set FlatLaf theme: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get a styled button with Stormy Morning colors.
     */
    public static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(COLOR_PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Dialog", Font.PLAIN, 11));
        return button;
    }

    /**
     * Get a styled label with dark text.
     */
    public static JLabel createStyledLabel(String text, int style) {
        JLabel label = new JLabel(text);
        label.setForeground(COLOR_TEXT);
        label.setFont(new Font("Dialog", style, 11));
        return label;
    }

    /**
     * Get a styled header label.
     */
    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(COLOR_DARK_ACCENT);
        label.setFont(new Font("Dialog", Font.BOLD, 16));
        return label;
    }
}
