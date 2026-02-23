package view.panels;

import view.utils.UIThemeManager;
import view.components.SearchToolbar;
import view.dialogs.CommandeFormDialog;
import controller.CommandeController;
import dto.CommandeDTO;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Command (Order) management panel with search and table display.
 * Part of the Phase 2 UI enhancement with Stormy Morning styling.
 */
public class CommandePanel extends JPanel {

    private SearchToolbar searchToolbar;
    private JTable commandeTable;
    private DefaultTableModel tableModel;
    private CommandeController commandeController;
    private List<CommandeDTO> allCommandes;
    private List<CommandeDTO> filteredCommandes;

    public CommandePanel() {
        this.commandeController = new CommandeController();
        initializeComponents();
        setupLayout();
        loadCommandeData();
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
                if (filteredCommandes != null) filteredCommandes = allCommandes;
                updateTable();
            }

            @Override
            public void onRefresh() {
                allCommandes = commandeController.getAllCommandes();
                if (allCommandes == null) allCommandes = List.of();
                filteredCommandes = allCommandes;
                updateTable();
            }
        });

        // Create table
        String[] columnNames = {"ID", "Date", "Client", "Total"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Phase 1: read-only tables
            }
        };

        commandeTable = new JTable(tableModel);
        commandeTable.setBackground(Color.WHITE);
        commandeTable.setForeground(UIThemeManager.COLOR_TEXT);
        commandeTable.setSelectionBackground(UIThemeManager.COLOR_PRIMARY);
        commandeTable.setSelectionForeground(Color.WHITE);
        commandeTable.setRowHeight(25);
        commandeTable.getTableHeader().setBackground(UIThemeManager.COLOR_DARK_ACCENT);
        commandeTable.getTableHeader().setForeground(Color.WHITE);
        commandeTable.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 11));

        // Column widths
        commandeTable.getColumnModel().getColumn(0).setPreferredWidth(40);  // ID
        commandeTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Date
        commandeTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Client
        commandeTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Total

        // Scroll pane for table
        JScrollPane scrollPane = new JScrollPane(commandeTable);
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

        JScrollPane scrollPane = new JScrollPane(commandeTable);
        add(scrollPane, "cell 0 1, grow");

        // Button panel
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0, gap 10", "push[][][][] push", ""));
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

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, "cell 0 2, growx");
    }

    /**
     * Load commande data from controller.
     */
    private void loadCommandeData() {
        try {
            allCommandes = commandeController.getAllCommandes();
            if (allCommandes == null) {
                allCommandes = List.of();
            }
            filteredCommandes = allCommandes;
            updateTable();
        } catch (Exception e) {
            System.err.println("Error loading commandes: " + e.getMessage());
            allCommandes = List.of();
            filteredCommandes = List.of();
            updateTable();
            showErrorMessage("Unable to load orders. Check database connection.");
        }
    }

    /**
     * Update table display with current filtered data.
     */
    private void updateTable() {
        tableModel.setRowCount(0);
        
        if (filteredCommandes == null) {
            filteredCommandes = List.of();
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        for (CommandeDTO commande : filteredCommandes) {
            String clientName = (commande.getClient() != null) ? commande.getClient().getNom() : "N/A";
            String dateStr = (commande.getDatecmd() != null) ? dateFormat.format(commande.getDatecmd()) : "N/A";
            
            Object[] row = {
                commande.getIdcmd(),
                dateStr,
                clientName,
                String.format("%.2f", commande.getTotal())
            };
            tableModel.addRow(row);
        }

        int total = (allCommandes != null) ? allCommandes.size() : 0;
        searchToolbar.setResultCount(filteredCommandes.size(), total);
    }

    /**
     * Perform search on commandes.
     */
    private void performSearch(String query) {
        try {
            if (query == null || query.isEmpty()) {
                filteredCommandes = allCommandes;
            } else {
                filteredCommandes = commandeController.searchCommandes(query);
                if (filteredCommandes == null) {
                    filteredCommandes = List.of();
                }
            }
            updateTable();
        } catch (Exception e) {
            System.err.println("Error searching commandes: " + e.getMessage());
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
     * Refresh commande data from database.
     */
    public void refresh() {
        allCommandes = commandeController.getAllCommandes();
        filteredCommandes = allCommandes;
        updateTable();
        searchToolbar.clearAll();
    }

    /**
     * Open add order dialog.
     */
    private void openAddDialog() {
        CommandeFormDialog dialog = new CommandeFormDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refresh();
        }
    }

    /**
     * Open edit order dialog.
     */
    private void openEditDialog() {
        int selectedRow = commandeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order to edit", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int commandeId = (int) tableModel.getValueAt(selectedRow, 0);
        // Note: CommandeController needs to implement getCommandeDTO method
        CommandeDTO commande = commandeController.getCommandeDTO(commandeId);
        
        if (commande != null) {
            CommandeFormDialog dialog = new CommandeFormDialog((Frame) SwingUtilities.getWindowAncestor(this), commande);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refresh();
            }
        }
    }

    /**
     * Delete selected order.
     */
    private void deleteSelected() {
        int selectedRow = commandeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order to delete", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int commandeId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this order?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (commandeController.deleteCommande(commandeId)) {
                JOptionPane.showMessageDialog(this, "Order deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                refresh();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete order", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
