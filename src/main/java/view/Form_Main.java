package view;

import view.utils.UIThemeManager;
import view.panels.DashboardPanel;
import view.panels.ClientPanel;
import view.panels.CommandePanel;
import view.panels.ProduitPanel;
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

		// Add Dashboard tab (first/home tab)
		DashboardPanel dashboardPanel = new DashboardPanel();
		tabbedPane.addTab("Dashboard", dashboardPanel);

		// Add Client management tab
		ClientPanel clientPanel = new ClientPanel();
		tabbedPane.addTab("Clients", clientPanel);

		// Add Command (Order) management tab
		CommandePanel commandePanel = new CommandePanel();
		tabbedPane.addTab("Orders", commandePanel);

		// Add Product management tab
		ProduitPanel produitPanel = new ProduitPanel();
		tabbedPane.addTab("Products", produitPanel);
	}
}
