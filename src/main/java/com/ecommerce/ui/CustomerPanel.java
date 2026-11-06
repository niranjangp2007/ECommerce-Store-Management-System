package com.ecommerce.ui;

import com.ecommerce.model.Customer;
import com.ecommerce.service.CustomerService;
import com.ecommerce.service.DatabaseOperationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Enterprise Customer Management screen supporting search, add, update, delete,
 * and foreign-key error alerts with professional styling and aligned modal dialogs.
 */
public class CustomerPanel extends JPanel {

    private final CustomerService customerService;
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JLabel lblCountBadge;
    private JLabel lblResultsInfo;

    private JButton btnEdit;
    private JButton btnDelete;

    public CustomerPanel(CustomerService customerService) {
        this.customerService = customerService;

        setLayout(new BorderLayout(14, 14));
        setBackground(UIUtils.BG_LIGHT);
        setBorder(new EmptyBorder(18, 20, 20, 20));

        initComponents();
        loadCustomers();
    }

    private void initComponents() {
        // Container for Header + Toolbar
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // 1. Page Header
        topContainer.add(createHeaderPanel());
        topContainer.add(Box.createVerticalStrut(14));

        // 2. Toolbar (Search & Actions)
        topContainer.add(createToolbarPanel());
        add(topContainer, BorderLayout.NORTH);

        // 3. Customer Directory Table Card
        String[] columns = {"Customer ID", "Customer Name", "City", "Join Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        customerTable = new JTable(tableModel);
        UIUtils.formatTable(customerTable);

        // Align ID and Join Date to Center
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        customerTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        customerTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        // Enable/disable action buttons based on table row selection
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = customerTable.getSelectedRow() >= 0;
            btnEdit.setEnabled(hasSelection);
            btnDelete.setEnabled(hasSelection);
        });

        // Double-click row to edit
        customerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && customerTable.getSelectedRow() >= 0) {
                    showEditCustomerDialog();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));

        JPanel tableCard = UIUtils.createCard("Customer Directory (customers table)", scrollPane);
        add(tableCard, BorderLayout.CENTER);
    }

    /**
     * Professional page header with title, subtitle, count badge, and refresh button.
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        // Left Header Titles + Badge
        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleBox.setOpaque(false);

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        JLabel title = new JLabel("Customers");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(17, 24, 39));

        JLabel sub = new JLabel("Manage your customer database.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(107, 114, 128));

        textCol.add(title);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(sub);
        titleBox.add(textCol);

        lblCountBadge = UIUtils.createPillBadge("0 Customers", new Color(239, 246, 255), new Color(37, 99, 235));
        titleBox.add(lblCountBadge);

        header.add(titleBox, BorderLayout.WEST);

        // Right Refresh
        JButton btnRefresh = UIUtils.createSecondaryButton("↻ Refresh");
        btnRefresh.setPreferredSize(new Dimension(105, 32));
        btnRefresh.addActionListener(e -> loadCustomers());
        header.add(btnRefresh, BorderLayout.EAST);

        return header;
    }

    /**
     * Compact, enterprise-style toolbar with search and CRUD buttons.
     */
    private JPanel createToolbarPanel() {
        JPanel bar = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(new Color(229, 231, 235));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(10, 14, 10, 14));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        // Left Search Section
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBox.setOpaque(false);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UIUtils.BOLD_FONT);
        lblSearch.setForeground(new Color(55, 65, 81));

        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(220, 30));
        txtSearch.putClientProperty("JTextField.placeholderText", "Search name, ID or city...");
        txtSearch.addActionListener(e -> searchCustomers());

        JButton btnSearch = UIUtils.createSecondaryButton("Search");
        btnSearch.setPreferredSize(new Dimension(75, 30));
        btnSearch.addActionListener(e -> searchCustomers());

        JButton btnClear = UIUtils.createSecondaryButton("Clear");
        btnClear.setPreferredSize(new Dimension(65, 30));
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            loadCustomers();
        });

        lblResultsInfo = new JLabel("");
        lblResultsInfo.setFont(UIUtils.REGULAR_FONT);
        lblResultsInfo.setForeground(new Color(107, 114, 128));

        searchBox.add(lblSearch);
        searchBox.add(txtSearch);
        searchBox.add(btnSearch);
        searchBox.add(btnClear);
        searchBox.add(lblResultsInfo);
        bar.add(searchBox, BorderLayout.WEST);

        // Right Action Buttons
        JPanel actionBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBox.setOpaque(false);

        JButton btnAdd = UIUtils.createPrimaryButton("+ Add Customer");
        btnAdd.setPreferredSize(new Dimension(135, 30));
        btnAdd.addActionListener(e -> showAddCustomerDialog());

        btnEdit = UIUtils.createSecondaryButton("Edit Selected");
        btnEdit.setPreferredSize(new Dimension(115, 30));
        btnEdit.setEnabled(false);
        btnEdit.addActionListener(e -> showEditCustomerDialog());

        btnDelete = UIUtils.createDangerButton("Delete Selected");
        btnDelete.setPreferredSize(new Dimension(130, 30));
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(e -> deleteSelectedCustomer());

        actionBox.add(btnAdd);
        actionBox.add(btnEdit);
        actionBox.add(btnDelete);
        bar.add(actionBox, BorderLayout.EAST);

        return bar;
    }

    public void loadCustomers() {
        tableModel.setRowCount(0);
        try {
            List<Customer> customers = customerService.getAllCustomers();
            for (Customer c : customers) {
                tableModel.addRow(new Object[]{
                        c.getCustomerId(),
                        c.getName(),
                        c.getCity(),
                        c.getJoinDate()
                });
            }
            updateCounts(customers.size(), customers.size());
        } catch (SQLException e) {
            UIUtils.showError(this, "Failed to load customers: " + e.getMessage());
        }
    }

    private void searchCustomers() {
        String query = txtSearch.getText().trim();
        tableModel.setRowCount(0);
        try {
            List<Customer> customers = customerService.searchCustomers(query);
            for (Customer c : customers) {
                tableModel.addRow(new Object[]{
                        c.getCustomerId(),
                        c.getName(),
                        c.getCity(),
                        c.getJoinDate()
                });
            }
            int total = customerService.getCustomerCount();
            updateCounts(total, customers.size());
        } catch (SQLException e) {
            UIUtils.showError(this, "Search failed: " + e.getMessage());
        }
    }

    private void updateCounts(int total, int displayed) {
        lblCountBadge.setText(total + " Customers Total");
        if (displayed < total) {
            lblResultsInfo.setText("Showing " + displayed + " of " + total);
        } else {
            lblResultsInfo.setText("Showing all " + total + " customers");
        }
    }

    /**
     * Professional, structured modal dialog for adding a new customer.
     */
    private void showAddCustomerDialog() {
        int nextId = 1;
        try {
            nextId = customerService.getNextCustomerId();
        } catch (SQLException ignored) {
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Add New Customer", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        // Dialog Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(24, 34, 54));
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel("Create Customer Record");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblDesc = new JLabel("Enter personal details and registration date.");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(new Color(180, 195, 215));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblDesc);
        header.add(titleBox, BorderLayout.WEST);
        dialog.add(header, BorderLayout.NORTH);

        // Form Grid
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 16, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtId = new JTextField(String.valueOf(nextId), 18);
        txtId.setPreferredSize(new Dimension(220, 30));

        JTextField txtName = new JTextField(18);
        txtName.setPreferredSize(new Dimension(220, 30));

        JTextField txtCity = new JTextField(18);
        txtCity.setPreferredSize(new Dimension(220, 30));

        JTextField txtJoinDate = new JTextField(LocalDate.now().toString(), 18);
        txtJoinDate.setPreferredSize(new Dimension(220, 30));

        JLabel lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblError.setForeground(UIUtils.DANGER_COLOR);

        // Row 0: Customer ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(new JLabel("Customer ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtId, gbc);

        // Row 1: Full Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("Full Name: *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtName, gbc);

        // Row 2: City
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        form.add(new JLabel("City: *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtCity, gbc);

        // Row 3: Join Date
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        form.add(new JLabel("Join Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtJoinDate, gbc);

        // Row 4: Inline Error
        gbc.gridx = 1; gbc.gridy = 4;
        form.add(lblError, gbc);

        dialog.add(form, BorderLayout.CENTER);

        // Button Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(new Color(245, 247, 250));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));

        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(90, 32));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = UIUtils.createPrimaryButton("Save Customer");
        btnSave.setPreferredSize(new Dimension(130, 32));
        btnSave.addActionListener(e -> {
            try {
                String idStr = txtId.getText().trim();
                String name = txtName.getText().trim();
                String city = txtCity.getText().trim();
                String dateStr = txtJoinDate.getText().trim();

                if (name.isEmpty()) {
                    lblError.setText("Customer name is required.");
                    return;
                }
                if (city.isEmpty()) {
                    lblError.setText("City is required.");
                    return;
                }

                int id = Integer.parseInt(idStr);
                LocalDate joinDate = LocalDate.parse(dateStr);

                customerService.addCustomer(id, name, city, joinDate);
                dialog.dispose();
                UIUtils.showInfo(this, "Customer #" + id + " ('" + name + "') added successfully!");
                loadCustomers();

            } catch (NumberFormatException ex) {
                lblError.setText("Customer ID must be a valid positive integer.");
            } catch (DateTimeParseException ex) {
                lblError.setText("Date must be formatted YYYY-MM-DD (e.g. 2026-03-15).");
            } catch (DatabaseOperationException | SQLException ex) {
                lblError.setText(ex.getMessage());
            }
        });

        footer.add(btnCancel);
        footer.add(btnSave);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Professional, structured modal dialog for editing an existing customer.
     */
    private void showEditCustomerDialog() {
        int row = customerTable.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select a customer from the table to edit.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String currentName = (String) tableModel.getValueAt(row, 1);
        String currentCity = (String) tableModel.getValueAt(row, 2);
        LocalDate currentDate = (LocalDate) tableModel.getValueAt(row, 3);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Edit Customer #" + id, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(24, 34, 54));
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel("Edit Customer #" + id);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblDesc = new JLabel("Modify customer profile details.");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(new Color(180, 195, 215));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblDesc);
        header.add(titleBox, BorderLayout.WEST);
        dialog.add(header, BorderLayout.NORTH);

        // Form Grid
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 16, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtId = new JTextField(String.valueOf(id), 18);
        txtId.setPreferredSize(new Dimension(220, 30));
        txtId.setEnabled(false); // ID is primary key immutable

        JTextField txtName = new JTextField(currentName, 18);
        txtName.setPreferredSize(new Dimension(220, 30));

        JTextField txtCity = new JTextField(currentCity, 18);
        txtCity.setPreferredSize(new Dimension(220, 30));

        JTextField txtJoinDate = new JTextField(currentDate.toString(), 18);
        txtJoinDate.setPreferredSize(new Dimension(220, 30));

        JLabel lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblError.setForeground(UIUtils.DANGER_COLOR);

        // Layout rows
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(new JLabel("Customer ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("Full Name: *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        form.add(new JLabel("City: *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtCity, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        form.add(new JLabel("Join Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtJoinDate, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        form.add(lblError, gbc);

        dialog.add(form, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(new Color(245, 247, 250));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));

        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(90, 32));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = UIUtils.createPrimaryButton("Save Changes");
        btnSave.setPreferredSize(new Dimension(130, 32));
        btnSave.addActionListener(e -> {
            try {
                String name = txtName.getText().trim();
                String city = txtCity.getText().trim();
                String dateStr = txtJoinDate.getText().trim();

                if (name.isEmpty()) {
                    lblError.setText("Customer name cannot be empty.");
                    return;
                }
                if (city.isEmpty()) {
                    lblError.setText("City cannot be empty.");
                    return;
                }

                LocalDate joinDate = LocalDate.parse(dateStr);
                customerService.updateCustomer(id, name, city, joinDate);
                dialog.dispose();
                UIUtils.showInfo(this, "Customer #" + id + " updated successfully!");
                loadCustomers();

            } catch (DateTimeParseException ex) {
                lblError.setText("Date must be formatted YYYY-MM-DD.");
            } catch (DatabaseOperationException | SQLException ex) {
                lblError.setText(ex.getMessage());
            }
        });

        footer.add(btnCancel);
        footer.add(btnSave);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Confirms customer deletion and preserves database foreign-key integrity constraints.
     */
    private void deleteSelectedCustomer() {
        int row = customerTable.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select a customer from the table to delete.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        boolean confirm = UIUtils.confirmAction(this,
                "Are you sure you want to delete customer #" + id + " ('" + name + "')?\n\n" +
                "Database Integrity: Deletion is blocked if past order transactions reference this customer.",
                "Confirm Customer Deletion");

        if (confirm) {
            try {
                customerService.deleteCustomer(id);
                UIUtils.showInfo(this, "Customer #" + id + " deleted successfully.");
                loadCustomers();
            } catch (DatabaseOperationException e) {
                UIUtils.showError(this, e.getMessage());
            } catch (SQLException e) {
                UIUtils.showError(this, "Database error: " + e.getMessage());
            }
        }
    }
}
