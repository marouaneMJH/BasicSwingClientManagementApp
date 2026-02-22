package view.panels;

import view.utils.UIThemeManager;
import view.components.SearchToolbar;
import controller.ClientController;
import dto.ClientDTO;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Client management panel with search and table display.
 * Part of the Phase 1 UI enhancement with Stormy Morning styling.
 */
public class ClientPanel extends JPanel {

    private SearchToolbar searchToolbar;
    private JTable clientTable;
    private DefaultTableModel tableModel;
    private ClientController clientController;
    private List<ClientDTO> allClients;
    private List<ClientDTO> filteredClients;

    public ClientPanel() {
        this.clientController = new ClientController();
        initializeComponents();
        setupLayout();
        loadClientData();
    }

    /**
     * Initialize UI components.
     */
    private void initializeComponents() {
        setBackground(UIThemeManager.COLOR_BACKGROUND);

        // Create search toolbar
        searchToolbar = new SearchToolbar();
        searchToolbar.setSearchListener(new SearchToolbar.SearchListener() {
            @Override
            public void onSearch(String query, List<String> filterValues) {
                performSearch(query);
            }

            @Override
            public void onClear() {
                if (filteredClients != null) filteredClients = allClients;
                updateTable();
            }

            @Override
            public void onRefresh() {
                allClients = clientController.getAllClients();
                if (allClients == null) allClients = List.of();
                filteredClients = allClients;
                updateTable();
            }
        });

        // Create table
        String[] columnNames = {"ID", "Name", "Capital", "Address"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Phase 1: read-only tables
            }
        };

        clientTable = new JTable(tableModel);
        clientTable.setBackground(Color.WHITE);
        clientTable.setForeground(UIThemeManager.COLOR_TEXT);
        clientTable.setSelectionBackground(UIThemeManager.COLOR_PRIMARY);
        clientTable.setSelectionForeground(Color.WHITE);
        clientTable.setRowHeight(25);
        clientTable.getTableHeader().setBackground(UIThemeManager.COLOR_DARK_ACCENT);
        clientTable.getTableHeader().setForeground(Color.WHITE);
        clientTable.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 11));

        // Column widths
        clientTable.getColumnModel().getColumn(0).setPreferredWidth(40);  // ID
        clientTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Name
        clientTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Capital
        clientTable.getColumnModel().getColumn(3).setPreferredWidth(200); // Address

        // Scroll pane for table
        JScrollPane scrollPane = new JScrollPane(clientTable);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
    }

    /**
     * Setup layout using MigLayout.
     */
    private void setupLayout() {
        setLayout(new MigLayout(
            "fill, insets 10",
            "[grow]",
            "[top]10[grow]"
        ));

        add(searchToolbar, "cell 0 0, growx");

        JScrollPane scrollPane = new JScrollPane(clientTable);
        add(scrollPane, "cell 0 1, grow");
    }

    /**
     * Load client data from controller.
     */
    private void loadClientData() {
        try {
            allClients = clientController.getAllClients();
            if (allClients == null) {
                allClients = List.of();
            }
            filteredClients = allClients;
            updateTable();
        } catch (Exception e) {
            System.err.println("Error loading clients: " + e.getMessage());
            allClients = List.of();
            filteredClients = List.of();
            updateTable();
            showErrorMessage("Unable to load clients. Check database connection.");
        }
    }

    /**
     * Update table display with current filtered data.
     */
    private void updateTable() {
        tableModel.setRowCount(0);
        
        if (filteredClients == null) {
            filteredClients = List.of();
        }
        
        for (ClientDTO client : filteredClients) {
            Object[] row = {
                client.getId(),
                client.getNom(),
                String.format("%.2f", client.getCapital()),
                client.getAdresse()
            };
            tableModel.addRow(row);
        }

        int total = (allClients != null) ? allClients.size() : 0;
        searchToolbar.setResultCount(filteredClients.size(), total);
    }

    /**
     * Perform search on clients.
     */
    private void performSearch(String query) {
        try {
            if (query == null || query.isEmpty()) {
                filteredClients = allClients;
            } else {
                filteredClients = clientController.searchClients(query);
                if (filteredClients == null) {
                    filteredClients = List.of();
                }
            }
            updateTable();
        } catch (Exception e) {
            System.err.println("Error searching clients: " + e.getMessage());
            showErrorMessage("Search failed: " + e.getMessage());
        }
    }

    /**
     * Show error dialog to user.
     */
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Refresh client data from database.
     */
    public void refresh() {
        allClients = clientController.getAllClients();
        filteredClients = allClients;
        updateTable();
        searchToolbar.clearAll();
    }
}
