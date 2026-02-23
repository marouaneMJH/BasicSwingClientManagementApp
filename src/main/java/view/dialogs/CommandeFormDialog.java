package view.dialogs;

import bo.Client;
import bo.Commande;
import bo.Ligne_Commande;
import bo.Produit;
import controller.ClientController;
import controller.CommandeController;
import controller.ProduitController;
import dto.ClientDTO;
import dto.CommandeDTO;
import dto.ProduitDTO;
import net.miginfocom.swing.MigLayout;
import view.utils.UIThemeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommandeFormDialog extends JDialog {
    private final CommandeController controller = new CommandeController();
    private final ClientController clientController = new ClientController();
    private final ProduitController produitController = new ProduitController();
    
    private final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
    private final JComboBox<ClientDTO> clientCombo = new JComboBox<>();
    private final JLabel totalLabel = new JLabel("$0.00");
    private final JTable lineItemsTable;
    private final DefaultTableModel lineItemsModel;
    private final JButton saveButton = new JButton("Save");
    private final JButton cancelButton = new JButton("Cancel");
    
    private CommandeDTO currentDto;
    private boolean saved = false;
    private List<ProduitDTO> availableProducts;
    private List<LineItem> orderItems = new ArrayList<>();
    
    // Inner class to hold line item data
    private static class LineItem {
        ProduitDTO product;
        int quantity;
        float subtotal;
        
        LineItem(ProduitDTO product, int quantity) {
            this.product = product;
            this.quantity = quantity;
            this.subtotal = product.getPrix() * quantity;
        }
    }

    public CommandeFormDialog(Frame parent, CommandeDTO dto) {
        super(parent, "Order Form", true);
        this.currentDto = dto;
        
        // Initialize table model
        String[] columnNames = {"Product", "Price", "Available Stock", "Quantity", "Subtotal"};
        lineItemsModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        lineItemsTable = new JTable(lineItemsModel);
        lineItemsTable.setBackground(Color.WHITE);
        lineItemsTable.setRowHeight(25);
        lineItemsTable.getTableHeader().setBackground(UIThemeManager.COLOR_DARK_ACCENT);
        lineItemsTable.getTableHeader().setForeground(Color.WHITE);
        
        initializeUI();
        loadProducts();
        populateFields();
        setSize(800, 600);
    }

    private void initializeUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(getParent());
        setResizable(true);

        JPanel contentPane = new JPanel(new MigLayout("fill, insets 15, gap 10", "[grow]", "[][][][grow][]"));
        contentPane.setBackground(UIThemeManager.COLOR_BACKGROUND);

        // Header section
        JPanel headerPanel = new JPanel(new MigLayout("fill", "[label 100][fill]", "[][]"));
        headerPanel.setBackground(UIThemeManager.COLOR_BACKGROUND);
        
        // Date field
        JLabel dateLabel = new JLabel("Order Date:");
        dateLabel.setForeground(UIThemeManager.COLOR_TEXT);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new Date());
        headerPanel.add(dateLabel);
        headerPanel.add(dateSpinner, "grow, wrap");

        // Client dropdown
        JLabel clientLabel = new JLabel("Client:");
        clientLabel.setForeground(UIThemeManager.COLOR_TEXT);
        loadClients();
        headerPanel.add(clientLabel);
        headerPanel.add(clientCombo, "grow");
        
        contentPane.add(headerPanel, "growx, wrap");
        
        // Line items section
        JLabel itemsLabel = UIThemeManager.createStyledLabel("Order Items:", Font.BOLD);
        contentPane.add(itemsLabel, "wrap");
        
        JScrollPane scrollPane = new JScrollPane(lineItemsTable);
        scrollPane.setPreferredSize(new Dimension(750, 250));
        contentPane.add(scrollPane, "grow, wrap");
        
        // Add/Remove items buttons
        JPanel itemButtonPanel = new JPanel(new MigLayout("insets 0, gap 10", "[][]push[]", ""));
        itemButtonPanel.setBackground(UIThemeManager.COLOR_BACKGROUND);
        
        JButton addItemButton = new JButton("Add Product");
        addItemButton.setBackground(UIThemeManager.COLOR_SUCCESS);
        addItemButton.setForeground(Color.WHITE);
        addItemButton.addActionListener(e -> addLineItem());
        
        JButton removeItemButton = new JButton("Remove Selected");
        removeItemButton.setBackground(UIThemeManager.COLOR_ERROR);
        removeItemButton.setForeground(Color.WHITE);
        removeItemButton.addActionListener(e -> removeLineItem());
        
        JLabel totalTextLabel = new JLabel("Total:");
        totalTextLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        totalTextLabel.setForeground(UIThemeManager.COLOR_TEXT);
        totalLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        totalLabel.setForeground(UIThemeManager.COLOR_SUCCESS);
        
        itemButtonPanel.add(addItemButton);
        itemButtonPanel.add(removeItemButton);
        itemButtonPanel.add(totalTextLabel);
        itemButtonPanel.add(totalLabel);
        
        contentPane.add(itemButtonPanel, "growx, wrap");

        // Save/Cancel buttons
        JPanel buttonPanel = new JPanel(new MigLayout("insets 10 0 0 0, gap 10", "push[][]", ""));
        buttonPanel.setBackground(UIThemeManager.COLOR_BACKGROUND);
        saveButton.setBackground(UIThemeManager.COLOR_PRIMARY);
        saveButton.setForeground(Color.WHITE);
        cancelButton.setBackground(UIThemeManager.COLOR_DARK_ACCENT);
        cancelButton.setForeground(Color.WHITE);

        saveButton.addActionListener(e -> saveCommande());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        contentPane.add(buttonPanel, "growx");

        setContentPane(contentPane);
    }

    private void loadClients() {
        try {
            List<ClientDTO> clients = clientController.getAllClients();
            for (ClientDTO client : clients) {
                clientCombo.addItem(client);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading clients: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadProducts() {
        try {
            availableProducts = produitController.getAllProduits();
            if (availableProducts == null) {
                availableProducts = new ArrayList<>();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            availableProducts = new ArrayList<>();
        }
    }
    
    private void addLineItem() {
        if (availableProducts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No products available", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Show product selection dialog
        JDialog dialog = new JDialog(this, "Add Product", true);
        dialog.setLayout(new MigLayout("fill, insets 15", "[grow]", "[][][]"));
        
        JComboBox<ProduitDTO> productCombo = new JComboBox<>();
        for (ProduitDTO product : availableProducts) {
            if (product.getQtstock() > 0) { // Only show products in stock
                productCombo.addItem(product);
            }
        }
        
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        JLabel stockLabel = new JLabel("Available: 0");
        
        // Update stock label when product changes
        productCombo.addActionListener(e -> {
            ProduitDTO selected = (ProduitDTO) productCombo.getSelectedItem();
            if (selected != null) {
                stockLabel.setText("Available: " + selected.getQtstock());
                ((SpinnerNumberModel) quantitySpinner.getModel()).setMaximum(selected.getQtstock());
            }
        });
        
        // Trigger initial update
        if (productCombo.getItemCount() > 0) {
            productCombo.setSelectedIndex(0);
        }
        
        dialog.add(new JLabel("Product:"));
        dialog.add(productCombo, "grow, wrap");
        dialog.add(new JLabel("Quantity:"));
        dialog.add(quantitySpinner, "split 2");
        dialog.add(stockLabel, "wrap");
        
        JButton okButton = new JButton("Add");
        okButton.setBackground(UIThemeManager.COLOR_PRIMARY);
        okButton.setForeground(Color.WHITE);
        okButton.addActionListener(e -> {
            ProduitDTO selectedProduct = (ProduitDTO) productCombo.getSelectedItem();
            int quantity = (Integer) quantitySpinner.getValue();
            
            if (selectedProduct != null) {
                if (quantity > selectedProduct.getQtstock()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Insufficient stock! Available: " + selectedProduct.getQtstock(), 
                        "Stock Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Add to order items
                LineItem item = new LineItem(selectedProduct, quantity);
                orderItems.add(item);
                updateLineItemsTable();
                dialog.dispose();
            }
        });
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        JPanel btnPanel = new JPanel(new MigLayout("", "push[][]", ""));
        btnPanel.add(okButton);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, "span, grow");
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void removeLineItem() {
        int selectedRow = lineItemsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove", 
                "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        orderItems.remove(selectedRow);
        updateLineItemsTable();
    }
    
    private void updateLineItemsTable() {
        lineItemsModel.setRowCount(0);
        float total = 0;
        
        for (LineItem item : orderItems) {
            Object[] row = {
                item.product.getLibelle(),
                String.format("$%.2f", item.product.getPrix()),
                item.product.getQtstock(),
                item.quantity,
                String.format("$%.2f", item.subtotal)
            };
            lineItemsModel.addRow(row);
            total += item.subtotal;
        }
        
        totalLabel.setText(String.format("$%.2f", total));
    }

    private void populateFields() {
        if (currentDto != null) {
            if (currentDto.getDatecmd() != null) {
                dateSpinner.setValue(currentDto.getDatecmd());
            }
            if (currentDto.getClient() != null) {
                for (int i = 0; i < clientCombo.getItemCount(); i++) {
                    ClientDTO item = clientCombo.getItemAt(i);
                    if (item.getId() == currentDto.getClient().getId()) {
                        clientCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
            // TODO: Load existing line items if editing
        }
    }

    private void saveCommande() {
        if (clientCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a client", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (orderItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add at least one product to the order", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Validate stock availability again before saving
        for (LineItem item : orderItems) {
            ProduitDTO currentProduct = produitController.getProduitDTO(item.product.getId());
            if (currentProduct.getQtstock() < item.quantity) {
                JOptionPane.showMessageDialog(this, 
                    "Insufficient stock for " + item.product.getLibelle() + 
                    "! Available: " + currentProduct.getQtstock(), 
                    "Stock Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try {
            Commande commande = new Commande();
            
            // Only set ID if it's a valid existing order (> 0)
            if (currentDto != null && currentDto.getIdcmd() > 0) {
                commande.setIdcmd(currentDto.getIdcmd());
            }
            
            commande.setDatecmd((Date) dateSpinner.getValue());
            
            // Calculate and set total
            float total = 0;
            for (LineItem item : orderItems) {
                total += item.subtotal;
            }
            commande.setTotal(total);
            
            ClientDTO selectedClient = (ClientDTO) clientCombo.getSelectedItem();
            Client client = new Client();
            client.setId(selectedClient.getId());
            commande.setClient(client);
            
            // Create line items
            List<Ligne_Commande> lignes = new ArrayList<>();
            for (LineItem item : orderItems) {
                Ligne_Commande ligne = new Ligne_Commande();
                Produit product = new Produit();
                product.setId(item.product.getId());
                ligne.setProduit(product);
                ligne.setQuantite(item.quantity);
                ligne.setSous_total(item.subtotal);
                lignes.add(ligne);
            }
            commande.setLignes(lignes);

            controller.saveCommandeEntity(commande);
            
            // Decrease stock for each product
            for (LineItem item : orderItems) {
                ProduitDTO product = produitController.getProduitDTO(item.product.getId());
                product.setQtstock(product.getQtstock() - item.quantity);
                
                Produit produitEntity = new Produit();
                produitEntity.setId(product.getId());
                produitEntity.setLibelle(product.getLibelle());
                produitEntity.setPrix(product.getPrix());
                produitEntity.setQtstock(product.getQtstock());
                
                produitController.saveProduit(produitEntity);
            }
            
            saved = true;
            JOptionPane.showMessageDialog(this, "Order saved successfully!", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving order: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
