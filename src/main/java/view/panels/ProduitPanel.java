package view.panels;

import view.utils.UIThemeManager;
import view.utils.ExportUtil;
import view.utils.ImportUtil;
import view.components.SearchToolbar;
import view.dialogs.ProduitFormDialog;
import view.dialogs.CsvImportDialog;
import controller.ProduitController;
import dto.ProduitDTO;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Product management panel with search and table display.
 * Part of the Phase 2 UI enhancement with Stormy Morning styling.
 */
public class ProduitPanel extends JPanel {

    private SearchToolbar searchToolbar;
    private JTable produitTable;
    private DefaultTableModel tableModel;
    private ProduitController produitController;
    private List<ProduitDTO> allProduits;
    private List<ProduitDTO> filteredProduits;

    public ProduitPanel() {
        this.produitController = new ProduitController();
        initializeComponents();
        setupLayout();
        loadProduitData();
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
                if (filteredProduits != null) filteredProduits = allProduits;
                updateTable();
            }

            @Override
            public void onRefresh() {
                allProduits = produitController.getAllProduits();
                if (allProduits == null) allProduits = List.of();
                filteredProduits = allProduits;
                updateTable();
            }
        });

        // Create table
        String[] columnNames = {"ID", "Name", "Price", "Stock"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Phase 1: read-only tables
            }
        };

        produitTable = new JTable(tableModel);
        produitTable.setBackground(Color.WHITE);
        produitTable.setForeground(UIThemeManager.COLOR_TEXT);
        produitTable.setSelectionBackground(UIThemeManager.COLOR_PRIMARY);
        produitTable.setSelectionForeground(Color.WHITE);
        produitTable.setRowHeight(25);
        produitTable.getTableHeader().setBackground(UIThemeManager.COLOR_DARK_ACCENT);
        produitTable.getTableHeader().setForeground(Color.WHITE);
        produitTable.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 11));

        // Column widths
        produitTable.getColumnModel().getColumn(0).setPreferredWidth(40);  // ID
        produitTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Name
        produitTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Price
        produitTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Stock

        // Scroll pane for table
        JScrollPane scrollPane = new JScrollPane(produitTable);
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

        JScrollPane scrollPane = new JScrollPane(produitTable);
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
     * Load produit data from controller.
     */
    private void loadProduitData() {
        try {
            allProduits = produitController.getAllProduits();
            if (allProduits == null) {
                allProduits = List.of();
            }
            filteredProduits = allProduits;
            updateTable();
        } catch (Exception e) {
            System.err.println("Error loading produits: " + e.getMessage());
            allProduits = List.of();
            filteredProduits = List.of();
            updateTable();
            showErrorMessage("Unable to load products. Check database connection.");
        }
    }

    /**
     * Update table display with current filtered data.
     */
    private void updateTable() {
        tableModel.setRowCount(0);
        
        if (filteredProduits == null) {
            filteredProduits = List.of();
        }
        
        for (ProduitDTO produit : filteredProduits) {
            Object[] row = {
                produit.getId(),
                produit.getLibelle(),
                String.format("%.2f", produit.getPrix()),
                produit.getQtstock()
            };
            tableModel.addRow(row);
        }

        int total = (allProduits != null) ? allProduits.size() : 0;
        searchToolbar.setResultCount(filteredProduits.size(), total);
    }

    /**
     * Perform search on produits.
     */
    private void performSearch(String query) {
        try {
            if (query == null || query.isEmpty()) {
                filteredProduits = allProduits;
            } else {
                filteredProduits = produitController.searchProduits(query);
                if (filteredProduits == null) {
                    filteredProduits = List.of();
                }
            }
            updateTable();
        } catch (Exception e) {
            System.err.println("Error searching produits: " + e.getMessage());
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
     * Refresh produit data from database.
     */
    public void refresh() {
        allProduits = produitController.getAllProduits();
        filteredProduits = allProduits;
        updateTable();
        searchToolbar.clearAll();
    }

    /**
     * Open add product dialog.
     */
    private void openAddDialog() {
        ProduitFormDialog dialog = new ProduitFormDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refresh();
        }
    }

    /**
     * Open edit product dialog.
     */
    private void openEditDialog() {
        int selectedRow = produitTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int produitId = (int) tableModel.getValueAt(selectedRow, 0);
        ProduitDTO produit = produitController.getProduitDTO(produitId);
        
        if (produit != null) {
            ProduitFormDialog dialog = new ProduitFormDialog((Frame) SwingUtilities.getWindowAncestor(this), produit);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refresh();
            }
        }
    }

    /**
     * Delete selected product.
     */
    private void deleteSelected() {
        int selectedRow = produitTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int produitId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this product?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (produitController.deleteProduit(produitId)) {
                JOptionPane.showMessageDialog(this, "Product deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                refresh();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete product", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Export produit data to CSV file.
     */
    private void exportData() {
        File file = ExportUtil.showSaveDialog(this, "products_export_" + System.currentTimeMillis());
        if (file != null) {
            if (ExportUtil.exportProductsToCSV(allProduits, file)) {
                JOptionPane.showMessageDialog(this, "Data exported successfully to:\n" + file.getAbsolutePath(), 
                    "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to export data", "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Import product data from CSV file.
     */
    private void importData() {
        CsvImportDialog dialog = new CsvImportDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), 
            CsvImportDialog.ImportType.PRODUCTS
        );
        dialog.setVisible(true);
        
        if (dialog.isImportConfirmed()) {
            java.util.List<dto.ProduitDTO> importedProducts = dialog.getImportedProducts();
            int successCount = 0;
            int failCount = 0;
            
            for (dto.ProduitDTO produit : importedProducts) {
                try {
                    produitController.ajouterProduit(produit);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    System.err.println("Failed to import product: " + e.getMessage());
                }
            }
            
            String message = String.format("Import completed!\nSuccessful: %d\nFailed: %d", successCount, failCount);
            JOptionPane.showMessageDialog(this, message, "Import Result", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        }
    }
}
