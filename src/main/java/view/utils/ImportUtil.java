package view.utils;

import dto.ClientDTO;
import dto.CommandeDTO;
import dto.ProduitDTO;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Utility class for importing data from CSV files.
 * Provides validation, error handling, and preview capabilities.
 */
public class ImportUtil {

    /**
     * Result class for import operations.
     */
    public static class ImportResult<T> {
        private List<T> successfulRecords;
        private List<ImportError> errors;
        private int totalRows;
        
        public ImportResult() {
            this.successfulRecords = new ArrayList<>();
            this.errors = new ArrayList<>();
            this.totalRows = 0;
        }
        
        public List<T> getSuccessfulRecords() { return successfulRecords; }
        public List<ImportError> getErrors() { return errors; }
        public int getTotalRows() { return totalRows; }
        public int getSuccessCount() { return successfulRecords.size(); }
        public int getErrorCount() { return errors.size(); }
        public boolean hasErrors() { return !errors.isEmpty(); }
        
        public void setTotalRows(int total) { this.totalRows = total; }
        public void addSuccess(T record) { successfulRecords.add(record); }
        public void addError(ImportError error) { errors.add(error); }
    }
    
    /**
     * Error class for import validation.
     */
    public static class ImportError {
        private int rowNumber;
        private String field;
        private String value;
        private String message;
        
        public ImportError(int rowNumber, String field, String value, String message) {
            this.rowNumber = rowNumber;
            this.field = field;
            this.value = value;
            this.message = message;
        }
        
        public int getRowNumber() { return rowNumber; }
        public String getField() { return field; }
        public String getValue() { return value; }
        public String getMessage() { return message; }
        
        @Override
        public String toString() {
            return String.format("Row %d, %s: %s - %s", rowNumber, field, value, message);
        }
    }

    /**
     * Show file open dialog for CSV import.
     */
    public static File showOpenDialog(Component parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import CSV File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    /**
     * Parse CSV file and return raw data for preview.
     */
    public static List<String[]> parseCSVPreview(File file, int maxRows) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count < maxRows) {
                rows.add(parseCSVLine(line));
                count++;
            }
        }
        return rows;
    }

    /**
     * Parse a CSV line handling quoted fields.
     */
    public static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());
        
        return fields.toArray(new String[0]);
    }

    /**
     * Import clients from CSV file.
     * Expected format: Name,Capital,Address (header optional)
     */
    public static ImportResult<ClientDTO> importClientsFromCSV(File file, boolean hasHeader) {
        ImportResult<ClientDTO> result = new ImportResult<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            
            // Skip header if present
            if (hasHeader && (line = reader.readLine()) != null) {
                lineNumber++;
            }
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                result.setTotalRows(result.getTotalRows() + 1);
                
                try {
                    String[] fields = parseCSVLine(line);
                    
                    // Validate minimum fields
                    if (fields.length < 3) {
                        result.addError(new ImportError(lineNumber, "row", line, 
                            "Insufficient fields. Expected: Name, Capital, Address"));
                        continue;
                    }
                    
                    ClientDTO client = new ClientDTO();
                    
                    // Name (required)
                    String name = fields[0].trim();
                    if (name.isEmpty()) {
                        result.addError(new ImportError(lineNumber, "Name", name, "Name is required"));
                        continue;
                    }
                    client.setNom(name);
                    
                    // Capital (required, numeric)
                    String capitalStr = fields[1].trim();
                    try {
                        double capital = Double.parseDouble(capitalStr);
                        if (capital < 0) {
                            result.addError(new ImportError(lineNumber, "Capital", capitalStr, 
                                "Capital cannot be negative"));
                            continue;
                        }
                        client.setCapital(capital);
                    } catch (NumberFormatException e) {
                        result.addError(new ImportError(lineNumber, "Capital", capitalStr, 
                            "Invalid number format"));
                        continue;
                    }
                    
                    // Address (required)
                    String address = fields[2].trim();
                    if (address.isEmpty()) {
                        result.addError(new ImportError(lineNumber, "Address", address, "Address is required"));
                        continue;
                    }
                    client.setAdresse(address);
                    
                    result.addSuccess(client);
                    
                } catch (Exception e) {
                    result.addError(new ImportError(lineNumber, "row", line, 
                        "Failed to parse: " + e.getMessage()));
                }
            }
            
        } catch (IOException e) {
            result.addError(new ImportError(0, "file", file.getName(), 
                "Failed to read file: " + e.getMessage()));
        }
        
        return result;
    }

    /**
     * Import products from CSV file.
     * Expected format: Name,Price,Stock (header optional)
     */
    public static ImportResult<ProduitDTO> importProductsFromCSV(File file, boolean hasHeader) {
        ImportResult<ProduitDTO> result = new ImportResult<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            
            // Skip header if present
            if (hasHeader && (line = reader.readLine()) != null) {
                lineNumber++;
            }
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                result.setTotalRows(result.getTotalRows() + 1);
                
                try {
                    String[] fields = parseCSVLine(line);
                    
                    // Validate minimum fields
                    if (fields.length < 3) {
                        result.addError(new ImportError(lineNumber, "row", line, 
                            "Insufficient fields. Expected: Name, Price, Stock"));
                        continue;
                    }
                    
                    ProduitDTO produit = new ProduitDTO();
                    
                    // Name (required)
                    String name = fields[0].trim();
                    if (name.isEmpty()) {
                        result.addError(new ImportError(lineNumber, "Name", name, "Name is required"));
                        continue;
                    }
                    produit.setLibelle(name);
                    
                    // Price (required, numeric)
                    String priceStr = fields[1].trim();
                    try {
                        float price = Float.parseFloat(priceStr);
                        if (price < 0) {
                            result.addError(new ImportError(lineNumber, "Price", priceStr, 
                                "Price cannot be negative"));
                            continue;
                        }
                        produit.setPrix(price);
                    } catch (NumberFormatException e) {
                        result.addError(new ImportError(lineNumber, "Price", priceStr, 
                            "Invalid number format"));
                        continue;
                    }
                    
                    // Stock (required, integer)
                    String stockStr = fields[2].trim();
                    try {
                        int stock = Integer.parseInt(stockStr);
                        if (stock < 0) {
                            result.addError(new ImportError(lineNumber, "Stock", stockStr, 
                                "Stock cannot be negative"));
                            continue;
                        }
                        produit.setQtstock(stock);
                    } catch (NumberFormatException e) {
                        result.addError(new ImportError(lineNumber, "Stock", stockStr, 
                            "Invalid integer format"));
                        continue;
                    }
                    
                    result.addSuccess(produit);
                    
                } catch (Exception e) {
                    result.addError(new ImportError(lineNumber, "row", line, 
                        "Failed to parse: " + e.getMessage()));
                }
            }
            
        } catch (IOException e) {
            result.addError(new ImportError(0, "file", file.getName(), 
                "Failed to read file: " + e.getMessage()));
        }
        
        return result;
    }

    /**
     * Import orders from CSV file (basic - date only, no line items).
     * Expected format: Date (yyyy-MM-dd), ClientId (header optional)
     */
    public static ImportResult<CommandeDTO> importOrdersFromCSV(File file, boolean hasHeader) {
        ImportResult<CommandeDTO> result = new ImportResult<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            
            // Skip header if present
            if (hasHeader && (line = reader.readLine()) != null) {
                lineNumber++;
            }
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                result.setTotalRows(result.getTotalRows() + 1);
                
                try {
                    String[] fields = parseCSVLine(line);
                    
                    // Validate minimum fields
                    if (fields.length < 1) {
                        result.addError(new ImportError(lineNumber, "row", line, 
                            "Insufficient fields. Expected: Date"));
                        continue;
                    }
                    
                    CommandeDTO commande = new CommandeDTO();
                    
                    // Date (required)
                    String dateStr = fields[0].trim();
                    try {
                        Date date = dateFormat.parse(dateStr);
                        commande.setDatecmd(date);
                    } catch (ParseException e) {
                        result.addError(new ImportError(lineNumber, "Date", dateStr, 
                            "Invalid date format. Expected: yyyy-MM-dd"));
                        continue;
                    }
                    
                    result.addSuccess(commande);
                    
                } catch (Exception e) {
                    result.addError(new ImportError(lineNumber, "row", line, 
                        "Failed to parse: " + e.getMessage()));
                }
            }
            
        } catch (IOException e) {
            result.addError(new ImportError(0, "file", file.getName(), 
                "Failed to read file: " + e.getMessage()));
        }
        
        return result;
    }

    /**
     * Generate sample CSV content for clients.
     */
    public static String getSampleClientCSV() {
        return "Name,Capital,Address\n" +
               "Acme Corp,50000.00,123 Business Street\n" +
               "Tech Solutions,75000.50,456 Innovation Ave\n" +
               "Global Industries,120000.00,789 Enterprise Blvd";
    }

    /**
     * Generate sample CSV content for products.
     */
    public static String getSampleProductCSV() {
        return "Name,Price,Stock\n" +
               "Laptop Pro,999.99,50\n" +
               "Wireless Mouse,29.50,200\n" +
               "USB Cable,9.99,500";
    }

    /**
     * Generate sample CSV content for orders.
     */
    public static String getSampleOrderCSV() {
        return "Date\n" +
               "2026-02-01\n" +
               "2026-02-15\n" +
               "2026-02-20";
    }
}
