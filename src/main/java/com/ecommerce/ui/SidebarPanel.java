package com.ecommerce.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Modern, dark navy left sidebar navigation panel for the E-Commerce Store application.
 * Features:
 * - Application branding with modern vector emblem and subtitle
 * - Five primary navigation items with hover and active states
 * - Left-edge vibrant blue indicator bar for the active selection
 * - Real-time database connection status card with recheck action
 * - Resolution-independent vector icons (no emojis or external asset files)
 */
public class SidebarPanel extends JPanel {

    // View Identifiers
    public static final String NAV_DASHBOARD = "DASHBOARD";
    public static final String NAV_CUSTOMERS = "CUSTOMERS";
    public static final String NAV_PRODUCTS = "PRODUCTS";
    public static final String NAV_CREATE_ORDER = "CREATE_ORDER";
    public static final String NAV_ORDER_HISTORY = "ORDER_HISTORY";

    /**
     * Listener interface notified when a navigation item is selected.
     */
    @FunctionalInterface
    public interface NavSelectionListener {
        void onNavSelected(String navId);
    }

    private final Map<String, NavButton> navButtons = new LinkedHashMap<>();
    private final List<NavSelectionListener> navListeners = new ArrayList<>();
    private String selectedNav = NAV_DASHBOARD;

    private boolean isDbOnline = false;
    private JLabel lblStatusDot;
    private JLabel lblStatusText;
    private JButton btnRecheckDb;
    private Runnable onReconnectAction;

    public SidebarPanel() {
        setPreferredSize(new Dimension(240, 0));
        setMinimumSize(new Dimension(240, 0));
        setLayout(new BorderLayout());
        setBackground(UIUtils.SIDEBAR_BG);

        initBranding();
        initNavigation();
        initDbStatusFooter();
    }

    /**
     * Header branding section with custom vector logo and store title.
     */
    private void initBranding() {
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(20, 16, 16, 16));

        // Branding Row (Emblem + Titles)
        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandRow.setOpaque(false);

        // Logo Badge
        JPanel logoBadge = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIUtils.NAV_ACTIVE_ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoBadge.setOpaque(false);
        logoBadge.setPreferredSize(new Dimension(38, 38));
        JLabel logoIcon = new JLabel(UIUtils.createNavIcon("STORE_LOGO", 20, Color.WHITE));
        logoBadge.add(logoIcon);
        brandRow.add(logoBadge);

        // Text titles
        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        JLabel lblTitle = new JLabel("E-STORE ADMIN");
        lblTitle.setFont(UIUtils.BRAND_TITLE_FONT);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Store Management");
        lblSub.setFont(UIUtils.BRAND_SUB_FONT);
        lblSub.setForeground(UIUtils.SIDEBAR_TEXT);

        textCol.add(lblTitle);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(lblSub);
        brandRow.add(textCol);

        brandPanel.add(brandRow);
        brandPanel.add(Box.createVerticalStrut(16));
        brandPanel.add(UIUtils.createDarkSeparator());

        add(brandPanel, BorderLayout.NORTH);
    }

    /**
     * Center navigation section containing the 5 required navigation items.
     */
    private void initNavigation() {
        JPanel navCenter = new JPanel();
        navCenter.setLayout(new BoxLayout(navCenter, BoxLayout.Y_AXIS));
        navCenter.setOpaque(false);
        navCenter.setBorder(new EmptyBorder(12, 10, 12, 10));

        // Menu Section Label
        JLabel lblMenu = new JLabel("MAIN NAVIGATION");
        lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblMenu.setForeground(new Color(107, 114, 128));
        lblMenu.setBorder(new EmptyBorder(4, 12, 8, 12));
        navCenter.add(lblMenu);

        // 5 Primary Navigation Items
        addNavButton(navCenter, NAV_DASHBOARD, "Dashboard", "DASHBOARD");
        addNavButton(navCenter, NAV_CUSTOMERS, "Customers", "CUSTOMERS");
        addNavButton(navCenter, NAV_PRODUCTS, "Products & Inventory", "PRODUCTS");
        addNavButton(navCenter, NAV_CREATE_ORDER, "Place Order", "CREATE_ORDER");
        addNavButton(navCenter, NAV_ORDER_HISTORY, "Orders & History", "ORDER_HISTORY");

        // Mark default active
        NavButton defaultBtn = navButtons.get(selectedNav);
        if (defaultBtn != null) {
            defaultBtn.setSelectedState(true);
        }

        // Fill remaining vertical space
        navCenter.add(Box.createVerticalGlue());

        add(navCenter, BorderLayout.CENTER);
    }

    private void addNavButton(JPanel container, String navId, String label, String iconType) {
        NavButton btn = new NavButton(navId, label, iconType);
        btn.addActionListener(e -> selectNav(navId, true));
        navButtons.put(navId, btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(4));
    }

    /**
     * Footer section displaying database connection status and recheck action.
     */
    private void initDbStatusFooter() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(12, 12, 16, 12));

        footer.add(UIUtils.createDarkSeparator());
        footer.add(Box.createVerticalStrut(12));

        // Dark Status Card
        JPanel statusCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIUtils.SIDEBAR_CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(UIUtils.SIDEBAR_BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        statusCard.setLayout(new BoxLayout(statusCard, BoxLayout.Y_AXIS));
        statusCard.setOpaque(false);
        statusCard.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblHeader = new JLabel("DATABASE STATUS");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblHeader.setForeground(new Color(140, 150, 165));
        statusCard.add(lblHeader);

        // Status indicator row (Dot + text)
        JPanel dotRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        dotRow.setOpaque(false);

        lblStatusDot = new JLabel(UIUtils.createNavIcon("STATUS_DOT", 10,
                () -> isDbOnline ? UIUtils.STATUS_ONLINE : UIUtils.STATUS_OFFLINE));

        lblStatusText = new JLabel("Checking DB...");
        lblStatusText.setFont(UIUtils.STATUS_FONT);
        lblStatusText.setForeground(Color.WHITE);

        dotRow.add(lblStatusDot);
        dotRow.add(lblStatusText);
        statusCard.add(dotRow);
        statusCard.add(Box.createVerticalStrut(6));

        // Recheck button
        btnRecheckDb = UIUtils.createSidebarSecondaryButton("↻ Recheck Connection");
        btnRecheckDb.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRecheckDb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        btnRecheckDb.addActionListener(e -> {
            if (onReconnectAction != null) {
                onReconnectAction.run();
            }
        });
        statusCard.add(btnRecheckDb);

        footer.add(statusCard);
        add(footer, BorderLayout.SOUTH);
    }

    /**
     * Updates selected state and optionally broadcasts to registered listeners.
     */
    public void selectNav(String navId, boolean notifyListeners) {
        if (!navButtons.containsKey(navId)) {
            return;
        }

        this.selectedNav = navId;
        for (Map.Entry<String, NavButton> entry : navButtons.entrySet()) {
            entry.getValue().setSelectedState(entry.getKey().equals(navId));
        }

        if (notifyListeners) {
            for (NavSelectionListener listener : navListeners) {
                listener.onNavSelected(navId);
            }
        }
    }

    /**
     * Programmatically sets the selected navigation item without firing events back to listeners.
     */
    public void setSelectedNav(String navId) {
        selectNav(navId, false);
    }

    public String getSelectedNav() {
        return selectedNav;
    }

    public void addNavSelectionListener(NavSelectionListener listener) {
        if (listener != null) {
            navListeners.add(listener);
        }
    }

    public void removeNavSelectionListener(NavSelectionListener listener) {
        navListeners.remove(listener);
    }

    public void setOnReconnectRequested(Runnable action) {
        this.onReconnectAction = action;
    }

    /**
     * Updates the database status indicator and textual description.
     */
    public void setDatabaseStatus(boolean isOnline, String description) {
        setDatabaseStatus(isOnline, description, description);
    }

    public void setDatabaseStatus(boolean isOnline, String description, String tooltip) {
        this.isDbOnline = isOnline;
        SwingUtilities.invokeLater(() -> {
            if (isOnline) {
                lblStatusText.setText(description != null ? description : "Connected");
                lblStatusText.setForeground(new Color(220, 240, 225));
                lblStatusText.setToolTipText(tooltip != null ? tooltip : "MySQL connection active (ecom_db)");
            } else {
                lblStatusText.setText(description != null ? description : "Offline");
                lblStatusText.setForeground(new Color(255, 200, 200));
                lblStatusText.setToolTipText(tooltip != null ? tooltip : description);
            }
            lblStatusDot.setToolTipText(lblStatusText.getToolTipText());
            lblStatusDot.repaint();
        });
    }

    /**
     * Custom navigation button with hover effects, active blue indicator bar,
     * and dynamic vector icon rendering.
     */
    private class NavButton extends JButton {
        private final String navId;
        private final String iconType;
        private boolean isSelected = false;
        private boolean isHovered = false;

        public NavButton(String navId, String text, String iconType) {
            super(text);
            this.navId = navId;
            this.iconType = iconType;

            setFont(UIUtils.NAV_ITEM_FONT);
            setHorizontalAlignment(SwingConstants.LEFT);
            setIconTextGap(12);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(220, 42));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            setBorder(new EmptyBorder(10, 16, 10, 14));

            // Dynamic vector icon matching the button's foreground color
            setIcon(UIUtils.createNavIcon(iconType, 16, this::getForeground));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    updateStyle();
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    updateStyle();
                    repaint();
                }
            });

            updateStyle();
        }

        public void setSelectedState(boolean selected) {
            this.isSelected = selected;
            updateStyle();
            repaint();
        }

        private void updateStyle() {
            if (isSelected) {
                setForeground(UIUtils.SIDEBAR_TEXT_ACTIVE);
            } else if (isHovered) {
                setForeground(UIUtils.SIDEBAR_TEXT_HOVER);
            } else {
                setForeground(UIUtils.SIDEBAR_TEXT);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (isSelected) {
                // Active selection background
                g2.setColor(UIUtils.SIDEBAR_ACTIVE);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 8, 8));

                // Blue active indicator bar on the left edge
                g2.setColor(UIUtils.NAV_ACTIVE_ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 4, 4, h - 8, 2, 2));

            } else if (isHovered) {
                // Subtle hover highlight
                g2.setColor(UIUtils.SIDEBAR_HOVER);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 8, 8));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}

