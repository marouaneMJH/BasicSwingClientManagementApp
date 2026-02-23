package view.panels;

import view.utils.UIThemeManager;
import view.utils.ExportUtil;
import view.utils.ImportUtil;
import view.components.SearchToolbar;
import view.dialogs.ClientFormDialog;
import view.dialogs.CsvImportDialog;
import controller.ClientController;
import dto.ClientDTO;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
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
            "[top]10[grow]10[bottom]"
        ));

        add(searchToolbar, "cell 0 0, growx");

        JScrollPane scrollPane = new JScrollPane(clientTable);
        add(scrollPane, "cell 0 1, grow");

        // Button panel
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0, gap 10", "push[][][][][][]push", ""));
        buttonPanel.setBackground(UIThemeManager.COLOR_BACKGROUND);

        JButton addButton = new JButton("Add");
        addButton.setBackground(UIThemeManager.COLOR_PRIMARY);
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> openAddDialog());

        JButton editButton = new JButton("Edit");
        editButton.setBackground(UIThemeManager.COLOR_SECONDARY);
        editButton.setForeground(Color.WHITE);
        editButton.addActionListener(e -> openEditDialog());

        JButton deleteButton = new JButton("Delete");
        deleteButton.setBackground(UIThemeManager.COLOR_DARK_ACCENT);
        deleteButton.setForeground(Color.WHITE);
        deleteButton.addActionListener(e -> deleteSelected());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(UIThemeManager.COLOR_SECONDARY);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.addActionListener(e -> refresh());

        JButton exportButton = new JButton("Export");
        exportButton.setBackground(UIThemeManager.COLOR_PRIMARY);
        exportButton.setForeground(Color.WHITE);
        exportButton.addActionListener(e -> exportData());

        JButton importButton = new JButton("Import");
        importButton.setBackground(UIThemeManager.COLOR_SUCCESS);
        importButton.setForeground(Color.WHITE);
        importButton.addActionListener(e -> importData());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(importButton);

        add(buttonPanel, "cell 0 2, growx");
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

    /**
     * Open add client dialog.
     */
    private void openAddDialog() {
        ClientFormDialog dialog = new ClientFormDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refresh();
        }
    }

    /**
     * Open edit client dialog.
     */
    private void openEditDialog() {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a client to edit", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int clientId = (int) tableModel.getValueAt(selectedRow, 0);
        ClientDTO client = clientController.getClientDTO(clientId);
        
        if (client != null) {
            ClientFormDialog dialog = new ClientFormDialog((Frame) SwingUtilities.getWindowAncestor(this), client);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refresh();
            }
        }
    }

    /**
     * Delete selected client.
     */
    private void deleteSelected() {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a client to delete", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int clientId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this client?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (clientController.deleteClient(clientId)) {
                JOptionPane.showMessageDialog(this, "Client deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                refresh();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete client", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Export client data to CSV file.
     */
    private void exportData() {
        File file = ExportUtil.showSaveDialog(this, "clients_export_" + System.currentTimeMillis());
        if (file != null) {
            if (ExportUtil.exportClientsToCSV(allClients, file)) {
                JOptionPane.showMessageDialog(this, "Data exported successfully to:\n" + file.getAbsolutePath(), 
                    "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to export data", "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Import client data from CSV file.
     */
    private void importData() {
        CsvImportDialog dialog = new CsvImportDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), 
            CsvImportDialog.ImportType.CLIENTS
        );
        dialog.setVisible(true);
        
        if (dialog.isImportConfirmed()) {
            java.util.List<dto.ClientDTO> importedClients = dialog.getImportedClients();
            int successCount = 0;
            int failCount = 0;
            
            for (dto.ClientDTO client : importedClients) {
                try {
                    clientController.ajouterClient(client);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    System.err.println("Failed to import client: " + e.getMessage());
                }
            }
            
            String message = String.format("Import completed!\nSuccessful: %d\nFailed: %d", successCount, failCount);
            JOptionPane.showMessageDialog(this, message, "Import Result", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        }
    }
}
