package view;

import view.utils.UIThemeManager;
import view.panels.ClientPanel;
import java.awt.EventQueue;
import javax.swing.*;

/**
 * Main application window with tabbed interface.
 * Phase 1 UI Enhancement with Stormy Morning styling and search/filter capabilities.
 */
public class Form_Main extends JFrame {

	private static final long serialVersionUID = 1L;
	private JTabbedPane tabbedPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// Initialize FlatLaf theme
					UIThemeManager.initializeTheme();
					
					Form_Main frame = new Form_Main();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the main application frame.
	 */
	public Form_Main() {
		setTitle("Gestion Commerciale - Commercial Management System");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1200, 700);
		setLocationRelativeTo(null);
		setExtendedState(JFrame.MAXIMIZED_BOTH);

		// Create tabbed pane
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		setContentPane(tabbedPane);

		// Add Client management tab
		ClientPanel clientPanel = new ClientPanel();
		tabbedPane.addTab("Clients", clientPanel);

		// Add Command management tab (placeholder for now)
		JPanel commandePanel = new JPanel();
		commandePanel.add(new JLabel("Commandes Management (Coming Soon)"));
		tabbedPane.addTab("Orders", commandePanel);

		// Add Product management tab (placeholder for now)
		JPanel produitPanel = new JPanel();
		produitPanel.add(new JLabel("Products Management (Coming Soon)"));
		tabbedPane.addTab("Products", produitPanel);

		// Add exit option
		JPanel exitPanel = new JPanel();
		exitPanel.add(new JLabel("Application Controls"));
		tabbedPane.addTab("Exit", exitPanel);
	}
}
