package view.components;

import view.utils.UIThemeManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Reusable search and filter toolbar component.
 * Provides real-time search with debouncing and filter dropdowns.
 */
public class SearchToolbar extends JPanel {

    private JTextField searchField;
    private List<JComboBox<String>> filterComboBoxes;
    private JButton clearButton;
    private JButton refreshButton;
    private JLabel resultCountLabel;
    private SearchListener searchListener;
    private static final int DEBOUNCE_DELAY = 300;
    private Timer debounceTimer;

    /**
     * Interface for search event callbacks.
     */
    public interface SearchListener {
        void onSearch(String query, List<String> filterValues);
        void onClear();
        void onRefresh();
    }

    /**
     * Create a new SearchToolbar.
     */
    public SearchToolbar() {
        this.filterComboBoxes = new ArrayList<>();
        initializeComponents();
        setupLayout();
    }

    /**
     * Initialize UI components.
     */
    private void initializeComponents() {
        setBackground(UIThemeManager.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Search field
        searchField = new JTextField();
        searchField.setFont(new Font("Dialog", Font.PLAIN, 11));
        searchField.setPreferredSize(new Dimension(200, 28));
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                onSearchInputChanged();
            }
        });

        // Clear button
        clearButton = UIThemeManager.createStyledButton("Clear Filters");
        clearButton.setPreferredSize(new Dimension(100, 28));
        clearButton.addActionListener(e -> onClearFilters());

        // Refresh button
        refreshButton = UIThemeManager.createStyledButton("Refresh");
        refreshButton.setPreferredSize(new Dimension(80, 28));
        refreshButton.addActionListener(e -> onRefresh());

        // Result count label
        resultCountLabel = UIThemeManager.createStyledLabel("", Font.PLAIN);
    }

    /**
     * Setup layout using MigLayout.
     */
    private void setupLayout() {
        setLayout(new MigLayout(
            "fillx, insets 0",
            "[][grow][][][]",
            "[]"
        ));

        add(new JLabel("🔍"), "cell 0 0");
        add(searchField, "cell 1 0, growx");
        add(clearButton, "cell 2 0");
        add(refreshButton, "cell 3 0");
        add(resultCountLabel, "cell 4 0");
    }

    /**
     * Add a filter dropdown.
     */
    public void addFilter(String label, String[] options) {
        JComboBox<String> comboBox = new JComboBox<>(options);
        comboBox.setSelectedIndex(0);
        comboBox.addActionListener(e -> onSearchInputChanged());
        filterComboBoxes.add(comboBox);

        // Add to UI (position 1 for first filter, 2 for second, etc.)
        add(new JLabel(label + ":"), "gap para");
        add(comboBox, "wmin 80");
    }

    /**
     * Clear all search and filter fields.
     */
    private void onClearFilters() {
        searchField.setText("");
        for (JComboBox<String> filter : filterComboBoxes) {
            filter.setSelectedIndex(0);
        }
        resultCountLabel.setText("");
        if (searchListener != null) {
            searchListener.onClear();
        }
    }

    /**
     * Refresh data.
     */
    private void onRefresh() {
        if (searchListener != null) {
            searchListener.onRefresh();
        }
    }

    /**
     * Handle search input changes with debouncing.
     */
    private void onSearchInputChanged() {
        // Cancel previous timer
        if (debounceTimer != null && debounceTimer.isRunning()) {
            debounceTimer.stop();
        }

        // Schedule new search after delay
        debounceTimer = new Timer(DEBOUNCE_DELAY, e -> {
            if (searchListener != null) {
                String query = searchField.getText().trim();
                List<String> filterValues = new ArrayList<>();
                for (JComboBox<String> filter : filterComboBoxes) {
                    filterValues.add((String) filter.getSelectedItem());
                }
                searchListener.onSearch(query, filterValues);
            }
        });
        debounceTimer.setRepeats(false);
        debounceTimer.start();
    }

    /**
     * Set the search listener.
     */
    public void setSearchListener(SearchListener listener) {
        this.searchListener = listener;
    }

    /**
     * Update the result count display.
     */
    public void setResultCount(int found, int total) {
        resultCountLabel.setText(String.format("Found %d of %d", found, total));
    }

    /**
     * Get the current search query.
     */
    public String getSearchQuery() {
        return searchField.getText().trim();
    }

    /**
     * Get the current filter values.
     */
    public List<String> getFilterValues() {
        List<String> values = new ArrayList<>();
        for (JComboBox<String> filter : filterComboBoxes) {
            values.add((String) filter.getSelectedItem());
        }
        return values;
    }

    /**
     * Clear all fields.
     */
    public void clearAll() {
        searchField.setText("");
        for (JComboBox<String> filter : filterComboBoxes) {
            filter.setSelectedIndex(0);
        }
        resultCountLabel.setText("");
    }
}
