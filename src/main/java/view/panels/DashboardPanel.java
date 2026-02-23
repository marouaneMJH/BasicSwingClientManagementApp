package view.panels;

import view.utils.UIThemeManager;
import controller.ClientController;
import controller.CommandeController;
import controller.ProduitController;
import dto.ClientDTO;
import dto.CommandeDTO;
import dto.ProduitDTO;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/**
 * Dashboard panel with business overview widgets and client segmentation.
 * Provides at-a-glance metrics and quick insights.
 */
public class DashboardPanel extends JPanel {

    private ClientController clientController;
    private CommandeController commandeController;
    private ProduitController produitController;

    // Stats widgets
    private JLabel totalClientsLabel;
    private JLabel totalOrdersLabel;
    private JLabel totalProductsLabel;
    private JLabel totalRevenueLabel;
    private JLabel lowStockLabel;
    private JLabel avgOrderValueLabel;

    // Segmentation panels
    private JPanel clientSegmentPanel;
    private JPanel topClientsPanel;
    private JPanel lowStockPanel;
    private JPanel recentOrdersPanel;

    private NumberFormat currencyFormat;

    public DashboardPanel() {
        this.clientController = new ClientController();
        this.commandeController = new CommandeController();
        this.produitController = new ProduitController();
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        
        initializeComponents();
        setupLayout();
        loadDashboardData();
    }

    private void initializeComponents() {
        setBackground(UIThemeManager.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Initialize labels with placeholders
        totalClientsLabel = createStatLabel("0");
        totalOrdersLabel = createStatLabel("0");
        totalProductsLabel = createStatLabel("0");
        totalRevenueLabel = createStatLabel("$0.00");
        lowStockLabel = createStatLabel("0");
        avgOrderValueLabel = createStatLabel("$0.00");

        // Initialize segment panels
        clientSegmentPanel = new JPanel(new MigLayout("fill, insets 10", "[grow]", "[]"));
        clientSegmentPanel.setBackground(Color.WHITE);

        topClientsPanel = new JPanel(new MigLayout("fill, insets 10", "[grow]", "[]"));
        topClientsPanel.setBackground(Color.WHITE);

        lowStockPanel = new JPanel(new MigLayout("fill, insets 10", "[grow]", "[]"));
        lowStockPanel.setBackground(Color.WHITE);

        recentOrdersPanel = new JPanel(new MigLayout("fill, insets 10", "[grow]", "[]"));
        recentOrdersPanel.setBackground(Color.WHITE);
    }

    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Dialog", Font.BOLD, 28));
        label.setForeground(UIThemeManager.COLOR_DARK_ACCENT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private void setupLayout() {
        setLayout(new MigLayout(
            "fill, insets 0, gap 15",
            "[grow][grow][grow]",
            "[][grow][grow]"
        ));

        // Header with refresh button
        JPanel headerPanel = new JPanel(new MigLayout("insets 0", "[]push[]", ""));
        headerPanel.setBackground(UIThemeManager.COLOR_BACKGROUND);
        
        JLabel titleLabel = UIThemeManager.createHeaderLabel("Business Dashboard");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 20));
        headerPanel.add(titleLabel);
        
        JButton refreshBtn = UIThemeManager.createStyledButton("Refresh");
        refreshBtn.addActionListener(e -> loadDashboardData());
        headerPanel.add(refreshBtn);
        
        add(headerPanel, "cell 0 0 3 1, growx, wrap");

        // Row 1: Stats cards (6 cards in 2 rows of 3)
        JPanel statsRow1 = new JPanel(new MigLayout("fill, insets 0, gap 15", "[grow][grow][grow]", ""));
        statsRow1.setBackground(UIThemeManager.COLOR_BACKGROUND);
        statsRow1.add(createStatCard("Total Clients", totalClientsLabel, UIThemeManager.COLOR_PRIMARY), "grow");
        statsRow1.add(createStatCard("Total Orders", totalOrdersLabel, UIThemeManager.COLOR_SECONDARY), "grow");
        statsRow1.add(createStatCard("Total Products", totalProductsLabel, UIThemeManager.COLOR_DARK_ACCENT), "grow");
        add(statsRow1, "cell 0 1 3 1, growx, wrap");

        JPanel statsRow2 = new JPanel(new MigLayout("fill, insets 0, gap 15", "[grow][grow][grow]", ""));
        statsRow2.setBackground(UIThemeManager.COLOR_BACKGROUND);
        statsRow2.add(createStatCard("Total Revenue", totalRevenueLabel, UIThemeManager.COLOR_SUCCESS), "grow");
        statsRow2.add(createStatCard("Low Stock Items", lowStockLabel, UIThemeManager.COLOR_WARNING), "grow");
        statsRow2.add(createStatCard("Avg Order Value", avgOrderValueLabel, UIThemeManager.COLOR_PRIMARY), "grow");
        add(statsRow2, "cell 0 2 3 1, growx, wrap");

        // Row 2: Segmentation widgets
        add(createWidgetPanel("Client Segmentation", clientSegmentPanel), "cell 0 3, grow");
        add(createWidgetPanel("Top Clients by Capital", topClientsPanel), "cell 1 3, grow");
        add(createWidgetPanel("Low Stock Alert", lowStockPanel), "cell 2 3, grow");

        // Row 3: Recent activity
        add(createWidgetPanel("Recent Orders", recentOrdersPanel), "cell 0 4 3 1, grow, h 150!");
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new MigLayout("fill, insets 15", "[grow]", "[][]"));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(accentColor, 2, true),
            new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Dialog", Font.BOLD, 12));
        titleLbl.setForeground(UIThemeManager.COLOR_SECONDARY);
        titleLbl.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLbl, "growx, wrap");
        card.add(valueLabel, "growx");

        return card;
    }

    private JPanel createWidgetPanel(String title, JPanel contentPanel) {
        JPanel wrapper = new JPanel(new MigLayout("fill, insets 0", "[grow]", "[]0[grow]"));
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new LineBorder(UIThemeManager.COLOR_SECONDARY, 1, true));

        // Header
        JPanel header = new JPanel(new MigLayout("insets 8", "[grow]", ""));
        header.setBackground(UIThemeManager.COLOR_DARK_ACCENT);
        JLabel headerLabel = new JLabel(title);
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        header.add(headerLabel);
        
        wrapper.add(header, "growx, wrap");
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(Color.WHITE);
        wrapper.add(scrollPane, "grow");

        return wrapper;
    }

    /**
     * Load all dashboard data from controllers.
     */
    public void loadDashboardData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private List<ClientDTO> clients;
            private List<CommandeDTO> orders;
            private List<ProduitDTO> products;

            @Override
            protected Void doInBackground() {
                clients = clientController.getAllClients();
                orders = commandeController.getAllCommandes();
                products = produitController.getAllProduits();
                return null;
            }

            @Override
            protected void done() {
                if (clients == null) clients = List.of();
                if (orders == null) orders = List.of();
                if (products == null) products = List.of();

                updateStats(clients, orders, products);
                updateClientSegmentation(clients);
                updateTopClients(clients);
                updateLowStockAlerts(products);
                updateRecentOrders(orders);
            }
        };
        worker.execute();
    }

    private void updateStats(List<ClientDTO> clients, List<CommandeDTO> orders, List<ProduitDTO> products) {
        totalClientsLabel.setText(String.valueOf(clients.size()));
        totalOrdersLabel.setText(String.valueOf(orders.size()));
        totalProductsLabel.setText(String.valueOf(products.size()));

        // Calculate total revenue
        double totalRevenue = orders.stream()
            .mapToDouble(CommandeDTO::getTotal)
            .sum();
        totalRevenueLabel.setText(currencyFormat.format(totalRevenue));

        // Calculate low stock items (stock < 10)
        long lowStockCount = products.stream()
            .filter(p -> p.getQtstock() < 10)
            .count();
        lowStockLabel.setText(String.valueOf(lowStockCount));
        if (lowStockCount > 0) {
            lowStockLabel.setForeground(UIThemeManager.COLOR_WARNING);
        }

        // Calculate average order value
        double avgValue = orders.isEmpty() ? 0 : totalRevenue / orders.size();
        avgOrderValueLabel.setText(currencyFormat.format(avgValue));
    }

    private void updateClientSegmentation(List<ClientDTO> clients) {
        clientSegmentPanel.removeAll();
        clientSegmentPanel.setLayout(new MigLayout("fill, insets 5", "[grow]", ""));

        // Segment by capital ranges
        long highValue = clients.stream().filter(c -> c.getCapital() >= 50000).count();
        long midValue = clients.stream().filter(c -> c.getCapital() >= 10000 && c.getCapital() < 50000).count();
        long lowValue = clients.stream().filter(c -> c.getCapital() < 10000).count();

        clientSegmentPanel.add(createSegmentBar("High Value (≥$50K)", highValue, clients.size(), UIThemeManager.COLOR_SUCCESS), "growx, wrap");
        clientSegmentPanel.add(createSegmentBar("Mid Value ($10K-$50K)", midValue, clients.size(), UIThemeManager.COLOR_PRIMARY), "growx, wrap");
        clientSegmentPanel.add(createSegmentBar("Low Value (<$10K)", lowValue, clients.size(), UIThemeManager.COLOR_WARNING), "growx, wrap");

        // Summary
        JLabel summaryLabel = new JLabel(String.format("Total: %d clients", clients.size()));
        summaryLabel.setFont(new Font("Dialog", Font.ITALIC, 10));
        summaryLabel.setForeground(UIThemeManager.COLOR_SECONDARY);
        clientSegmentPanel.add(summaryLabel, "right");

        clientSegmentPanel.revalidate();
        clientSegmentPanel.repaint();
    }

    private JPanel createSegmentBar(String label, long count, int total, Color color) {
        JPanel panel = new JPanel(new MigLayout("fill, insets 2", "[][grow][]", ""));
        panel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        nameLabel.setPreferredSize(new Dimension(140, 20));
        panel.add(nameLabel);

        // Progress bar
        JProgressBar progressBar = new JProgressBar(0, Math.max(1, total));
        progressBar.setValue((int) count);
        progressBar.setStringPainted(false);
        progressBar.setForeground(color);
        progressBar.setBackground(UIThemeManager.COLOR_BACKGROUND);
        progressBar.setPreferredSize(new Dimension(100, 16));
        panel.add(progressBar, "growx");

        // Count label
        double percent = total > 0 ? (count * 100.0 / total) : 0;
        JLabel countLabel = new JLabel(String.format("%d (%.0f%%)", count, percent));
        countLabel.setFont(new Font("Dialog", Font.BOLD, 10));
        countLabel.setForeground(color);
        panel.add(countLabel);

        return panel;
    }

    private void updateTopClients(List<ClientDTO> clients) {
        topClientsPanel.removeAll();
        topClientsPanel.setLayout(new MigLayout("fill, insets 5", "[grow][]", ""));

        // Sort by capital descending and take top 5
        clients.stream()
            .sorted((a, b) -> Double.compare(b.getCapital(), a.getCapital()))
            .limit(5)
            .forEach(client -> {
                JLabel nameLabel = new JLabel(client.getNom());
                nameLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
                
                JLabel capitalLabel = new JLabel(currencyFormat.format(client.getCapital()));
                capitalLabel.setFont(new Font("Dialog", Font.BOLD, 11));
                capitalLabel.setForeground(UIThemeManager.COLOR_SUCCESS);
                
                topClientsPanel.add(nameLabel, "growx");
                topClientsPanel.add(capitalLabel, "wrap");
            });

        if (clients.isEmpty()) {
            JLabel emptyLabel = new JLabel("No clients found");
            emptyLabel.setForeground(UIThemeManager.COLOR_SECONDARY);
            topClientsPanel.add(emptyLabel);
        }

        topClientsPanel.revalidate();
        topClientsPanel.repaint();
    }

    private void updateLowStockAlerts(List<ProduitDTO> products) {
        lowStockPanel.removeAll();
        lowStockPanel.setLayout(new MigLayout("fill, insets 5", "[grow][]", ""));

        // Filter products with stock < 10
        List<ProduitDTO> lowStock = products.stream()
            .filter(p -> p.getQtstock() < 10)
            .sorted((a, b) -> Integer.compare(a.getQtstock(), b.getQtstock()))
            .limit(5)
            .toList();

        for (ProduitDTO product : lowStock) {
            JLabel nameLabel = new JLabel(product.getLibelle());
            nameLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
            
            JLabel stockLabel = new JLabel(String.valueOf(product.getQtstock()));
            stockLabel.setFont(new Font("Dialog", Font.BOLD, 11));
            
            if (product.getQtstock() == 0) {
                stockLabel.setForeground(UIThemeManager.COLOR_ERROR);
                stockLabel.setText("OUT OF STOCK");
            } else {
                stockLabel.setForeground(UIThemeManager.COLOR_WARNING);
                stockLabel.setText(product.getQtstock() + " left");
            }
            
            lowStockPanel.add(nameLabel, "growx");
            lowStockPanel.add(stockLabel, "wrap");
        }

        if (lowStock.isEmpty()) {
            JLabel okLabel = new JLabel("✓ All products well stocked");
            okLabel.setForeground(UIThemeManager.COLOR_SUCCESS);
            lowStockPanel.add(okLabel);
        }

        lowStockPanel.revalidate();
        lowStockPanel.repaint();
    }

    private void updateRecentOrders(List<CommandeDTO> orders) {
        recentOrdersPanel.removeAll();
        recentOrdersPanel.setLayout(new MigLayout("fill, insets 5", "[][grow][][]", ""));

        // Sort by date descending and take recent 5
        orders.stream()
            .sorted((a, b) -> {
                if (a.getDatecmd() == null) return 1;
                if (b.getDatecmd() == null) return -1;
                return b.getDatecmd().compareTo(a.getDatecmd());
            })
            .limit(5)
            .forEach(order -> {
                JLabel idLabel = new JLabel("#" + order.getIdcmd());
                idLabel.setFont(new Font("Dialog", Font.BOLD, 11));
                idLabel.setForeground(UIThemeManager.COLOR_PRIMARY);
                
                String clientName = order.getClient() != null ? order.getClient().getNom() : "Unknown";
                JLabel clientLabel = new JLabel(clientName);
                clientLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
                
                String dateStr = order.getDatecmd() != null ? 
                    new java.text.SimpleDateFormat("yyyy-MM-dd").format(order.getDatecmd()) : "N/A";
                JLabel dateLabel = new JLabel(dateStr);
                dateLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
                dateLabel.setForeground(UIThemeManager.COLOR_SECONDARY);
                
                JLabel totalLabel = new JLabel(currencyFormat.format(order.getTotal()));
                totalLabel.setFont(new Font("Dialog", Font.BOLD, 11));
                
                recentOrdersPanel.add(idLabel);
                recentOrdersPanel.add(clientLabel, "growx");
                recentOrdersPanel.add(dateLabel);
                recentOrdersPanel.add(totalLabel, "wrap");
            });

        if (orders.isEmpty()) {
            JLabel emptyLabel = new JLabel("No orders found");
            emptyLabel.setForeground(UIThemeManager.COLOR_SECONDARY);
            recentOrdersPanel.add(emptyLabel);
        }

        recentOrdersPanel.revalidate();
        recentOrdersPanel.repaint();
    }
}
