package view.dialogs;

import view.utils.ImportUtil;
import view.utils.ImportUtil.ImportError;
import view.utils.ImportUtil.ImportResult;
import view.utils.UIThemeManager;
import dto.ClientDTO;
import dto.ProduitDTO;
import dto.CommandeDTO;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Dialog for importing data from CSV files with preview and validation.
 */
public class CsvImportDialog extends JDialog {

    public enum ImportType {
        CLIENTS, PRODUCTS, ORDERS
    }

    private ImportType importType;
    private File selectedFile;
    private boolean hasHeader = true;
    private JLabel fileLabel;
    private JCheckBox headerCheckbox;
    private JTable previewTable;
    private DefaultTableModel previewModel;
    private JTextArea errorsArea;
    private JLabel statusLabel;
    private JButton importButton;
    
    private ImportResult<?> importResult;
    private boolean importConfirmed = false;

    public CsvImportDialog(Frame parent, ImportType type) {
        super(parent, getTitle(type), true);
        this.importType = type;
        initializeComponents();
        setupLayout();
        pack();
        setLocationRelativeTo(parent);
        setMinimumSize(new Dimension(700, 500));
    }

    private static String getTitle(ImportType type) {
        switch (type) {
            case CLIENTS: return "Import Clients from CSV";
            case PRODUCTS: return "Import Products from CSV";
            case ORDERS: return "Import Orders from CSV";
            default: return "Import CSV";
        }
    }

    private void initializeComponents() {
        // File selection
        fileLabel = new JLabel("No file selected");
        fileLabel.setForeground(UIThemeManager.COLOR_TEXT);

        // Header checkbox
        headerCheckbox = new JCheckBox("First row is header", true);
        headerCheckbox.setBackground(UIThemeManager.COLOR_BACKGROUND);
        headerCheckbox.addActionListener(e -> {
            hasHeader = headerCheckbox.isSelected();
            if (selectedFile != null) {
                loadPreview();
            }
        });

        // Preview table
        previewModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        previewTable = new JTable(previewModel);
        previewTable.setBackground(Color.WHITE);
        previewTable.setRowHeight(22);
        previewTable.getTableHeader().setBackground(UIThemeManager.COLOR_DARK_ACCENT);
        previewTable.getTableHeader().setForeground(Color.WHITE);

        // Errors area
        errorsArea = new JTextArea(5, 50);
        errorsArea.setEditable(false);
        errorsArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        errorsArea.setForeground(UIThemeManager.COLOR_ERROR);

        // Status label
        statusLabel = new JLabel("");
        statusLabel.setForeground(UIThemeManager.COLOR_TEXT);

        // Import button (initially disabled)
        importButton = new JButton("Import");
        importButton.setBackground(UIThemeManager.COLOR_PRIMARY);
        importButton.setForeground(Color.WHITE);
        importButton.setEnabled(false);
        importButton.addActionListener(e -> confirmImport());
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new MigLayout(
            "fill, insets 15",
            "[grow]",
            "[][][][grow][][grow][]"
        ));
        mainPanel.setBackground(UIThemeManager.COLOR_BACKGROUND);

        // File selection row
        JPanel filePanel = new JPanel(new MigLayout("insets 0", "[][grow][]", ""));
        filePanel.setBackground(UIThemeManager.COLOR_BACKGROUND);
        
        JLabel filePrompt = new JLabel("CSV File:");
        filePrompt.setForeground(UIThemeManager.COLOR_TEXT);
        filePanel.add(filePrompt);
        filePanel.add(fileLabel, "growx");
        
        JButton browseButton = UIThemeManager.createStyledButton("Browse...");
        browseButton.addActionListener(e -> browseFile());
        filePanel.add(browseButton);
        
        mainPanel.add(filePanel, "growx, wrap");

        // Options row
        JPanel optionsPanel = new JPanel(new MigLayout("insets 0", "[][]push[]", ""));
        optionsPanel.setBackground(UIThemeManager.COLOR_BACKGROUND);
        optionsPanel.add(headerCheckbox);
        
        JButton sampleButton = new JButton("Show Sample Format");
        sampleButton.setFont(new Font("Dialog", Font.PLAIN, 10));
        sampleButton.addActionListener(e -> showSampleFormat());
        optionsPanel.add(sampleButton);
        
        optionsPanel.add(statusLabel);
        mainPanel.add(optionsPanel, "growx, wrap");

        // Expected format info
        JLabel formatLabel = new JLabel(getExpectedFormat());
        formatLabel.setForeground(UIThemeManager.COLOR_SECONDARY);
        formatLabel.setFont(new Font("Dialog", Font.ITALIC, 11));
        mainPanel.add(formatLabel, "wrap");

        // Preview table
        mainPanel.add(new JLabel("Preview (first 10 rows):"), "wrap");
        JScrollPane previewScroll = new JScrollPane(previewTable);
        previewScroll.setPreferredSize(new Dimension(650, 150));
        mainPanel.add(previewScroll, "grow, wrap");

        // Errors section
        mainPanel.add(new JLabel("Validation Errors:"), "wrap");
        JScrollPane errorsScroll = new JScrollPane(errorsArea);
        errorsScroll.setPreferredSize(new Dimension(650, 100));
        mainPanel.add(errorsScroll, "grow, wrap");

        // Button row
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "push[][]", ""));
        buttonPanel.setBackground(UIThemeManager.COLOR_BACKGROUND);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(UIThemeManager.COLOR_SECONDARY);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(importButton);
        
        mainPanel.add(buttonPanel, "growx");

        setContentPane(mainPanel);
    }

    private String getExpectedFormat() {
        switch (importType) {
            case CLIENTS:
                return "Expected format: Name, Capital, Address";
            case PRODUCTS:
                return "Expected format: Name, Price, Stock";
            case ORDERS:
                return "Expected format: Date (yyyy-MM-dd)";
            default:
                return "";
        }
    }

    private void browseFile() {
        selectedFile = ImportUtil.showOpenDialog(this);
        if (selectedFile != null) {
            fileLabel.setText(selectedFile.getName());
            loadPreview();
            validateFile();
        }
    }

    private void loadPreview() {
        if (selectedFile == null) return;
        
        try {
            List<String[]> preview = ImportUtil.parseCSVPreview(selectedFile, 11); // 10 data rows + header
            
            previewModel.setRowCount(0);
            previewModel.setColumnCount(0);
            
            if (preview.isEmpty()) {
                statusLabel.setText("File is empty");
                return;
            }
            
            // Set columns from first row or default
            String[] firstRow = preview.get(0);
            if (hasHeader && preview.size() > 0) {
                for (String col : firstRow) {
                    previewModel.addColumn(col);
                }
                preview.remove(0);
            } else {
                // Generate default column names
                for (int i = 0; i < firstRow.length; i++) {
                    previewModel.addColumn("Column " + (i + 1));
                }
            }
            
            // Add data rows
            for (String[] row : preview) {
                previewModel.addRow(row);
            }
            
        } catch (Exception e) {
            statusLabel.setText("Error reading file: " + e.getMessage());
        }
    }

    private void validateFile() {
        if (selectedFile == null) return;
        
        errorsArea.setText("");
        
        switch (importType) {
            case CLIENTS:
                importResult = ImportUtil.importClientsFromCSV(selectedFile, hasHeader);
                break;
            case PRODUCTS:
                importResult = ImportUtil.importProductsFromCSV(selectedFile, hasHeader);
                break;
            case ORDERS:
                importResult = ImportUtil.importOrdersFromCSV(selectedFile, hasHeader);
                break;
        }
        
        // Show validation results
        StringBuilder errors = new StringBuilder();
        for (ImportError error : importResult.getErrors()) {
            errors.append(error.toString()).append("\n");
        }
        errorsArea.setText(errors.toString());
        
        // Update status
        statusLabel.setText(String.format("Valid: %d, Errors: %d", 
            importResult.getSuccessCount(), importResult.getErrorCount()));
        
        // Enable import if there are valid records
        importButton.setEnabled(importResult.getSuccessCount() > 0);
        
        if (importResult.hasErrors()) {
            statusLabel.setForeground(UIThemeManager.COLOR_WARNING);
        } else {
            statusLabel.setForeground(UIThemeManager.COLOR_SUCCESS);
        }
    }

    private void showSampleFormat() {
        String sample;
        switch (importType) {
            case CLIENTS:
                sample = ImportUtil.getSampleClientCSV();
                break;
            case PRODUCTS:
                sample = ImportUtil.getSampleProductCSV();
                break;
            case ORDERS:
                sample = ImportUtil.getSampleOrderCSV();
                break;
            default:
                sample = "";
        }
        
        JTextArea sampleArea = new JTextArea(sample);
        sampleArea.setEditable(false);
        sampleArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(sampleArea);
        scrollPane.setPreferredSize(new Dimension(400, 150));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Sample CSV Format", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void confirmImport() {
        if (importResult == null || importResult.getSuccessCount() == 0) {
            return;
        }
        
        String message;
        if (importResult.hasErrors()) {
            message = String.format(
                "Import %d valid records?\n\n%d records will be skipped due to errors.\n\nContinue?",
                importResult.getSuccessCount(), importResult.getErrorCount());
        } else {
            message = String.format("Import %d records?", importResult.getSuccessCount());
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, message, "Confirm Import", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            importConfirmed = true;
            dispose();
        }
    }

    /**
     * Check if import was confirmed.
     */
    public boolean isImportConfirmed() {
        return importConfirmed;
    }

    /**
     * Get imported client records.
     */
    @SuppressWarnings("unchecked")
    public List<ClientDTO> getImportedClients() {
        if (importType == ImportType.CLIENTS && importResult != null) {
            return ((ImportResult<ClientDTO>) importResult).getSuccessfulRecords();
        }
        return List.of();
    }

    /**
     * Get imported product records.
     */
    @SuppressWarnings("unchecked")
    public List<ProduitDTO> getImportedProducts() {
        if (importType == ImportType.PRODUCTS && importResult != null) {
            return ((ImportResult<ProduitDTO>) importResult).getSuccessfulRecords();
        }
        return List.of();
    }

    /**
     * Get imported order records.
     */
    @SuppressWarnings("unchecked")
    public List<CommandeDTO> getImportedOrders() {
        if (importType == ImportType.ORDERS && importResult != null) {
            return ((ImportResult<CommandeDTO>) importResult).getSuccessfulRecords();
        }
        return List.of();
    }

    /**
     * Get total successful imports.
     */
    public int getImportCount() {
        return importResult != null ? importResult.getSuccessCount() : 0;
    }
}
