package com.chatsystem.admin.components;

import com.chatsystem.admin.theme.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Dark left sidebar navigation panel for the ChatSystem Admin dashboard,
 * restyled to match Member 1's User application ({@code com.chatsystem.user.UserApp}
 * and {@code NavigationIcon}).
 * Provides navigation to all 9 required Administrator screens.
 */
public class SidebarPanel extends JPanel {

    // Group 1: Core Administration (Items 1-4)
    public static final String NAV_USERS = "Quản lý người dùng";
    public static final String NAV_LOGIN_HISTORY = "Lịch sử đăng nhập";
    public static final String NAV_GROUPS = "Quản lý nhóm chat";
    public static final String NAV_SPAM = "Báo cáo spam";

    // Group 2: Statistics & Charts (Items 5-9)
    public static final String NAV_NEW_REGISTRATIONS = "Người dùng đăng ký mới";
    public static final String NAV_REGISTRATION_CHART = "Biểu đồ đăng ký mới";
    public static final String NAV_FRIEND_STATS = "Thống kê bạn bè";
    public static final String NAV_USER_ACTIVITY = "Người dùng hoạt động";
    public static final String NAV_ACTIVITY_CHART = "Biểu đồ hoạt động";

    private static final int NAV_BUTTON_HEIGHT = 42;

    private final Map<String, JButton> navigationButtons = new LinkedHashMap<>();
    private String selectedItem = NAV_USERS;
    private Consumer<String> navigationListener;

    public SidebarPanel() {
        super(new BorderLayout());
        setBackground(AppTheme.SIDEBAR_BG);
        setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 0));
        setMinimumSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 0));
        setBorder(BorderFactory.createEmptyBorder(22, 12, 16, 12));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("ChatSystem Admin");
        brand.setForeground(AppTheme.SIDEBAR_TEXT);
        brand.setFont(AppTheme.FONT_BRAND_TITLE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 4, 18, 0));
        top.add(brand);

        JSeparator separator = new JSeparator();
        separator.setForeground(AppTheme.SIDEBAR_BORDER);
        separator.setBackground(AppTheme.SIDEBAR_BORDER);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(separator);
        top.add(Box.createVerticalStrut(14));

        addNavigation(top, NAV_USERS, "users");
        addNavigation(top, NAV_LOGIN_HISTORY, "history");
        addNavigation(top, NAV_GROUPS, "groups");
        addNavigation(top, NAV_SPAM, "spam");
        addNavigation(top, NAV_NEW_REGISTRATIONS, "new_user");
        addNavigation(top, NAV_REGISTRATION_CHART, "reg_chart");
        addNavigation(top, NAV_FRIEND_STATS, "friend_stats");
        addNavigation(top, NAV_USER_ACTIVITY, "activity");
        addNavigation(top, NAV_ACTIVITY_CHART, "act_chart");

        add(top, BorderLayout.NORTH);
        add(createFooter(), BorderLayout.SOUTH);

        updateNavigationSelection();
    }

    public void setNavigationListener(Consumer<String> listener) {
        this.navigationListener = listener;
    }

    public String getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(String itemLabel) {
        this.selectedItem = itemLabel;
        updateNavigationSelection();
    }

    private void addNavigation(JPanel parent, String label, String iconKind) {
        JButton button = new JButton(label, new AdminNavigationIcon(iconKind));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(11);
        button.setForeground(AppTheme.SIDEBAR_NAV_TEXT);
        button.setBackground(AppTheme.SIDEBAR_BG);
        button.setFont(AppTheme.FONT_BODY);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 8));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, NAV_BUTTON_HEIGHT));
        button.setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH - 24, NAV_BUTTON_HEIGHT));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!label.equals(selectedItem)) {
                    button.setBackground(AppTheme.SIDEBAR_SURFACE);
                    button.setForeground(AppTheme.SIDEBAR_TEXT);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!label.equals(selectedItem)) {
                    button.setBackground(AppTheme.SIDEBAR_BG);
                    button.setForeground(AppTheme.SIDEBAR_NAV_TEXT);
                }
            }
        });

        button.addActionListener(e -> {
            setSelectedItem(label);
            if (navigationListener != null) {
                navigationListener.accept(label);
            }
        });

        parent.add(button);
        parent.add(Box.createVerticalStrut(6));
        navigationButtons.put(label, button);
    }

    private void updateNavigationSelection() {
        navigationButtons.forEach((label, button) -> {
            boolean active = label.equals(selectedItem);
            button.setBackground(active ? AppTheme.PRIMARY : AppTheme.SIDEBAR_BG);
            button.setForeground(active ? AppTheme.SIDEBAR_TEXT : AppTheme.SIDEBAR_NAV_TEXT);
            button.setFont(active ? AppTheme.FONT_BODY_BOLD : AppTheme.FONT_BODY);
        });
        repaint();
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new GridLayout(0, 1, 0, 6));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

        JButton logout = new JButton("Đăng xuất");
        logout.setFont(AppTheme.FONT_BODY);
        logout.setForeground(AppTheme.SIDEBAR_FOOTER_TEXT);
        logout.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 0));
        logout.setBorderPainted(false);
        logout.setContentAreaFilled(false);
        logout.setFocusPainted(false);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "Bản thiết kế giao diện quản trị — dữ liệu mẫu, chưa kết nối đăng nhập/đăng xuất máy chủ.",
                "Phiên bản 1",
                JOptionPane.INFORMATION_MESSAGE
        ));

        JLabel who = new JLabel("Quản trị viên · Phiên bản 1");
        who.setForeground(AppTheme.SIDEBAR_FOOTER_TEXT);
        who.setFont(AppTheme.FONT_CAPTION);

        JLabel note = new JLabel("Dữ liệu mẫu · Swing UI");
        note.setForeground(AppTheme.SIDEBAR_TEXT_MUTED);
        note.setFont(AppTheme.FONT_SIDEBAR_NOTE);

        footer.add(logout);
        footer.add(who);
        footer.add(note);
        return footer;
    }

    /**
     * Local Java2D outline icons matching {@code com.chatsystem.user.NavigationIcon}
     * (1.7f rounded stroke using the button's active/inactive foreground color).
     */
    private static final class AdminNavigationIcon implements Icon {

        private final String kind;

        private AdminNavigationIcon(String kind) {
            this.kind = kind;
        }

        @Override
        public int getIconWidth() {
            return 22;
        }

        @Override
        public int getIconHeight() {
            return 22;
        }

        @Override
        public void paintIcon(Component c, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c.getForeground());
            g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (kind) {
                case "users", "friend_stats" -> {
                    g.drawOval(3, 2, 7, 7);
                    g.drawOval(13, 4, 5, 5);
                    g.drawArc(1, 10, 13, 12, 0, 180);
                    g.drawArc(11, 11, 10, 10, 0, 145);
                }
                case "new_user" -> {
                    g.drawOval(3, 2, 7, 7);
                    g.drawArc(1, 10, 13, 12, 0, 180);
                    g.drawLine(15, 13, 21, 13);
                    g.drawLine(18, 10, 18, 16);
                }
                case "groups" -> {
                    g.drawRoundRect(2, 3, 18, 13, 5, 5);
                    g.drawLine(6, 16, 6, 20);
                    g.drawLine(6, 20, 10, 16);
                }
                case "history" -> {
                    g.drawOval(2, 2, 18, 18);
                    g.drawLine(11, 6, 11, 11);
                    g.drawLine(11, 11, 15, 14);
                }
                case "spam" -> {
                    g.drawRoundRect(3, 3, 16, 16, 5, 5);
                    g.drawLine(11, 7, 11, 12);
                    g.fillOval(10, 15, 2, 2);
                }
                case "reg_chart", "act_chart" -> {
                    g.drawLine(3, 19, 19, 19);
                    g.drawRect(5, 11, 3, 8);
                    g.drawRect(10, 6, 3, 13);
                    g.drawRect(15, 9, 3, 10);
                }
                case "activity" -> {
                    g.drawLine(2, 12, 6, 12);
                    g.drawLine(6, 12, 9, 5);
                    g.drawLine(9, 5, 13, 17);
                    g.drawLine(13, 17, 16, 12);
                    g.drawLine(16, 12, 20, 12);
                }
                default -> {
                    g.drawOval(4, 4, 14, 14);
                }
            }
            g.dispose();
        }
    }
}
