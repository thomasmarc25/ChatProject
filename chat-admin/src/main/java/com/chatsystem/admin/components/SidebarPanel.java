package com.chatsystem.admin.components;

import com.chatsystem.admin.theme.AppTheme;
import com.chatsystem.user.MockLoginFrame;
import com.chatsystem.user.UserSessionProfile;
import com.chatsystem.user.UserWorkspaceHost;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Dark left sidebar navigation panel for the integrated ChatSystem Admin window,
 * organized into two sections:
 * <ul>
 *     <li><b>Cá nhân:</b> Member 1's 6 personal User pages (Tin nhắn, Bạn bè, Nhóm chat, Tìm kiếm, Yêu cầu kết bạn, Cài đặt).</li>
 *     <li><b>Quản trị:</b> All 9 Administrator screens.</li>
 * </ul>
 * Uses a dark borderless scroll pane for the middle list while pinning the brand header
 * and {@code Đăng xuất} footer so every item remains accessible at the minimum 1100x700 window size.
 */
public class SidebarPanel extends JPanel {

    // Section 1: Cá nhân (Personal User features embedded from chat-user)
    public static final String NAV_PERSONAL_MESSAGES = UserWorkspaceHost.PAGE_MESSAGES;
    public static final String NAV_PERSONAL_FRIENDS = UserWorkspaceHost.PAGE_FRIENDS;
    public static final String NAV_PERSONAL_GROUPS = UserWorkspaceHost.PAGE_GROUPS;
    public static final String NAV_PERSONAL_SEARCH = UserWorkspaceHost.PAGE_SEARCH;
    public static final String NAV_PERSONAL_REQUESTS = UserWorkspaceHost.PAGE_REQUESTS;
    public static final String NAV_PERSONAL_SETTINGS = UserWorkspaceHost.PAGE_SETTINGS;

    // Section 2: Quản trị (All 9 Administrator screens)
    public static final String NAV_USERS = "Quản lý người dùng";
    public static final String NAV_LOGIN_HISTORY = "Lịch sử đăng nhập";
    public static final String NAV_GROUPS = "Quản lý nhóm chat";
    public static final String NAV_SPAM = "Báo cáo spam";
    public static final String NAV_NEW_REGISTRATIONS = "Người dùng đăng ký mới";
    public static final String NAV_REGISTRATION_CHART = "Biểu đồ đăng ký mới";
    public static final String NAV_FRIEND_STATS = "Thống kê bạn bè";
    public static final String NAV_USER_ACTIVITY = "Người dùng hoạt động";
    public static final String NAV_ACTIVITY_CHART = "Biểu đồ hoạt động";

    private static final int NAV_BUTTON_HEIGHT = 38;

    private final Map<String, JButton> navigationButtons = new LinkedHashMap<>();
    private String selectedItem = NAV_USERS;
    private Consumer<String> navigationListener;

    public SidebarPanel() {
        super(new BorderLayout());
        setBackground(AppTheme.SIDEBAR_BG);
        setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 0));
        setMinimumSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 12, 14, 12));

        // Pinned North: Brand Header
        JPanel brandHeader = new JPanel();
        brandHeader.setOpaque(false);
        brandHeader.setLayout(new BoxLayout(brandHeader, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("ChatSystem Admin");
        brand.setForeground(AppTheme.SIDEBAR_TEXT);
        brand.setFont(AppTheme.FONT_BRAND_TITLE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 4, 14, 0));
        brandHeader.add(brand);

        JSeparator separator = new JSeparator();
        separator.setForeground(AppTheme.SIDEBAR_BORDER);
        separator.setBackground(AppTheme.SIDEBAR_BORDER);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandHeader.add(separator);
        brandHeader.add(Box.createVerticalStrut(10));

        // Scrollable Center: Cá nhân (6 items) + Quản trị (9 items)
        JPanel navList = new JPanel();
        navList.setBackground(AppTheme.SIDEBAR_BG);
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));

        addSectionHeader(navList, "CÁ NHÂN");
        addNavigation(navList, NAV_PERSONAL_MESSAGES, "chat");
        addNavigation(navList, NAV_PERSONAL_FRIENDS, "friends");
        addNavigation(navList, NAV_PERSONAL_GROUPS, "group");
        addNavigation(navList, NAV_PERSONAL_SEARCH, "search");
        addNavigation(navList, NAV_PERSONAL_REQUESTS, "requests");
        addNavigation(navList, NAV_PERSONAL_SETTINGS, "settings");

        navList.add(Box.createVerticalStrut(8));
        JSeparator midSep = new JSeparator();
        midSep.setForeground(AppTheme.SIDEBAR_BORDER);
        midSep.setBackground(AppTheme.SIDEBAR_BORDER);
        midSep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        midSep.setAlignmentX(Component.LEFT_ALIGNMENT);
        navList.add(midSep);
        navList.add(Box.createVerticalStrut(10));

        addSectionHeader(navList, "QUẢN TRỊ");
        addNavigation(navList, NAV_USERS, "users");
        addNavigation(navList, NAV_LOGIN_HISTORY, "history");
        addNavigation(navList, NAV_GROUPS, "groups");
        addNavigation(navList, NAV_SPAM, "spam");
        addNavigation(navList, NAV_NEW_REGISTRATIONS, "new_user");
        addNavigation(navList, NAV_REGISTRATION_CHART, "reg_chart");
        addNavigation(navList, NAV_FRIEND_STATS, "friend_stats");
        addNavigation(navList, NAV_USER_ACTIVITY, "activity");
        addNavigation(navList, NAV_ACTIVITY_CHART, "act_chart");

        JScrollPane scrollPane = new JScrollPane(navList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(AppTheme.SIDEBAR_BG);
        scrollPane.setBackground(AppTheme.SIDEBAR_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        add(brandHeader, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        updateNavigationSelection();
    }

    public static boolean isPersonalDestination(String itemLabel) {
        return NAV_PERSONAL_MESSAGES.equals(itemLabel)
                || NAV_PERSONAL_FRIENDS.equals(itemLabel)
                || NAV_PERSONAL_GROUPS.equals(itemLabel)
                || NAV_PERSONAL_SEARCH.equals(itemLabel)
                || NAV_PERSONAL_REQUESTS.equals(itemLabel)
                || NAV_PERSONAL_SETTINGS.equals(itemLabel);
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

    private void addSectionHeader(JPanel parent, String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.FONT_CAPTION_BOLD);
        label.setForeground(AppTheme.SIDEBAR_TEXT_MUTED);
        label.setBorder(BorderFactory.createEmptyBorder(2, 10, 6, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(label);
    }

    private void addNavigation(JPanel parent, String label, String iconKind) {
        JButton button = new JButton(label, new AdminNavigationIcon(iconKind));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(10);
        button.setForeground(AppTheme.SIDEBAR_NAV_TEXT);
        button.setBackground(AppTheme.SIDEBAR_BG);
        button.setFont(AppTheme.FONT_BODY);
        button.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 8));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, NAV_BUTTON_HEIGHT));
        button.setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH - 28, NAV_BUTTON_HEIGHT));

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
        parent.add(Box.createVerticalStrut(4));
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
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JSeparator sep = new JSeparator();
        sep.setForeground(AppTheme.SIDEBAR_BORDER);
        sep.setBackground(AppTheme.SIDEBAR_BORDER);
        wrapper.add(sep, BorderLayout.NORTH);

        JPanel footer = new JPanel(new GridLayout(0, 1, 0, 5));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 0));

        JButton logout = new JButton("Đăng xuất");
        logout.setFont(AppTheme.FONT_BODY);
        logout.setForeground(AppTheme.SIDEBAR_FOOTER_TEXT);
        logout.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 0));
        logout.setBorderPainted(false);
        logout.setContentAreaFilled(false);
        logout.setFocusPainted(false);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.addActionListener(e -> handleLogout());

        JLabel who = new JLabel(UserSessionProfile.DEMO_ADMIN.fullName() + " · Phiên bản 1");
        who.setForeground(AppTheme.SIDEBAR_FOOTER_TEXT);
        who.setFont(AppTheme.FONT_CAPTION);

        JLabel note = new JLabel("Dữ liệu mẫu · Swing UI");
        note.setForeground(AppTheme.SIDEBAR_TEXT_MUTED);
        note.setFont(AppTheme.FONT_SIDEBAR_NOTE);

        footer.add(logout);
        footer.add(who);
        footer.add(note);
        wrapper.add(footer, BorderLayout.CENTER);
        return wrapper;
    }

    private void handleLogout() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        MockLoginFrame loginFrame = new MockLoginFrame();
        loginFrame.setVisible(true);
        if (owner != null) {
            owner.dispose();
        }
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
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }

        @Override
        public void paintIcon(Component c, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c.getForeground());
            g.setStroke(new BasicStroke(1.65f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (kind) {
                case "chat", "groups" -> {
                    g.drawRoundRect(2, 2, 16, 12, 4, 4);
                    g.drawLine(5, 14, 5, 18);
                    g.drawLine(5, 18, 9, 14);
                }
                case "search" -> {
                    g.drawOval(2, 2, 11, 11);
                    g.drawLine(12, 12, 18, 18);
                }
                case "friends", "group", "users", "friend_stats" -> {
                    g.drawOval(3, 2, 6, 6);
                    g.drawOval(12, 4, 5, 5);
                    g.drawArc(1, 9, 12, 11, 0, 180);
                    g.drawArc(10, 10, 9, 9, 0, 145);
                }
                case "requests", "new_user" -> {
                    g.drawOval(3, 2, 6, 6);
                    g.drawArc(1, 9, 12, 11, 0, 180);
                    g.drawLine(14, 12, 19, 12);
                    g.drawLine(16, 9, 16, 15);
                }
                case "settings" -> {
                    g.drawOval(4, 4, 12, 12);
                    g.drawOval(7, 7, 6, 6);
                    for (int a = 0; a < 8; a++) {
                        double t = a * Math.PI / 4;
                        g.drawLine((int) (10 + 7 * Math.cos(t)), (int) (10 + 7 * Math.sin(t)),
                                (int) (10 + 9 * Math.cos(t)), (int) (10 + 9 * Math.sin(t)));
                    }
                }
                case "history" -> {
                    g.drawOval(2, 2, 16, 16);
                    g.drawLine(10, 5, 10, 10);
                    g.drawLine(10, 10, 14, 13);
                }
                case "spam" -> {
                    g.drawRoundRect(2, 2, 16, 16, 4, 4);
                    g.drawLine(10, 6, 10, 11);
                    g.fillOval(9, 14, 2, 2);
                }
                case "reg_chart", "act_chart" -> {
                    g.drawLine(2, 17, 18, 17);
                    g.drawRect(4, 10, 3, 7);
                    g.drawRect(9, 5, 3, 12);
                    g.drawRect(14, 8, 3, 9);
                }
                case "activity" -> {
                    g.drawLine(1, 10, 5, 10);
                    g.drawLine(5, 10, 8, 4);
                    g.drawLine(8, 4, 12, 16);
                    g.drawLine(12, 16, 15, 10);
                    g.drawLine(15, 10, 19, 10);
                }
                default -> g.drawOval(3, 3, 14, 14);
            }
            g.dispose();
        }
    }
}
