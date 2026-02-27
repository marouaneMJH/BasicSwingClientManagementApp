package view.dialogs;

import bo.Produit;
import controller.ProduitController;
import dto.ProduitDTO;
import net.miginfocom.swing.MigLayout;
import view.utils.UIThemeManager;

import javax.swing.*;
import java.awt.*;

public class ProduitFormDialog extends JDialog {
    private final ProduitController controller = new ProduitController();
    private final JTextField libelleField = new JTextField(20);
    private final JTextField prixField = new JTextField(20);
    private final JTextField stockField = new JTextField(20);
    private final JButton saveButton = new JButton("Save");
    private final JButton cancelButton = new JButton("Cancel");
    private ProduitDTO currentDto;
    private boolean saved = false;

    public ProduitFormDialog(Frame parent, ProduitDTO dto) {
        super(parent, "Product Form", true);
        this.currentDto = dto;
        initializeUI();
        populateFields();
    }

    private void initializeUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel contentPane = new JPanel(new MigLayout("insets 15, gap 10", "[label 100][fill]", "[]10"));
        
        // Apply theme using static constants
        contentPane.setBackground(UIThemeManager.COLOR_BACKGROUND);
        contentPane.setForeground(UIThemeManager.COLOR_TEXT);

        // Form fields
        addFormField(contentPane, "Name:", libelleField);
        addFormField(contentPane, "Price:", prixField);
        addFormField(contentPane, "Stock:", stockField);

        // Buttons
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0, gap 10", "push[][]push", ""));
        buttonPanel.setBackground(UIThemeManager.COLOR_BACKGROUND);
        saveButton.setBackground(UIThemeManager.COLOR_PRIMARY);
        saveButton.setForeground(Color.WHITE);
        cancelButton.setBackground(UIThemeManager.COLOR_DARK_ACCENT);
        cancelButton.setForeground(Color.WHITE);

        saveButton.addActionListener(e -> saveProduit());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        contentPane.add(buttonPanel, "span, grow, wrap");

        setContentPane(contentPane);
    }

    private void addFormField(JPanel panel, String label, JTextField field) {
        JLabel jLabel = new JLabel(label);
        jLabel.setForeground(UIThemeManager.COLOR_TEXT);
        field.setBackground(Color.WHITE);
        field.setForeground(UIThemeManager.COLOR_TEXT);
        field.setCaretColor(UIThemeManager.COLOR_TEXT);
        panel.add(jLabel);
        panel.add(field, "grow, wrap");
    }

    private void populateFields() {
        if (currentDto != null) {
            libelleField.setText(currentDto.getLibelle() != null ? currentDto.getLibelle() : "");
            prixField.setText(String.valueOf(currentDto.getPrix()));
            stockField.setText(String.valueOf(currentDto.getQtstock()));
        }
    }

    private void saveProduit() {
        if (!validateFields()) {
            return;
        }

        try {
            Produit produit = new Produit();
            if (currentDto != null && currentDto.getId() > 0) {
                produit.setId(currentDto.getId());
            }
            produit.setLibelle(libelleField.getText().trim());
            produit.setPrix(Float.parseFloat(prixField.getText().trim()));
            produit.setQtstock(Integer.parseInt(stockField.getText().trim()));

            controller.saveProduit(produit);
            saved = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price and Stock must be valid numbers",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving product: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateFields() {
        if (libelleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (prixField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Price is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (stockField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Stock is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            Float.parseFloat(prixField.getText().trim());
            Integer.parseInt(stockField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price and Stock must be valid numbers",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean isSaved() {
        return saved;
    }
}
