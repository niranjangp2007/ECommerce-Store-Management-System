package com.ecommerce.ui;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.service.DatabaseOperationException;
import com.ecommerce.service.OrderService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise Order History & Lifecycle Management screen.
 * Features:
 * - Real-time order KPI statistics cards (Total, Delivered, Shipped, Pending, Cancelled)
 * - Search by Order ID and Customer name with live status filtering
 * - Color-coded status badges across all tables
 * - Dedicated modal dialog for detailed line item breakdowns
 * - Order status transitions and transactional cancellation with inventory restoration
 */
public class OrderHistoryPanel extends JPanel {

    private final OrderService orderService;
    private final MainFrame mainFrame;

    // Cached Orders for local fast search/filter
    private final List<Order> allOrdersCache = new ArrayList<>();

    // Header & Metric Labels
    private JLabel lblOrderCountBadge;
    private JLabel lblTotalOrdersMetric;
    private JLabel lblDeliveredMetric;
    private JLabel lblShippedMetric;
    private JLabel lblPendingMetric;
    private JLabel lblCancelledMetric;

    // Toolbar & Filters
    private JTextField txtSearch;
    private JComboBox<String> cmbStatusFilter;
    private JLabel lblResultsInfo;

    // Action Controls
    private JComboBox<String> cmbNewStatus;
    private JButton btnViewDetails;
    private JButton btnUpdateStatus;
    private JButton btnCancelOrder;

    // Orders Table & Model
    private JTable ordersTable;
    private DefaultTableModel ordersTableModel;

    public OrderHistoryPanel(MainFrame mainFrame, OrderService orderService) {
        this.mainFrame = mainFrame;
        this.orderService = orderService;

        setLayout(new BorderLayout(14, 14));
        setBackground(UIUtils.BG_LIGHT);
        setBorder(new EmptyBorder(18, 20, 20, 20));

        initComponents();
        loadOrders();
    }

    private void initComponents() {
        // Container for Header + Stats + Toolbar
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // 1. Page Header
        topContainer.add(createHeaderPanel());
        topContainer.add(Box.createVerticalStrut(14));

        // 2. Order Statistics Cards (5 KPIs)
        topContainer.add(createStatsGrid());
        topContainer.add(Box.createVerticalStrut(14));

        // 3. Search, Filter & Action Toolbar
        topContainer.add(createToolbarPanel());
        add(topContainer, BorderLayout.NORTH);

        // 4. Orders Data Table Card
        String[] orderCols = {"Order ID", "Customer Name", "Order Date", "Status", "Items Count", "Total Amount (₹)"};
        ordersTableModel = new DefaultTableModel(orderCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ordersTable = new JTable(ordersTableModel);
        UIUtils.formatTable(ordersTable);

        // Alignments
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        ordersTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        ordersTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        ordersTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Currency format right aligned
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        ordersTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

        // Status column color-coded badge renderer
        ordersTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
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
                } else {
                    setForeground(UIUtils.WARNING_COLOR);
                    setFont(UIUtils.BOLD_FONT);
                }
                return c;
            }
        });

        // Enable action buttons based on selection
        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = ordersTable.getSelectedRow() >= 0;
            btnViewDetails.setEnabled(hasSelection);
            btnUpdateStatus.setEnabled(hasSelection);
            btnCancelOrder.setEnabled(hasSelection);
        });

        // Double click row to view order line items breakdown
        ordersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && ordersTable.getSelectedRow() >= 0) {
                    showOrderDetailsDialog();
                }
            }
        });

        JScrollPane scrollOrders = new JScrollPane(ordersTable);
        scrollOrders.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));

        JPanel tableCard = UIUtils.createCard("Customer Orders Ledger (orders table)", scrollOrders);
        add(tableCard, BorderLayout.CENTER);
    }

    /**
     * Professional page header with title, subtitle, badge, and refresh trigger.
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleBox.setOpaque(false);

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        JLabel title = new JLabel("Orders & History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(17, 24, 39));

        JLabel sub = new JLabel("Track, inspect and manage customer order lifecycles and transactions.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(107, 114, 128));

        textCol.add(title);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(sub);
        titleBox.add(textCol);

        lblOrderCountBadge = UIUtils.createPillBadge("0 Orders Total", new Color(239, 246, 255), new Color(37, 99, 235));
        titleBox.add(lblOrderCountBadge);

        header.add(titleBox, BorderLayout.WEST);

        JButton btnRefresh = UIUtils.createSecondaryButton("↻ Refresh");
        btnRefresh.setPreferredSize(new Dimension(105, 32));
        btnRefresh.addActionListener(e -> loadOrders());
        header.add(btnRefresh, BorderLayout.EAST);

        return header;
    }

    /**
     * Five compact KPI statistic cards for order lifecycle metrics.
     */
    private JPanel createStatsGrid() {
        JPanel grid = new JPanel(new GridLayout(1, 5, 12, 12));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

        lblTotalOrdersMetric = new JLabel("—");
        lblDeliveredMetric = new JLabel("—");
        lblShippedMetric = new JLabel("—");
        lblPendingMetric = new JLabel("—");
        lblCancelledMetric = new JLabel("—");

        grid.add(createMiniCard("TOTAL ORDERS", lblTotalOrdersMetric, "All Records", new Color(37, 99, 235)));
        grid.add(createMiniCard("DELIVERED", lblDeliveredMetric, "Completed", new Color(5, 150, 105)));
        grid.add(createMiniCard("SHIPPED", lblShippedMetric, "In Transit", new Color(2, 132, 199)));
        grid.add(createMiniCard("PENDING", lblPendingMetric, "Awaiting Dispatch", new Color(217, 119, 6)));
        grid.add(createMiniCard("CANCELLED", lblCancelledMetric, "Stock Restored", new Color(220, 38, 38)));

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
     * Compact Toolbar with search, status filtering, and action buttons.
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

        // Left Filters & Search
        JPanel filterBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBox.setOpaque(false);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UIUtils.BOLD_FONT);
        lblSearch.setForeground(new Color(55, 65, 81));

        txtSearch = new JTextField(15);
        txtSearch.setPreferredSize(new Dimension(170, 30));
        txtSearch.putClientProperty("JTextField.placeholderText", "Order ID or Customer...");
        txtSearch.addActionListener(e -> applyFilter());

        JLabel lblFilter = new JLabel("Status:");
        lblFilter.setFont(UIUtils.BOLD_FONT);
        lblFilter.setForeground(new Color(55, 65, 81));

        cmbStatusFilter = new JComboBox<>(new String[]{
                "All", Order.STATUS_DELIVERED, Order.STATUS_SHIPPED, Order.STATUS_PENDING, Order.STATUS_CANCELLED
        });
        cmbStatusFilter.setPreferredSize(new Dimension(130, 30));
        cmbStatusFilter.addActionListener(e -> applyFilter());

        JButton btnFilter = UIUtils.createSecondaryButton("Filter");
        btnFilter.setPreferredSize(new Dimension(68, 30));
        btnFilter.addActionListener(e -> applyFilter());

        JButton btnReset = UIUtils.createSecondaryButton("Reset");
        btnReset.setPreferredSize(new Dimension(65, 30));
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbStatusFilter.setSelectedIndex(0);
            applyFilter();
        });

        lblResultsInfo = new JLabel("");
        lblResultsInfo.setFont(UIUtils.REGULAR_FONT);
        lblResultsInfo.setForeground(new Color(107, 114, 128));

        filterBox.add(lblSearch);
        filterBox.add(txtSearch);
        filterBox.add(lblFilter);
        filterBox.add(cmbStatusFilter);
        filterBox.add(btnFilter);
        filterBox.add(btnReset);
        filterBox.add(lblResultsInfo);
        bar.add(filterBox, BorderLayout.WEST);

        // Right Actions (View Details, Status Update, Cancellation)
        JPanel actionBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBox.setOpaque(false);

        btnViewDetails = UIUtils.createSecondaryButton("View Details");
        btnViewDetails.setPreferredSize(new Dimension(110, 30));
        btnViewDetails.setEnabled(false);
        btnViewDetails.addActionListener(e -> showOrderDetailsDialog());

        JLabel lblChange = new JLabel("Set Status:");
        lblChange.setFont(UIUtils.BOLD_FONT);
        lblChange.setForeground(new Color(55, 65, 81));

        cmbNewStatus = new JComboBox<>(new String[]{
                Order.STATUS_PENDING, Order.STATUS_SHIPPED, Order.STATUS_DELIVERED
        });
        cmbNewStatus.setPreferredSize(new Dimension(115, 30));

        btnUpdateStatus = UIUtils.createPrimaryButton("Update");
        btnUpdateStatus.setPreferredSize(new Dimension(85, 30));
        btnUpdateStatus.setEnabled(false);
        btnUpdateStatus.addActionListener(e -> updateSelectedOrderStatus());

        btnCancelOrder = UIUtils.createDangerButton("✕ Cancel Order");
        btnCancelOrder.setPreferredSize(new Dimension(130, 30));
        btnCancelOrder.setEnabled(false);
        btnCancelOrder.setToolTipText("Cancels order and automatically restores product stock to inventory");
        btnCancelOrder.addActionListener(e -> cancelSelectedOrder());

        actionBox.add(btnViewDetails);
        actionBox.add(lblChange);
        actionBox.add(cmbNewStatus);
        actionBox.add(btnUpdateStatus);
        actionBox.add(btnCancelOrder);
        bar.add(actionBox, BorderLayout.EAST);

        return bar;
    }

    public void loadOrders() {
        try {
            allOrdersCache.clear();
            allOrdersCache.addAll(orderService.getAllOrders());
            updateKpiMetrics(allOrdersCache);
            applyFilter();
        } catch (SQLException e) {
            UIUtils.showError(this, "Failed to load orders: " + e.getMessage());
        }
    }

    private void applyFilter() {
        ordersTableModel.setRowCount(0);
        String search = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        String statusFilter = cmbStatusFilter != null ? (String) cmbStatusFilter.getSelectedItem() : "All";

        int displayedCount = 0;
        for (Order o : allOrdersCache) {
            boolean matchesSearch = search.isEmpty() ||
                    String.valueOf(o.getOrderId()).contains(search) ||
                    (o.getCustomerName() != null && o.getCustomerName().toLowerCase().contains(search));

            boolean matchesStatus = "All".equalsIgnoreCase(statusFilter) ||
                    o.getStatus().equalsIgnoreCase(statusFilter);

            if (matchesSearch && matchesStatus) {
                ordersTableModel.addRow(new Object[]{
                        "#" + o.getOrderId(),
                        o.getCustomerName() != null ? o.getCustomerName() : "Customer #" + o.getCustomerId(),
                        o.getOrderDate(),
                        o.getStatus(),
                        o.getItems().size(),
                        String.format("₹%.2f", o.calculateTotal())
                });
                displayedCount++;
            }
        }

        int total = allOrdersCache.size();
        lblOrderCountBadge.setText(total + " Orders Total");
        if (displayedCount < total) {
            lblResultsInfo.setText("Showing " + displayedCount + " of " + total);
        } else {
            lblResultsInfo.setText("Showing all " + total);
        }
    }

    private void updateKpiMetrics(List<Order> orders) {
        int total = orders.size();
        int delivered = 0;
        int shipped = 0;
        int pending = 0;
        int cancelled = 0;

        for (Order o : orders) {
            String s = o.getStatus();
            if (Order.STATUS_DELIVERED.equalsIgnoreCase(s)) delivered++;
            else if (Order.STATUS_SHIPPED.equalsIgnoreCase(s)) shipped++;
            else if (Order.STATUS_PENDING.equalsIgnoreCase(s)) pending++;
            else if (Order.STATUS_CANCELLED.equalsIgnoreCase(s)) cancelled++;
        }

        lblTotalOrdersMetric.setText(String.valueOf(total));
        lblDeliveredMetric.setText(String.valueOf(delivered));
        lblShippedMetric.setText(String.valueOf(shipped));
        lblPendingMetric.setText(String.valueOf(pending));
        lblCancelledMetric.setText(String.valueOf(cancelled));
    }

    /**
     * Professional modal dialog displaying the full line item breakdown for a selected order.
     */
    private void showOrderDetailsDialog() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select an order to view details.");
            return;
        }

        String idStr = String.valueOf(ordersTableModel.getValueAt(row, 0)).replace("#", "").trim();
        int orderId = Integer.parseInt(idStr);

        try {
            Order order = orderService.getOrderDetails(orderId);
            if (order == null) {
                UIUtils.showError(this, "Order details not found for Order #" + orderId);
                return;
            }

            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Order Details #" + orderId, Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setLayout(new BorderLayout());
            dialog.setResizable(false);

            // Dialog Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(new Color(24, 34, 54));
            header.setBorder(new EmptyBorder(16, 20, 16, 20));

            JPanel titleCol = new JPanel();
            titleCol.setLayout(new BoxLayout(titleCol, BoxLayout.Y_AXIS));
            titleCol.setOpaque(false);

            JLabel lblTitle = new JLabel("Order #" + orderId + " Details");
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
            lblTitle.setForeground(Color.WHITE);

            JLabel lblCustomer = new JLabel("Customer: " + (order.getCustomerName() != null ? order.getCustomerName() : "Customer #" + order.getCustomerId()) +
                    "  •  Date: " + order.getOrderDate() + "  •  Status: " + order.getStatus());
            lblCustomer.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblCustomer.setForeground(new Color(180, 195, 215));

            titleCol.add(lblTitle);
            titleCol.add(Box.createVerticalStrut(2));
            titleCol.add(lblCustomer);
            header.add(titleCol, BorderLayout.WEST);
            dialog.add(header, BorderLayout.NORTH);

            // Table of Line Items
            String[] itemCols = {"Line Item ID", "Product Name", "Unit Price (₹)", "Quantity", "Line Total (₹)"};
            DefaultTableModel itemsModel = new DefaultTableModel(itemCols, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };

            for (OrderItem item : order.getItems()) {
                itemsModel.addRow(new Object[]{
                        "#" + item.getItemId(),
                        item.getProductName(),
                        String.format("₹%.2f", item.getUnitPrice()),
                        item.getQuantity(),
                        String.format("₹%.2f", item.getLineTotal())
                });
            }

            JTable itemsTable = new JTable(itemsModel);
            UIUtils.formatTable(itemsTable);

            // Alignments
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            itemsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
            itemsTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

            DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
            rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
            itemsTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
            itemsTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

            JScrollPane scrollItems = new JScrollPane(itemsTable);
            scrollItems.setPreferredSize(new Dimension(620, 220));
            scrollItems.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));

            JPanel contentCard = new JPanel(new BorderLayout(8, 8));
            contentCard.setBackground(Color.WHITE);
            contentCard.setBorder(new EmptyBorder(14, 18, 14, 18));
            contentCard.add(scrollItems, BorderLayout.CENTER);
            dialog.add(contentCard, BorderLayout.CENTER);

            // Dialog Footer with Summary and Close
            JPanel footer = new JPanel(new BorderLayout());
            footer.setBackground(new Color(245, 247, 250));
            footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));

            JPanel summaryBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
            summaryBox.setOpaque(false);
            JLabel lblItemCount = new JLabel("Total Items: " + order.getItems().size());
            lblItemCount.setFont(UIUtils.BOLD_FONT);
            JLabel lblGrandTotal = new JLabel("Order Grand Total: ₹" + order.calculateTotal());
            lblGrandTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblGrandTotal.setForeground(UIUtils.NAV_ACTIVE_ACCENT);

            summaryBox.add(lblItemCount);
            summaryBox.add(lblGrandTotal);
            footer.add(summaryBox, BorderLayout.WEST);

            JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
            btnBox.setOpaque(false);
            JButton btnClose = UIUtils.createSecondaryButton("Close");
            btnClose.setPreferredSize(new Dimension(85, 32));
            btnClose.addActionListener(e -> dialog.dispose());
            btnBox.add(btnClose);
            footer.add(btnBox, BorderLayout.EAST);

            dialog.add(footer, BorderLayout.SOUTH);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

        } catch (SQLException e) {
            UIUtils.showError(this, "Failed to load order items: " + e.getMessage());
        }
    }

    /**
     * Updates selected order status and synchronizes cross-panel data.
     */
    private void updateSelectedOrderStatus() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select an order from the table to update.");
            return;
        }

        String idStr = String.valueOf(ordersTableModel.getValueAt(row, 0)).replace("#", "").trim();
        int orderId = Integer.parseInt(idStr);
        String currentStatus = (String) ordersTableModel.getValueAt(row, 3);
        String newStatus = (String) cmbNewStatus.getSelectedItem();

        if (currentStatus.equalsIgnoreCase(newStatus)) {
            UIUtils.showInfo(this, "Order #" + orderId + " is already in '" + newStatus + "' status.");
            return;
        }

        if (Order.STATUS_CANCELLED.equalsIgnoreCase(currentStatus)) {
            UIUtils.showError(this, "Order #" + orderId + " is Cancelled. Cancelled orders cannot be modified.");
            return;
        }

        try {
            orderService.updateOrderStatus(orderId, newStatus);
            UIUtils.showInfo(this, "Order #" + orderId + " status updated to '" + newStatus + "'.");
            loadOrders();
            mainFrame.refreshAllPanels();

        } catch (DatabaseOperationException | SQLException e) {
            UIUtils.showError(this, e.getMessage());
        }
    }

    /**
     * Cancels an order and verifies transactional inventory stock restoration.
     */
    private void cancelSelectedOrder() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select an order from the table to cancel.");
            return;
        }

        String idStr = String.valueOf(ordersTableModel.getValueAt(row, 0)).replace("#", "").trim();
        int orderId = Integer.parseInt(idStr);
        String currentStatus = (String) ordersTableModel.getValueAt(row, 3);

        if (Order.STATUS_CANCELLED.equalsIgnoreCase(currentStatus)) {
            UIUtils.showError(this, "Order #" + orderId + " is already Cancelled.");
            return;
        }

        boolean confirm = UIUtils.confirmAction(this,
                "Cancel Order #" + orderId + "?\n\n" +
                "Transactional stock restoration will automatically return reserved items back to product inventory.",
                "Confirm Order Cancellation");

        if (confirm) {
            try {
                orderService.cancelOrder(orderId);
                UIUtils.showInfo(this, "Order #" + orderId + " has been Cancelled.\nStock has been restored successfully!");
                loadOrders();
                mainFrame.refreshAllPanels();

            } catch (DatabaseOperationException | SQLException e) {
                UIUtils.showError(this, "Cancellation failed: " + e.getMessage());
            }
        }
    }
}
