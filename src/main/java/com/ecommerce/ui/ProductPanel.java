package com.ecommerce.ui;

import com.ecommerce.model.Product;
import com.ecommerce.service.DatabaseOperationException;
import com.ecommerce.service.ProductService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Enterprise Product & Inventory Management screen.
 * Features:
 * - Real-time inventory KPI metric cards (Total, In Stock, Low Stock, Out of Stock)
 * - Multi-filter toolbar (Keyword search, category dropdown, out-of-stock toggle)
 * - Enhanced data table with formatted currency and 3-tier status badges
 * - Professional modal dialogs for product addition and updates with input validation
 * - Foreign-key deletion protection with helpful diagnostic alerts
 */
public class ProductPanel extends JPanel {

    private final ProductService productService;

    // Table & Models
    private JTable productTable;
    private DefaultTableModel tableModel;

    // Filters & Inputs
    private JTextField txtSearch;
    private JComboBox<String> cmbCategory;
    private JCheckBox chkOutOfStockOnly;
    private JLabel lblTotalBadge;
    private JLabel lblResultsInfo;

    // Metric Labels
    private JLabel lblTotalCount;
    private JLabel lblInStockCount;
    private JLabel lblLowStockCount;
    private JLabel lblOutStockCount;

    // Action Buttons
    private JButton btnEdit;
    private JButton btnDelete;

    public ProductPanel(ProductService productService) {
        this.productService = productService;

        setLayout(new BorderLayout(14, 14));
        setBackground(UIUtils.BG_LIGHT);
        setBorder(new EmptyBorder(18, 20, 20, 20));

        initComponents();
        loadCategories();
        loadProducts();
    }

    private void initComponents() {
        // Container for Header + Metric Cards + Toolbar
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // 1. Header
        topContainer.add(createHeaderPanel());
        topContainer.add(Box.createVerticalStrut(14));

        // 2. Metric Cards Grid
        topContainer.add(createMetricCardsGrid());
        topContainer.add(Box.createVerticalStrut(14));

        // 3. Toolbar
        topContainer.add(createToolbarPanel());
        add(topContainer, BorderLayout.NORTH);

        // 4. Products Catalog Table Card
        String[] columns = {"Product ID", "Product Name", "Category", "Price (₹)", "Stock Units", "Stock Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        productTable = new JTable(tableModel);
        UIUtils.formatTable(productTable);

        // Column Alignment & Badge Renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        productTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Currency Formatted Price Column (Right Aligned)
        productTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.RIGHT);
                setBorder(new EmptyBorder(0, 8, 0, 16));
                return c;
            }
        });

        // 3-Tier Stock Status Renderer (In Stock: Green, Low Stock: Orange, Out of Stock: Red)
        productTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                String val = String.valueOf(value);

                if (val.contains("OUT OF STOCK")) {
                    setForeground(UIUtils.DANGER_COLOR);
                    setFont(UIUtils.BOLD_FONT);
                } else if (val.contains("Low Stock")) {
                    setForeground(UIUtils.WARNING_COLOR);
                    setFont(UIUtils.BOLD_FONT);
                } else {
                    setForeground(UIUtils.SUCCESS_COLOR);
                    setFont(UIUtils.BOLD_FONT);
                }
                return c;
            }
        });

        // Enable/disable buttons based on row selection
        productTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = productTable.getSelectedRow() >= 0;
            btnEdit.setEnabled(hasSelection);
            btnDelete.setEnabled(hasSelection);
        });

        // Double click to edit
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && productTable.getSelectedRow() >= 0) {
                    showEditProductDialog();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));

        JPanel tableCard = UIUtils.createCard("Inventory Catalog (products table)", scrollPane);
        add(tableCard, BorderLayout.CENTER);
    }

    /**
     * Professional Header Panel.
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleBox.setOpaque(false);

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        JLabel title = new JLabel("Products & Inventory");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(17, 24, 39));

        JLabel sub = new JLabel("Manage catalog, pricing and stock.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(107, 114, 128));

        textCol.add(title);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(sub);
        titleBox.add(textCol);

        lblTotalBadge = UIUtils.createPillBadge("0 Products", new Color(239, 246, 255), new Color(37, 99, 235));
        titleBox.add(lblTotalBadge);

        header.add(titleBox, BorderLayout.WEST);

        JButton btnRefresh = UIUtils.createSecondaryButton("↻ Refresh");
        btnRefresh.setPreferredSize(new Dimension(105, 32));
        btnRefresh.addActionListener(e -> {
            loadCategories();
            loadProducts();
        });
        header.add(btnRefresh, BorderLayout.EAST);

        return header;
    }

    /**
     * Four Compact Metric Cards for Inventory Overview.
     */
    private JPanel createMetricCardsGrid() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 12, 12));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

        lblTotalCount = new JLabel("—");
        lblInStockCount = new JLabel("—");
        lblLowStockCount = new JLabel("—");
        lblOutStockCount = new JLabel("—");

        grid.add(createMiniCard("TOTAL PRODUCTS", lblTotalCount, "Catalog Items", new Color(37, 99, 235)));
        grid.add(createMiniCard("IN STOCK", lblInStockCount, "Healthy Inventory", new Color(5, 150, 105)));
        grid.add(createMiniCard("LOW STOCK (≤ 5)", lblLowStockCount, "Reorder Soon", new Color(217, 119, 6)));
        grid.add(createMiniCard("OUT OF STOCK", lblOutStockCount, "Needs Restocking", new Color(220, 38, 38)));

        return grid;
    }

    private JPanel createMiniCard(String title, JLabel valueLabel, String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(6, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));
                g2.setColor(new Color(229, 231, 235));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(8, 14, 8, 14));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitle.setForeground(new Color(107, 114, 128));
        card.add(lblTitle, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(new Color(17, 24, 39));
        card.add(valueLabel, BorderLayout.CENTER);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(new Color(156, 163, 175));
        card.add(lblSub, BorderLayout.SOUTH);

        return card;
    }

    /**
     * Compact, responsive Toolbar with search, category filtering, and action buttons.
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
        bar.setBorder(new EmptyBorder(8, 14, 8, 14));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        // Filters (Left)
        JPanel filterBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBox.setOpaque(false);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UIUtils.BOLD_FONT);
        lblSearch.setForeground(new Color(55, 65, 81));

        txtSearch = new JTextField(14);
        txtSearch.setPreferredSize(new Dimension(170, 30));
        txtSearch.putClientProperty("JTextField.placeholderText", "Product name / ID...");
        txtSearch.addActionListener(e -> applyFilter());

        cmbCategory = new JComboBox<>(new String[]{"All Categories"});
        cmbCategory.setPreferredSize(new Dimension(150, 30));
        cmbCategory.addActionListener(e -> applyFilter());

        chkOutOfStockOnly = new JCheckBox("Out of Stock Only");
        chkOutOfStockOnly.setOpaque(false);
        chkOutOfStockOnly.setFont(UIUtils.BOLD_FONT);
        chkOutOfStockOnly.setForeground(UIUtils.DANGER_COLOR);
        chkOutOfStockOnly.addActionListener(e -> applyFilter());

        JButton btnFilter = UIUtils.createSecondaryButton("Filter");
        btnFilter.setPreferredSize(new Dimension(70, 30));
        btnFilter.addActionListener(e -> applyFilter());

        JButton btnReset = UIUtils.createSecondaryButton("Reset");
        btnReset.setPreferredSize(new Dimension(65, 30));
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbCategory.setSelectedIndex(0);
            chkOutOfStockOnly.setSelected(false);
            loadProducts();
        });

        lblResultsInfo = new JLabel("");
        lblResultsInfo.setFont(UIUtils.REGULAR_FONT);
        lblResultsInfo.setForeground(new Color(107, 114, 128));

        filterBox.add(lblSearch);
        filterBox.add(txtSearch);
        filterBox.add(new JLabel("Category:"));
        filterBox.add(cmbCategory);
        filterBox.add(chkOutOfStockOnly);
        filterBox.add(btnFilter);
        filterBox.add(btnReset);
        filterBox.add(lblResultsInfo);
        bar.add(filterBox, BorderLayout.WEST);

        // Actions (Right)
        JPanel actionBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBox.setOpaque(false);

        JButton btnAdd = UIUtils.createPrimaryButton("+ Add Product");
        btnAdd.setPreferredSize(new Dimension(125, 30));
        btnAdd.addActionListener(e -> showAddProductDialog());

        btnEdit = UIUtils.createSecondaryButton("Edit Selected");
        btnEdit.setPreferredSize(new Dimension(110, 30));
        btnEdit.setEnabled(false);
        btnEdit.addActionListener(e -> showEditProductDialog());

        btnDelete = UIUtils.createDangerButton("Delete Selected");
        btnDelete.setPreferredSize(new Dimension(125, 30));
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(e -> deleteSelectedProduct());

        actionBox.add(btnAdd);
        actionBox.add(btnEdit);
        actionBox.add(btnDelete);
        bar.add(actionBox, BorderLayout.EAST);

        return bar;
    }

    private void loadCategories() {
        try {
            String current = (String) cmbCategory.getSelectedItem();
            cmbCategory.removeAllItems();
            cmbCategory.addItem("All Categories");
            List<String> categories = productService.getAllCategories();
            for (String cat : categories) {
                cmbCategory.addItem(cat);
            }
            if (current != null) {
                cmbCategory.setSelectedItem(current);
            }
        } catch (SQLException ignored) {
        }
    }

    public void loadProducts() {
        tableModel.setRowCount(0);
        try {
            List<Product> products = productService.getAllProducts();
            for (Product p : products) {
                addRow(p);
            }
            updateMetrics(products, products.size());
        } catch (SQLException e) {
            UIUtils.showError(this, "Failed to load products: " + e.getMessage());
        }
    }

    private void applyFilter() {
        tableModel.setRowCount(0);
        try {
            List<Product> displayed;
            if (chkOutOfStockOnly.isSelected()) {
                displayed = productService.getOutOfStockProducts();
            } else {
                String query = txtSearch.getText().trim();
                String category = (String) cmbCategory.getSelectedItem();
                displayed = productService.searchProducts(query, category);
            }
            for (Product p : displayed) {
                addRow(p);
            }

            List<Product> all = productService.getAllProducts();
            updateMetrics(all, displayed.size());

        } catch (SQLException e) {
            UIUtils.showError(this, "Filter query failed: " + e.getMessage());
        }
    }

    private void updateMetrics(List<Product> allProducts, int displayedCount) {
        int total = allProducts.size();
        int inStock = 0;
        int lowStock = 0;
        int outStock = 0;

        for (Product p : allProducts) {
            if (p.getStock() <= 0) {
                outStock++;
            } else if (p.getStock() <= 5) {
                lowStock++;
            } else {
                inStock++;
            }
        }

        lblTotalCount.setText(String.valueOf(total));
        lblInStockCount.setText(String.valueOf(inStock));
        lblLowStockCount.setText(String.valueOf(lowStock));
        lblOutStockCount.setText(String.valueOf(outStock));

        lblTotalBadge.setText(total + " Products Total");

        if (displayedCount < total) {
            lblResultsInfo.setText("Showing " + displayedCount + " of " + total);
        } else {
            lblResultsInfo.setText("Showing all " + total);
        }
    }

    private void addRow(Product p) {
        String status;
        if (p.getStock() <= 0) {
            status = "OUT OF STOCK";
        } else if (p.getStock() <= 5) {
            status = "Low Stock (" + p.getStock() + ")";
        } else {
            status = "In Stock";
        }

        tableModel.addRow(new Object[]{
                p.getProductId(),
                p.getName(),
                p.getCategory(),
                String.format("₹%.2f", p.getPrice()),
                p.getStock(),
                status
        });
    }

    /**
     * Professional, structured modal dialog for adding a new product.
     */
    private void showAddProductDialog() {
        int nextId = 1;
        try {
            nextId = productService.getNextProductId();
        } catch (SQLException ignored) {
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Add New Product", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(24, 34, 54));
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel("Create Product Catalog Item");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblDesc = new JLabel("Specify pricing, category classification and inventory stock.");
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

        JTextField txtCategory = new JTextField(18);
        txtCategory.setPreferredSize(new Dimension(220, 30));

        JTextField txtPrice = new JTextField("0.00", 18);
        txtPrice.setPreferredSize(new Dimension(220, 30));

        JTextField txtStock = new JTextField("0", 18);
        txtStock.setPreferredSize(new Dimension(220, 30));

        JLabel lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblError.setForeground(UIUtils.DANGER_COLOR);

        // Rows
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(new JLabel("Product ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("Product Name: *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        form.add(new JLabel("Category: *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtCategory, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        form.add(new JLabel("Price (₹): *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtPrice, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        form.add(new JLabel("Stock Quantity: *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtStock, gbc);

        gbc.gridx = 1; gbc.gridy = 5;
        form.add(lblError, gbc);

        dialog.add(form, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(new Color(245, 247, 250));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));

        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(90, 32));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = UIUtils.createPrimaryButton("Save Product");
        btnSave.setPreferredSize(new Dimension(130, 32));
        btnSave.addActionListener(e -> {
            try {
                int id = Integer.parseInt(txtId.getText().trim());
                String name = txtName.getText().trim();
                String category = txtCategory.getText().trim();
                BigDecimal price = new BigDecimal(txtPrice.getText().trim());
                int stock = Integer.parseInt(txtStock.getText().trim());

                if (name.isEmpty()) {
                    lblError.setText("Product name is required.");
                    return;
                }
                if (category.isEmpty()) {
                    lblError.setText("Category is required.");
                    return;
                }

                productService.addProduct(id, name, category, price, stock);
                dialog.dispose();
                UIUtils.showInfo(this, "Product #" + id + " ('" + name + "') added successfully!");
                loadCategories();
                loadProducts();

            } catch (NumberFormatException ex) {
                lblError.setText("Invalid number format for ID, Price, or Stock.");
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
     * Professional, structured modal dialog for editing an existing product.
     */
    private void showEditProductDialog() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select a product from the table to edit.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String currentName = (String) tableModel.getValueAt(row, 1);
        String currentCategory = (String) tableModel.getValueAt(row, 2);
        String priceStr = String.valueOf(tableModel.getValueAt(row, 3)).replace("₹", "").trim();
        int currentStock = (int) tableModel.getValueAt(row, 4);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Edit Product #" + id, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(24, 34, 54));
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel("Edit Product #" + id);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblDesc = new JLabel("Update product details, pricing or stock levels.");
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
        txtId.setEnabled(false); // ID is immutable primary key

        JTextField txtName = new JTextField(currentName, 18);
        txtName.setPreferredSize(new Dimension(220, 30));

        JTextField txtCategory = new JTextField(currentCategory, 18);
        txtCategory.setPreferredSize(new Dimension(220, 30));

        JTextField txtPrice = new JTextField(priceStr, 18);
        txtPrice.setPreferredSize(new Dimension(220, 30));

        JTextField txtStock = new JTextField(String.valueOf(currentStock), 18);
        txtStock.setPreferredSize(new Dimension(220, 30));

        JLabel lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblError.setForeground(UIUtils.DANGER_COLOR);

        // Rows
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(new JLabel("Product ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("Product Name: *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        form.add(new JLabel("Category: *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtCategory, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        form.add(new JLabel("Price (₹): *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtPrice, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        form.add(new JLabel("Stock Quantity: *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtStock, gbc);

        gbc.gridx = 1; gbc.gridy = 5;
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
                String category = txtCategory.getText().trim();
                BigDecimal price = new BigDecimal(txtPrice.getText().trim());
                int stock = Integer.parseInt(txtStock.getText().trim());

                if (name.isEmpty()) {
                    lblError.setText("Product name cannot be empty.");
                    return;
                }
                if (category.isEmpty()) {
                    lblError.setText("Category cannot be empty.");
                    return;
                }

                productService.updateProduct(id, name, category, price, stock);
                dialog.dispose();
                UIUtils.showInfo(this, "Product #" + id + " updated successfully!");
                loadCategories();
                loadProducts();

            } catch (NumberFormatException ex) {
                lblError.setText("Invalid number format for Price or Stock.");
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
     * Deletes a selected product with database foreign-key integrity alert protection.
     */
    private void deleteSelectedProduct() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select a product from the table to delete.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        boolean confirm = UIUtils.confirmAction(this,
                "Are you sure you want to delete product #" + id + " ('" + name + "')?\n\n" +
                "Database Integrity: Deletion is blocked if existing order item records reference this product.",
                "Confirm Product Deletion");

        if (confirm) {
            try {
                productService.deleteProduct(id);
                UIUtils.showInfo(this, "Product #" + id + " deleted successfully.");
                loadCategories();
                loadProducts();
            } catch (DatabaseOperationException e) {
                UIUtils.showError(this, e.getMessage());
            } catch (SQLException e) {
                UIUtils.showError(this, "Database error: " + e.getMessage());
            }
        }
    }
}
