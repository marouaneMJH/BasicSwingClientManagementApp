package view.utils;

import dto.ClientDTO;
import dto.CommandeDTO;
import dto.ProduitDTO;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Utility class for exporting data to CSV format.
 */
public class ExportUtil {

    /**
     * Export client list to CSV file.
     */
    public static boolean exportClientsToCSV(List<ClientDTO> clients, File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write header
            writer.write("ID,Name,Capital,Address\n");
            
            // Write data rows
            for (ClientDTO client : clients) {
                StringBuilder row = new StringBuilder();
                row.append(client.getId()).append(",");
                row.append(escapeCSV(client.getNom())).append(",");
                row.append(client.getCapital()).append(",");
                row.append(escapeCSV(client.getAdresse()));
                writer.write(row.toString());
                writer.newLine();
            }
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Export commande list to CSV file.
     */
    public static boolean exportCommandesToCSV(List<CommandeDTO> commandes, File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write header
            writer.write("ID,Date,Client,Total\n");
            
            // Write data rows
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            for (CommandeDTO commande : commandes) {
                StringBuilder row = new StringBuilder();
                row.append(commande.getIdcmd()).append(",");
                String dateStr = (commande.getDatecmd() != null) ? dateFormat.format(commande.getDatecmd()) : "N/A";
                row.append(dateStr).append(",");
                String clientName = (commande.getClient() != null) ? commande.getClient().getNom() : "N/A";
                row.append(escapeCSV(clientName)).append(",");
                row.append(String.format("%.2f", commande.getTotal()));
                writer.write(row.toString());
                writer.newLine();
            }
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Export produit list to CSV file.
     */
    public static boolean exportProductsToCSV(List<ProduitDTO> produits, File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write header
            writer.write("ID,Name,Price,Stock\n");
            
            // Write data rows
            for (ProduitDTO produit : produits) {
                StringBuilder row = new StringBuilder();
                row.append(produit.getId()).append(",");
                row.append(escapeCSV(produit.getLibelle())).append(",");
                row.append(String.format("%.2f", produit.getPrix())).append(",");
                row.append(produit.getQtstock());
                writer.write(row.toString());
                writer.newLine();
            }
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Escape CSV special characters (commas, quotes, newlines).
     */
    private static String escapeCSV(String field) {
        if (field == null) {
            return "";
        }
        
        // If field contains comma, quote, or newline, wrap in quotes and escape inner quotes
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        
        return field;
    }

    /**
     * Show file chooser dialog and return selected file.
     */
    public static File showSaveDialog(JComponent parent, String fileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export to CSV");
        fileChooser.setSelectedFile(new File(fileName + ".csv"));
        
        int result = fileChooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }
}
