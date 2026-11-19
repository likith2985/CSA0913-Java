package task03;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Campus Library Search Screen.
 * Demonstrates:
 * 1. Strategic layout manager composition using BorderLayout, FlowLayout, and JScrollPane.
 * 2. Predictable resize behavior where Center results consume all extra space,
 *    while North (Search) and South (Pagination & Actions) maintain fixed preferred heights.
 */
public class LibrarySearchScreen extends JFrame {
    private final JTextField searchField;
    private final JComboBox<String> categoryCombo;
    private final JButton searchButton;
    private final JTable resultsTable;
    private final DefaultTableModel tableModel;
    private final JScrollPane scrollPane;
    private final JButton prevButton;
    private final JButton nextButton;
    private final JButton reserveButton;
    private final JLabel statusLabel;
    private final JPanel northPanel;
    private final JPanel southPanel;

    private static final String[][] ALL_BOOKS = {
        {"Introduction to Java Programming", "Y. Daniel Liang", "2021", "Available"},
        {"Clean Code: Handbook of Agile Software Craftsmanship", "Robert C. Martin", "2008", "Checked Out"},
        {"Design Patterns: Elements of Reusable Object-Oriented Software", "Erich Gamma et al.", "1994", "Available"},
        {"Database System Concepts", "Abraham Silberschatz", "2019", "Available"},
        {"Computer Networking: A Top-Down Approach", "James Kurose", "2020", "Available"},
        {"Modern Operating Systems", "Andrew S. Tanenbaum", "2015", "Reserved"},
        {"Artificial Intelligence: A Modern Approach", "Stuart Russell", "2020", "Available"},
        {"Head First Java", "Kathy Sierra", "2022", "Available"},
        {"Algorithms", "Robert Sedgewick", "2011", "Checked Out"},
        {"Effective Java", "Joshua Bloch", "2018", "Available"}
    };

    public LibrarySearchScreen() {
        super("Campus Central Library - Search & Reservation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 480);
        setMinimumSize(new Dimension(500, 320));
        setLocationRelativeTo(null);

        // Root container uses BorderLayout
        setLayout(new BorderLayout(0, 0));

        // -------------------------------------------------------------
        // North Container: Search bar with FlowLayout (Left-aligned)
        // -------------------------------------------------------------
        northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        northPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 210, 210)));

        northPanel.add(new JLabel("Keyword:"));
        searchField = new JTextField(16);
        northPanel.add(searchField);

        northPanel.add(new JLabel("Category:"));
        categoryCombo = new JComboBox<>(new String[]{"All Categories", "Books", "Journals", "Theses", "Conference Proceedings"});
        northPanel.add(categoryCombo);

        searchButton = new JButton("Search");
        northPanel.add(searchButton);

        add(northPanel, BorderLayout.NORTH);

        // -------------------------------------------------------------
        // Center Container: Expandable results area using JScrollPane
        // -------------------------------------------------------------
        String[] columnNames = {"Title", "Author", "Year", "Availability"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only
            }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.setFillsViewportHeight(true);
        resultsTable.setRowHeight(24);
        resultsTable.getTableHeader().setReorderingAllowed(false);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        add(scrollPane, BorderLayout.CENTER);

        // -------------------------------------------------------------
        // South Container: Pagination controls, actions, and status message
        // -------------------------------------------------------------
        southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 210, 210)));

        JPanel southControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        prevButton = new JButton("< Previous");
        nextButton = new JButton("Next >");
        reserveButton = new JButton("Reserve Selected Book");
        reserveButton.setBackground(new Color(24, 110, 180));
        reserveButton.setForeground(Color.WHITE);

        southControls.add(prevButton);
        southControls.add(nextButton);
        southControls.add(reserveButton);

        statusLabel = new JLabel(" Showing 10 of 10 catalog entries | Ready  ", SwingConstants.RIGHT);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        southPanel.add(southControls, BorderLayout.WEST);
        southPanel.add(statusLabel, BorderLayout.EAST);

        add(southPanel, BorderLayout.SOUTH);

        // Initial Data Load
        loadBooks(null);

        // Listeners
        searchButton.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch());
        reserveButton.addActionListener(e -> performReservation());
    }

    public void loadBooks(String query) {
        tableModel.setRowCount(0);
        int matched = 0;
        for (String[] row : ALL_BOOKS) {
            if (query == null || query.trim().isEmpty() ||
                row[0].toLowerCase().contains(query.toLowerCase()) ||
                row[1].toLowerCase().contains(query.toLowerCase())) {
                tableModel.addRow(row);
                matched++;
            }
        }
        statusLabel.setText(" Showing " + matched + " of " + ALL_BOOKS.length + " catalog entries | Ready  ");
    }

    public void performSearch() {
        String query = searchField.getText();
        loadBooks(query);
    }

    public void performReservation() {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a book from the table to reserve.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String title = (String) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 3);

        if ("Available".equalsIgnoreCase(status)) {
            tableModel.setValueAt("Reserved", selectedRow, 3);
            statusLabel.setText(" Reserved: " + title + "  ");
            JOptionPane.showMessageDialog(this, "Book successfully reserved:\n" + title, "Reservation Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Cannot reserve book. Current status: " + status, "Unavailable", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Accessors for resize verification tests
    public JPanel getNorthPanel() { return northPanel; }
    public JScrollPane getScrollPane() { return scrollPane; }
    public JPanel getSouthPanel() { return southPanel; }
    public JTable getResultsTable() { return resultsTable; }
    public JTextField getSearchField() { return searchField; }
    public JLabel getStatusLabel() { return statusLabel; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibrarySearchScreen screen = new LibrarySearchScreen();
            screen.setVisible(true);
        });
    }
}
