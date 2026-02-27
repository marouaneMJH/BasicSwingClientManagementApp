package view.components;

import view.utils.UIThemeManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Advanced search panel with multi-field filtering capabilities.
 * Supports text search, numeric ranges, and date ranges.
 */
public class AdvancedSearchPanel extends JPanel {

    private JTextField searchField;
    private JTextField minCapitalField;
    private JTextField maxCapitalField;
    private JTextField minPriceField;
    private JTextField maxPriceField;
    private JTextField minStockField;
    private JTextField maxStockField;
    private JTextField dateFromField;
    private JTextField dateToField;
    private JPanel quickFiltersPanel;
    private JLabel resultCountLabel;
    
    private AdvancedSearchListener searchListener;
    private Timer debounceTimer;
    private static final int DEBOUNCE_DELAY = 300;
    
    public enum SearchMode {
        CLIENTS, PRODUCTS, ORDERS
    }
    
    private SearchMode currentMode = SearchMode.CLIENTS;

    /**
     * Interface for advanced search callbacks.
     */
    public interface AdvancedSearchListener {
        void onAdvancedSearch(Map<String, Object> filters);
        void onQuickFilter(String filterName);
        void onClear();
        void onRefresh();
    }

    public AdvancedSearchPanel() {
        initializeComponents();
        setupLayout();
    }

    public AdvancedSearchPanel(SearchMode mode) {
        this.currentMode = mode;
        initializeComponents();
        setupLayout();
        updateFieldsForMode();
    }

    private void initializeComponents() {
        setBackground(UIThemeManager.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Main search field
        searchField = createSearchField("Search by name, address...");
        
        // Numeric range fields
        minCapitalField = createNumericField("Min");
        maxCapitalField = createNumericField("Max");
        minPriceField = createNumericField("Min");
        maxPriceField = createNumericField("Max");
        minStockField = createNumericField("Min");
        maxStockField = createNumericField("Max");
        
        // Date range fields
        dateFromField = createDateField("From (yyyy-MM-dd)");
        dateToField = createDateField("To (yyyy-MM-dd)");

        // Result count label
        resultCountLabel = UIThemeManager.createStyledLabel("", Font.PLAIN);

        // Quick filters panel
        quickFiltersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        quickFiltersPanel.setBackground(UIThemeManager.COLOR_BACKGROUND);
    }

    private JTextField createSearchField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Dialog", Font.PLAIN, 11));
        field.setPreferredSize(new Dimension(200, 28));
        field.setToolTipText(placeholder);
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { triggerSearch(); }
            public void removeUpdate(DocumentEvent e) { triggerSearch(); }
            public void changedUpdate(DocumentEvent e) { triggerSearch(); }
        });
        return field;
    }

    private JTextField createNumericField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Dialog", Font.PLAIN, 11));
        field.setPreferredSize(new Dimension(70, 28));
        field.setToolTipText(placeholder);
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { triggerSearch(); }
            public void removeUpdate(DocumentEvent e) { triggerSearch(); }
            public void changedUpdate(DocumentEvent e) { triggerSearch(); }
        });
        return field;
    }

    private JTextField createDateField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Dialog", Font.PLAIN, 11));
        field.setPreferredSize(new Dimension(100, 28));
        field.setToolTipText(placeholder);
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { triggerSearch(); }
            public void removeUpdate(DocumentEvent e) { triggerSearch(); }
            public void changedUpdate(DocumentEvent e) { triggerSearch(); }
        });
        return field;
    }

    private void setupLayout() {
        setLayout(new MigLayout(
            "fillx, insets 0, gap 5",
            "[][grow][][]",
            "[][][]"
        ));

        // Row 1: Main search + buttons
        add(new JLabel("🔍"), "cell 0 0");
        add(searchField, "cell 1 0, growx");
        
        JButton clearButton = UIThemeManager.createStyledButton("Clear");
        clearButton.setPreferredSize(new Dimension(70, 28));
        clearButton.addActionListener(e -> clearAllFilters());
        add(clearButton, "cell 2 0");
        
        JButton refreshButton = UIThemeManager.createStyledButton("Refresh");
        refreshButton.setPreferredSize(new Dimension(80, 28));
        refreshButton.addActionListener(e -> {
            if (searchListener != null) searchListener.onRefresh();
        });
        add(refreshButton, "cell 3 0");

        // Row 2: Quick filters
        add(quickFiltersPanel, "cell 0 1 4 1, growx");

        // Row 3: Advanced filters panel (will be configured per mode)
        JPanel advancedPanel = createAdvancedFiltersPanel();
        add(advancedPanel, "cell 0 2 4 1, growx");
    }

    private JPanel createAdvancedFiltersPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0, gap 10", "[][][][][][grow][]", ""));
        panel.setBackground(UIThemeManager.COLOR_BACKGROUND);

        switch (currentMode) {
            case CLIENTS:
                panel.add(new JLabel("Capital:"));
                panel.add(minCapitalField);
                panel.add(new JLabel("-"));
                panel.add(maxCapitalField);
                panel.add(resultCountLabel, "skip, pushx, right");
                break;

            case PRODUCTS:
                panel.add(new JLabel("Price:"));
                panel.add(minPriceField);
                panel.add(new JLabel("-"));
                panel.add(maxPriceField);
                panel.add(new JLabel("Stock:"));
                panel.add(minStockField);
                panel.add(new JLabel("-"));
                panel.add(maxStockField);
                panel.add(resultCountLabel, "pushx, right");
                break;

            case ORDERS:
                panel.add(new JLabel("Date From:"));
                panel.add(dateFromField);
                panel.add(new JLabel("To:"));
                panel.add(dateToField);
                panel.add(resultCountLabel, "skip, pushx, right");
                break;
        }

        return panel;
    }

    private void updateFieldsForMode() {
        // Update quick filter buttons based on mode
        quickFiltersPanel.removeAll();
        
        switch (currentMode) {
            case CLIENTS:
                addQuickFilter("High Value", "capital > 10000");
                addQuickFilter("Low Capital", "capital < 1000");
                addQuickFilter("All Clients", "all");
                break;
                
            case PRODUCTS:
                addQuickFilter("Low Stock", "stock < 10");
                addQuickFilter("Out of Stock", "stock = 0");
                addQuickFilter("High Price", "price > 100");
                addQuickFilter("All Products", "all");
                break;
                
            case ORDERS:
                addQuickFilter("This Week", "week");
                addQuickFilter("This Month", "month");
                addQuickFilter("Last 30 Days", "days30");
                addQuickFilter("All Orders", "all");
                break;
        }
        
        quickFiltersPanel.revalidate();
        quickFiltersPanel.repaint();
    }

    /**
     * Add a quick filter button.
     */
    public void addQuickFilter(String label, String filterKey) {
        JButton button = new JButton(label);
        button.setFont(new Font("Dialog", Font.PLAIN, 10));
        button.setBackground(UIThemeManager.COLOR_SECONDARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(100, 24));
        button.addActionListener(e -> {
            if (searchListener != null) {
                searchListener.onQuickFilter(filterKey);
            }
        });
        quickFiltersPanel.add(button);
    }

    /**
     * Configure for client searches.
     */
    public void setModeClients() {
        this.currentMode = SearchMode.CLIENTS;
        updateFieldsForMode();
        removeAll();
        setupLayout();
        revalidate();
        repaint();
    }

    /**
     * Configure for product searches.
     */
    public void setModeProducts() {
        this.currentMode = SearchMode.PRODUCTS;
        updateFieldsForMode();
        removeAll();
        setupLayout();
        revalidate();
        repaint();
    }

    /**
     * Configure for order searches.
     */
    public void setModeOrders() {
        this.currentMode = SearchMode.ORDERS;
        updateFieldsForMode();
        removeAll();
        setupLayout();
        revalidate();
        repaint();
    }

    /**
     * Trigger search with debouncing.
     */
    private void triggerSearch() {
        if (debounceTimer != null && debounceTimer.isRunning()) {
            debounceTimer.stop();
        }

        debounceTimer = new Timer(DEBOUNCE_DELAY, e -> {
            if (searchListener != null) {
                searchListener.onAdvancedSearch(getFilters());
            }
        });
        debounceTimer.setRepeats(false);
        debounceTimer.start();
    }

    /**
     * Get current filter values as a map.
     */
    public Map<String, Object> getFilters() {
        Map<String, Object> filters = new HashMap<>();
        
        // Text search
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            filters.put("query", query);
        }

        // Numeric ranges
        Double minCapital = parseDouble(minCapitalField.getText());
        Double maxCapital = parseDouble(maxCapitalField.getText());
        if (minCapital != null) filters.put("minCapital", minCapital);
        if (maxCapital != null) filters.put("maxCapital", maxCapital);

        Double minPrice = parseDouble(minPriceField.getText());
        Double maxPrice = parseDouble(maxPriceField.getText());
        if (minPrice != null) filters.put("minPrice", minPrice);
        if (maxPrice != null) filters.put("maxPrice", maxPrice);

        Integer minStock = parseInteger(minStockField.getText());
        Integer maxStock = parseInteger(maxStockField.getText());
        if (minStock != null) filters.put("minStock", minStock);
        if (maxStock != null) filters.put("maxStock", maxStock);

        // Date ranges
        Date dateFrom = parseDate(dateFromField.getText());
        Date dateTo = parseDate(dateToField.getText());
        if (dateFrom != null) filters.put("dateFrom", dateFrom);
        if (dateTo != null) filters.put("dateTo", dateTo);

        return filters;
    }

    private Double parseDouble(String text) {
        try {
            return text.isEmpty() ? null : Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String text) {
        try {
            return text.isEmpty() ? null : Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Date parseDate(String text) {
        try {
            return text.isEmpty() ? null : new SimpleDateFormat("yyyy-MM-dd").parse(text.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Clear all filter fields.
     */
    public void clearAllFilters() {
        searchField.setText("");
        minCapitalField.setText("");
        maxCapitalField.setText("");
        minPriceField.setText("");
        maxPriceField.setText("");
        minStockField.setText("");
        maxStockField.setText("");
        dateFromField.setText("");
        dateToField.setText("");
        resultCountLabel.setText("");
        
        if (searchListener != null) {
            searchListener.onClear();
        }
    }

    /**
     * Set the search listener.
     */
    public void setSearchListener(AdvancedSearchListener listener) {
        this.searchListener = listener;
    }

    /**
     * Update result count display.
     */
    public void setResultCount(int found, int total) {
        resultCountLabel.setText(String.format("Found %d of %d", found, total));
    }

    /**
     * Get current search query.
     */
    public String getSearchQuery() {
        return searchField.getText().trim();
    }
}
