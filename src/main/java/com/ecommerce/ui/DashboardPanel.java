package com.ecommerce.ui;

import com.ecommerce.database.DBConnection;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.service.CustomerService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Enterprise Dashboard Panel for E-Commerce Store Management.
 * Features:
 * - Real-time store KPI metric cards with vector icons and accent badges
 * - Interactive Order Status Distribution table with live percentages
 * - Recent Orders table with status color badges
 * - Low-stock & inventory alert tracking (stock <= 5)
 * - Compact, ergonomic quick action toolbar
 * - Proactive database connection diagnostic banner with retry actions
 * - Resilient, non-swallowing error handling (no raw '?' markers or masked errors)
 */
public class DashboardPanel extends JPanel {

    private final CustomerService customerService;
    private final ProductService productService;
    private final OrderService orderService;
    private final MainFrame mainFrame;

    // Error / Diagnostic Banner
    private JPanel errorBanner;
    private JLabel lblErrorText;

    // Metric Value Labels
    private JLabel lblCustomersCount;
    private JLabel lblProductsCount;
    private JLabel lblOutOfStockCount;
    private JLabel lblOrdersCount;

    // Tables & Models
    private JTable statusTable;
    private DefaultTableModel statusTableModel;

    private JTable recentOrdersTable;
    private DefaultTableModel recentOrdersModel;

    private JTable lowStockTable;
    private DefaultTableModel lowStockModel;

    public DashboardPanel(MainFrame mainFrame, CustomerService customerService,
                          ProductService productService, OrderService orderService) {
        this.mainFrame = mainFrame;
        this.customerService = customerService;
        this.productService = productService;
        this.orderService = orderService;

        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_LIGHT);

        initComponents();
        refreshData();
    }

    private void initComponents() {
        JPanel contentBox = new JPanel();
        contentBox.setLayout(new BoxLayout(contentBox, BoxLayout.Y_AXIS));
        contentBox.setBackground(UIUtils.BG_LIGHT);
        contentBox.setBorder(new EmptyBorder(18, 20, 20, 20));

        // 1. Header with Refresh Button
        contentBox.add(createHeaderPanel());
        contentBox.add(Box.createVerticalStrut(12));

        // 2. Connection Warning / Error Banner (Dynamically shown/hidden)
        errorBanner = createErrorBanner();
        contentBox.add(errorBanner);
        contentBox.add(Box.createVerticalStrut(14));

        // 3. Four Compact Metric KPI Cards
        contentBox.add(createMetricsGrid());
        contentBox.add(Box.createVerticalStrut(14));

        // 4. Compact Quick Actions Toolbar
        contentBox.add(createQuickActionsBar());
        contentBox.add(Box.createVerticalStrut(16));

        // 5. Main Content: 2-Column Responsive Data Grid
        contentBox.add(createDataGrid());

        // Wrap in clean, borderless scroll pane for smooth window resizing
        JScrollPane scrollPane = new JScrollPane(contentBox);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Top header bar with greeting and refresh trigger.
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JPanel titleCol = new JPanel();
        titleCol.setLayout(new BoxLayout(titleCol, BoxLayout.Y_AXIS));
        titleCol.setOpaque(false);

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(17, 24, 39));

        JLabel subtitle = new JLabel("Welcome to Store Operations Console • Overview & Inventory Status");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(107, 114, 128));

        titleCol.add(title);
        titleCol.add(Box.createVerticalStrut(2));
        titleCol.add(subtitle);
        header.add(titleCol, BorderLayout.WEST);

        // Compact Refresh Button (Aligned top-right, approx 40–42px high)
        JButton btnRefresh = UIUtils.createSecondaryButton("↻ Refresh Data");
        btnRefresh.setPreferredSize(new Dimension(130, 40));
        btnRefresh.setMaximumSize(new Dimension(130, 42));
        btnRefresh.addActionListener(e -> {
            mainFrame.updateDbStatus();
            refreshData();
        });

        JPanel rightBox = new JPanel(new GridBagLayout());
        rightBox.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        rightBox.add(btnRefresh, gbc);

        header.add(rightBox, BorderLayout.EAST);

        return header;
    }

    /**
     * Diagnostic error banner displayed when the database is unreachable.
     */
    private JPanel createErrorBanner() {
        JPanel banner = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(254, 242, 242)); // Soft alert red
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(new Color(252, 165, 165));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        banner.setOpaque(false);
        banner.setBorder(new EmptyBorder(10, 14, 10, 14));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        banner.setVisible(false); // Hidden when connection is healthy

        JPanel msgBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        msgBox.setOpaque(false);

        JLabel icon = new JLabel(UIUtils.createNavIcon("ALERT", 18, new Color(220, 38, 38)));
        lblErrorText = new JLabel("Database connection offline: Unable to connect to MySQL on localhost:3306.");
        lblErrorText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblErrorText.setForeground(new Color(153, 27, 27));

        msgBox.add(icon);
        msgBox.add(lblErrorText);
        banner.add(msgBox, BorderLayout.WEST);

        JButton btnRetry = UIUtils.createDangerButton("↻ Recheck Database");
        btnRetry.setPreferredSize(new Dimension(150, 28));
        btnRetry.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnRetry.addActionListener(e -> {
            mainFrame.updateDbStatus();
            refreshData();
        });
        banner.add(btnRetry, BorderLayout.EAST);

        return banner;
    }

    /**
     * 1x4 Compact KPI Metric Cards Grid.
     */
    private JPanel createMetricsGrid() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 14, 14));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));

        lblCustomersCount = new JLabel("—");
        lblProductsCount = new JLabel("—");
        lblOutOfStockCount = new JLabel("—");
        lblOrdersCount = new JLabel("—");

        grid.add(createMetricCard("TOTAL CUSTOMERS", lblCustomersCount, "Active Buyers", "CUSTOMERS", new Color(37, 99, 235)));
        grid.add(createMetricCard("TOTAL PRODUCTS", lblProductsCount, "Catalog Items", "PRODUCTS", new Color(5, 150, 105)));
        grid.add(createMetricCard("OUT OF STOCK", lblOutOfStockCount, "Needs Restocking", "ALERT", new Color(220, 38, 38)));
        grid.add(createMetricCard("TOTAL ORDERS", lblOrdersCount, "Lifetime Placed", "ORDER_HISTORY", new Color(124, 58, 237)));

        return grid;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String subtitle, String iconType, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(8, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                // Left 4px accent indicator
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));
                // Subtle border
                g2.setColor(new Color(229, 231, 235));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        // Top Row: Title + Icon Badge
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitle.setForeground(new Color(107, 114, 128));
        topRow.add(lblTitle, BorderLayout.WEST);

        JLabel iconBadge = new JLabel(UIUtils.createNavIcon(iconType, 16, accentColor));
        topRow.add(iconBadge, BorderLayout.EAST);
        card.add(topRow, BorderLayout.NORTH);

        // Center: Value Display
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(new Color(17, 24, 39));
        card.add(valueLabel, BorderLayout.CENTER);

        // Bottom: Subtitle
        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(156, 163, 175));
        card.add(lblSub, BorderLayout.SOUTH);

        return card;
    }

    /**
     * Compact Quick Actions Toolbar with well-proportioned buttons (no oversized tiles).
     */
    private JPanel createQuickActionsBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
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
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftBox.setOpaque(false);

        JLabel lblActionTitle = new JLabel("Quick Actions:");
        lblActionTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblActionTitle.setForeground(new Color(55, 65, 81));
        leftBox.add(lblActionTitle);

        JButton btnNewOrder = UIUtils.createPrimaryButton("+ Create New Order");
        btnNewOrder.setPreferredSize(new Dimension(160, 30));
        btnNewOrder.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNewOrder.addActionListener(e -> mainFrame.switchToTab(MainFrame.TAB_CREATE_ORDER));

        JButton btnManageProducts = UIUtils.createSecondaryButton("Manage Inventory");
        btnManageProducts.setPreferredSize(new Dimension(145, 30));
        btnManageProducts.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnManageProducts.addActionListener(e -> mainFrame.switchToTab(MainFrame.TAB_PRODUCTS));

        JButton btnManageCustomers = UIUtils.createSecondaryButton("Manage Customers");
        btnManageCustomers.setPreferredSize(new Dimension(145, 30));
        btnManageCustomers.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnManageCustomers.addActionListener(e -> mainFrame.switchToTab(MainFrame.TAB_CUSTOMERS));

        leftBox.add(btnNewOrder);
        leftBox.add(btnManageProducts);
        leftBox.add(btnManageCustomers);

        bar.add(leftBox, BorderLayout.WEST);
        return bar;
    }

    /**
     * 2-Column Adaptive Data Grid containing:
     * - Left: Order Distribution by Status + Low Stock Inventory Alerts
     * - Right: Recent Orders Overview (Top 5)
     */
    private JPanel createDataGrid() {
        JPanel grid = new JPanel(new GridLayout(1, 2, 14, 14));
        grid.setOpaque(false);

        // ---------------- Left Column ----------------
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        // 1. Order Status Distribution Table
        statusTableModel = new DefaultTableModel(new String[]{"Order Status", "Total Orders", "% Share"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        statusTable = new JTable(statusTableModel);
        UIUtils.formatTable(statusTable);
        applyStatusBadgeRenderer(statusTable, 0);

        JScrollPane scrollStatus = new JScrollPane(statusTable);
        scrollStatus.setPreferredSize(new Dimension(400, 140));
        leftCol.add(UIUtils.createCard("Order Distribution by Status", scrollStatus));
        leftCol.add(Box.createVerticalStrut(14));

        // 2. Low Stock & Inventory Alerts Table
        lowStockModel = new DefaultTableModel(new String[]{"ID", "Product Name", "Category", "Stock", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        lowStockTable = new JTable(lowStockModel);
        UIUtils.formatTable(lowStockTable);
        applyStockAlertRenderer(lowStockTable, 4);

        JScrollPane scrollLowStock = new JScrollPane(lowStockTable);
        scrollLowStock.setPreferredSize(new Dimension(400, 160));
        leftCol.add(UIUtils.createCard("Inventory & Low Stock Alerts (Stock ≤ 5)", scrollLowStock));

        grid.add(leftCol);

        // ---------------- Right Column ----------------
        JPanel rightCol = new JPanel(new BorderLayout());
        rightCol.setOpaque(false);

        recentOrdersModel = new DefaultTableModel(new String[]{"Order ID", "Customer", "Date", "Status", "Total (₹)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recentOrdersTable = new JTable(recentOrdersModel);
        UIUtils.formatTable(recentOrdersTable);
        applyStatusBadgeRenderer(recentOrdersTable, 3);

        JScrollPane scrollRecent = new JScrollPane(recentOrdersTable);
        rightCol.add(UIUtils.createCard("Recent Orders (Latest 5 Transactions)", scrollRecent), BorderLayout.CENTER);

        grid.add(rightCol);

        return grid;
    }

    /**
     * Refreshes all metrics, tables, and distribution charts with live database data.
     * Never swallows exceptions or displays raw '?'.
     */
    public void refreshData() {
        try {
            DBConnection.ConnectionDiagnostic diag = DBConnection.diagnoseConnection();
            if (!diag.isSuccess()) {
                showConnectionError(diag.getMessage());
                setOfflineFallbackData();
                return;
            }

            hideConnectionError();

            // 1. Fetch live metrics
            int customers = customerService.getCustomerCount();
            int products = productService.getProductCount();
            int outStock = productService.getOutOfStockCount();
            int orders = orderService.getOrderCount();

            lblCustomersCount.setText(String.valueOf(customers));
            lblProductsCount.setText(String.valueOf(products));
            lblOutOfStockCount.setText(String.valueOf(outStock));
            lblOrdersCount.setText(String.valueOf(orders));

            // 2. Populate Order Distribution by Status
            statusTableModel.setRowCount(0);
            Map<String, Integer> counts = orderService.getOrderCountsByStatus();
            int totalStatusOrders = counts.values().stream().mapToInt(Integer::intValue).sum();
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                int count = entry.getValue();
                String pct = totalStatusOrders > 0
                        ? String.format("%.1f%%", (count * 100.0) / totalStatusOrders)
                        : "0.0%";
                statusTableModel.addRow(new Object[]{entry.getKey(), count, pct});
            }
            if (counts.isEmpty()) {
                statusTableModel.addRow(new Object[]{"No Orders Yet", 0, "0.0%"});
            }

            // 3. Populate Recent Orders (Top 5)
            recentOrdersModel.setRowCount(0);
            List<Order> allOrders = orderService.getAllOrders();
            int limit = Math.min(5, allOrders.size());
            for (int i = 0; i < limit; i++) {
                Order o = allOrders.get(i);
                recentOrdersModel.addRow(new Object[]{
                        "#" + o.getOrderId(),
                        o.getCustomerName() != null ? o.getCustomerName() : "Customer #" + o.getCustomerId(),
                        o.getOrderDate(),
                        o.getStatus(),
                        o.calculateTotal()
                });
            }
            if (allOrders.isEmpty()) {
                recentOrdersModel.addRow(new Object[]{"—", "No recent orders placed", "—", "—", "₹0.00"});
            }

            // 4. Populate Low Stock Alerts (Stock <= 5)
            lowStockModel.setRowCount(0);
            List<Product> allProducts = productService.getAllProducts();
            int lowStockCount = 0;
            for (Product p : allProducts) {
                if (p.getStock() <= 5) {
                    lowStockModel.addRow(new Object[]{
                            "#" + p.getProductId(),
                            p.getName(),
                            p.getCategory(),
                            p.getStock(),
                            p.isOutOfStock() ? "OUT OF STOCK" : "Low Stock (" + p.getStock() + ")"
                    });
                    lowStockCount++;
                }
            }
            if (lowStockCount == 0) {
                lowStockModel.addRow(new Object[]{"✔", "All products have healthy inventory levels", "—", "—", "Healthy"});
            }

        } catch (SQLException e) {
            showConnectionError("Database query failed: " + e.getMessage());
            setOfflineFallbackData();
        }
    }

    /**
     * Sets clean em-dash markers and descriptive placeholder rows when database is offline.
     * Prevents raw '?' from appearing and never masks database state with fake values.
     */
    private void setOfflineFallbackData() {
        lblCustomersCount.setText("—");
        lblProductsCount.setText("—");
        lblOutOfStockCount.setText("—");
        lblOrdersCount.setText("—");

        statusTableModel.setRowCount(0);
        statusTableModel.addRow(new Object[]{"Database Offline", 0, "—"});

        recentOrdersModel.setRowCount(0);
        recentOrdersModel.addRow(new Object[]{"—", "Database connection offline", "—", "—", "—"});

        lowStockModel.setRowCount(0);
        lowStockModel.addRow(new Object[]{"—", "Database connection offline", "—", "—", "—"});
    }

    public void showConnectionError(String message) {
        if (lblErrorText != null && errorBanner != null) {
            String sanitized = message != null ? message : "Unable to reach MySQL on localhost:3306.";
            lblErrorText.setText("<html><b>Database Offline:</b> " + sanitized + 
                    " <span style='font-size:10px;color:#7F1D1D;'>(Verify MySQL80 service is running)</span></html>");
            errorBanner.setVisible(true);
            revalidate();
            repaint();
        }
    }

    public void hideConnectionError() {
        if (errorBanner != null && errorBanner.isVisible()) {
            errorBanner.setVisible(false);
            revalidate();
            repaint();
        }
    }

    /**
     * Applies color-coded status badge rendering to table columns.
     */
    private void applyStatusBadgeRenderer(JTable table, int colIndex) {
        table.getColumnModel().getColumn(colIndex).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                String status = String.valueOf(value);
                if (Order.STATUS_DELIVERED.equalsIgnoreCase(status)) {
                    setForeground(UIUtils.SUCCESS_COLOR);
                    setFont(UIUtils.BOLD_FONT);
                } else if (Order.STATUS_CANCELLED.equalsIgnoreCase(status)) {
                    setForeground(UIUtils.DANGER_COLOR);
                    setFont(UIUtils.BOLD_FONT);
                } else if (Order.STATUS_SHIPPED.equalsIgnoreCase(status)) {
                    setForeground(UIUtils.ACCENT_COLOR);
                    setFont(UIUtils.BOLD_FONT);
                } else if (Order.STATUS_PENDING.equalsIgnoreCase(status)) {
                    setForeground(UIUtils.WARNING_COLOR);
                    setFont(UIUtils.BOLD_FONT);
                } else {
                    setForeground(new Color(100, 110, 120));
                    setFont(UIUtils.REGULAR_FONT);
                }
                return c;
            }
        });
    }

    /**
     * Highlights out of stock items in bold crimson and low stock items in orange.
     */
    private void applyStockAlertRenderer(JTable table, int colIndex) {
        table.getColumnModel().getColumn(colIndex).setCellRenderer(new DefaultTableCellRenderer() {
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
                } else if (val.contains("Healthy")) {
                    setForeground(UIUtils.SUCCESS_COLOR);
                    setFont(UIUtils.BOLD_FONT);
                } else {
                    setForeground(new Color(100, 110, 120));
                    setFont(UIUtils.REGULAR_FONT);
                }
                return c;
            }
        });
    }
}
