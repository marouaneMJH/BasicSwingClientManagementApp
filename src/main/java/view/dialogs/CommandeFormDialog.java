package view.dialogs;

import bo.Client;
import bo.Commande;
import controller.ClientController;
import controller.CommandeController;
import dto.ClientDTO;
import dto.CommandeDTO;
import net.miginfocom.swing.MigLayout;
import view.utils.UIThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;

public class CommandeFormDialog extends JDialog {
    private final CommandeController controller = new CommandeController();
    private final ClientController clientController = new ClientController();
    private final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
    private final JComboBox<ClientDTO> clientCombo = new JComboBox<>();
    private final JButton saveButton = new JButton("Save");
    private final JButton cancelButton = new JButton("Cancel");
    private CommandeDTO currentDto;
    private boolean saved = false;

    public CommandeFormDialog(Frame parent, CommandeDTO dto) {
        super(parent, "Order Form", true);
        this.currentDto = dto;
        initializeUI();
        populateFields();
    }

    private void initializeUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(getParent());
        setResizable(true);

        JPanel contentPane = new JPanel(new MigLayout("insets 15, gap 10", "[label 100][fill]", "[]10"));
        
        // Apply theme using static constants
        contentPane.setBackground(UIThemeManager.COLOR_BACKGROUND);
        contentPane.setForeground(UIThemeManager.COLOR_TEXT);

        // Date field
        JLabel dateLabel = new JLabel("Order Date:");
        dateLabel.setForeground(UIThemeManager.COLOR_TEXT);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new Date());
        contentPane.add(dateLabel);
        contentPane.add(dateSpinner, "grow, wrap");

        // Client dropdown
        JLabel clientLabel = new JLabel("Client:");
        clientLabel.setForeground(UIThemeManager.COLOR_TEXT);
        loadClients();
        contentPane.add(clientLabel);
        contentPane.add(clientCombo, "grow, wrap");

        // Buttons
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0, gap 10", "push[][]push", ""));
        buttonPanel.setBackground(UIThemeManager.COLOR_BACKGROUND);
        saveButton.setBackground(UIThemeManager.COLOR_PRIMARY);
        saveButton.setForeground(Color.WHITE);
        cancelButton.setBackground(UIThemeManager.COLOR_DARK_ACCENT);
        cancelButton.setForeground(Color.WHITE);

        saveButton.addActionListener(e -> saveCommande());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        contentPane.add(buttonPanel, "span, grow, wrap");

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
        }
    }

    private void saveCommande() {
        if (clientCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a client", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Commande commande = new Commande();
            
            // Only set ID if it's a valid existing order (> 0)
            if (currentDto != null && currentDto.getIdcmd() > 0) {
                commande.setIdcmd(currentDto.getIdcmd());
            }
            
            commande.setDatecmd((Date) dateSpinner.getValue());
            
            ClientDTO selectedClient = (ClientDTO) clientCombo.getSelectedItem();
            Client client = new Client();
            client.setId(selectedClient.getId());
            commande.setClient(client);

            controller.saveCommandeEntity(commande);
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving order: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
