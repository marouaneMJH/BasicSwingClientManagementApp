package view.dialogs;

import bo.Client;
import controller.ClientController;
import dto.ClientDTO;
import net.miginfocom.swing.MigLayout;
import view.utils.UIThemeManager;

import javax.swing.*;
import java.awt.*;

public class ClientFormDialog extends JDialog {
    private final ClientController controller = new ClientController();
    private final JTextField nomField = new JTextField(20);
    private final JTextField capitalField = new JTextField(20);
    private final JTextField adresseField = new JTextField(20);
    private final JButton saveButton = new JButton("Save");
    private final JButton cancelButton = new JButton("Cancel");
    private ClientDTO currentDto;
    private boolean saved = false;

    public ClientFormDialog(Frame parent, ClientDTO dto) {
        super(parent, "Client Form", true);
        this.currentDto = dto;
        initializeUI();
        populateFields();
    }

    private void initializeUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 280);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel contentPane = new JPanel(new MigLayout("insets 15, gap 10", "[label 100][fill]", "[]10"));
        
        // Apply theme using static constants
        contentPane.setBackground(UIThemeManager.COLOR_BACKGROUND);
        contentPane.setForeground(UIThemeManager.COLOR_TEXT);

        // Form fields - only nom, capital, adresse
        addFormField(contentPane, "Name:", nomField);
        addFormField(contentPane, "Capital:", capitalField);
        addFormField(contentPane, "Address:", adresseField);

        // Buttons
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0, gap 10", "push[][]push", ""));
        buttonPanel.setBackground(UIThemeManager.COLOR_BACKGROUND);
        saveButton.setBackground(UIThemeManager.COLOR_PRIMARY);
        saveButton.setForeground(Color.WHITE);
        cancelButton.setBackground(UIThemeManager.COLOR_DARK_ACCENT);
        cancelButton.setForeground(Color.WHITE);

        saveButton.addActionListener(e -> saveClient());
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
            nomField.setText(currentDto.getNom() != null ? currentDto.getNom() : "");
            capitalField.setText(String.valueOf(currentDto.getCapital()));
            adresseField.setText(currentDto.getAdresse() != null ? currentDto.getAdresse() : "");
        }
    }

    private void saveClient() {
        if (!validateFields()) {
            return;
        }

        try {
            Client client = new Client();
            if (currentDto != null && currentDto.getId() > 0) {
                client.setId(currentDto.getId());
            }
            client.setNom(nomField.getText().trim());
            client.setCapital(Double.parseDouble(capitalField.getText().trim()));
            client.setAdresse(adresseField.getText().trim());

            controller.saveClient(client);
            saved = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Capital must be a valid number",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving client: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateFields() {
        if (nomField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (capitalField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Capital is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (adresseField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Address is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            Double.parseDouble(capitalField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Capital must be a valid number",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean isSaved() {
        return saved;
    }
}
