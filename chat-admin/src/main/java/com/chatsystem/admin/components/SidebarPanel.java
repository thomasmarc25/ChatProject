package com.chatsystem.admin.components;

import com.chatsystem.admin.theme.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dark 240px left sidebar navigation panel for the ChatSystem Admin dashboard.
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

    private static final String[] MANAGEMENT_NAV_ITEMS = {
            NAV_USERS,
            NAV_LOGIN_HISTORY,
            NAV_GROUPS,
            NAV_SPAM
    };

    private static final String[] ANALYTICS_NAV_ITEMS = {
            NAV_NEW_REGISTRATIONS,
            NAV_REGISTRATION_CHART,
            NAV_FRIEND_STATS,
            NAV_USER_ACTIVITY,
            NAV_ACTIVITY_CHART
    };

    private static final int NAV_BUTTON_HEIGHT = 36;

    private final List<NavButton> navButtons = new ArrayList<>();
    private String selectedItem = NAV_USERS;
    private Consumer<String> navigationListener;

    public SidebarPanel() {
        super(new BorderLayout());
        setBackground(AppTheme.SIDEBAR_BG);
        setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 0));
        setMinimumSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 0));

        add(createBrandHeader(), BorderLayout.NORTH);
        add(createNavigationMenu(), BorderLayout.CENTER);
        add(createProfileFooter(), BorderLayout.SOUTH);
    }

    public void setNavigationListener(Consumer<String> listener) {
        this.navigationListener = listener;
    }

    public String getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(String itemLabel) {
        this.selectedItem = itemLabel;
        for (NavButton button : navButtons) {
            button.setSelectedState(button.getNavLabel().equals(itemLabel));
        }
        repaint();
    }

    private JPanel createBrandHeader() {
        JPanel brandPanel = new JPanel(new BorderLayout(AppTheme.SPACING_SM + 4, 0));
        brandPanel.setBackground(AppTheme.SIDEBAR_BG);
        brandPanel.setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH, AppTheme.HEADER_HEIGHT));
        brandPanel.setMinimumSize(new Dimension(AppTheme.SIDEBAR_WIDTH, AppTheme.HEADER_HEIGHT));
        brandPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SIDEBAR_BORDER),
                BorderFactory.createEmptyBorder(0, AppTheme.SPACING_MD, 0, AppTheme.SPACING_MD)
        ));

        JPanel badgeWrapper = new JPanel(new BorderLayout());
        badgeWrapper.setOpaque(false);
        JPanel brandBadge = new RoundedBadgePanel("CS", AppTheme.PRIMARY, AppTheme.SIDEBAR_TEXT, 36);
        JPanel badgeCenter = new JPanel();
        badgeCenter.setLayout(new BoxLayout(badgeCenter, BoxLayout.Y_AXIS));
        badgeCenter.setOpaque(false);
        badgeCenter.add(Box.createVerticalGlue());
        brandBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        badgeCenter.add(brandBadge);
        badgeCenter.add(Box.createVerticalGlue());
        badgeWrapper.add(badgeCenter, BorderLayout.CENTER);

        JPanel textGroup = new JPanel();
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));
        textGroup.setOpaque(false);

        JLabel titleLabel = new JLabel("ChatSystem");
        titleLabel.setFont(AppTheme.FONT_SECTION_TITLE);
        titleLabel.setForeground(AppTheme.SIDEBAR_TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Admin Dashboard");
        subtitleLabel.setFont(AppTheme.FONT_CAPTION);
        subtitleLabel.setForeground(AppTheme.SIDEBAR_TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textGroup.add(Box.createVerticalGlue());
        textGroup.add(titleLabel);
        textGroup.add(Box.createVerticalStrut(2));
        textGroup.add(subtitleLabel);
        textGroup.add(Box.createVerticalGlue());

        brandPanel.add(badgeWrapper, BorderLayout.WEST);
        brandPanel.add(textGroup, BorderLayout.CENTER);
        return brandPanel;
    }

    private JPanel createNavigationMenu() {
        JPanel navContainer = new JPanel();
        navContainer.setLayout(new BoxLayout(navContainer, BoxLayout.Y_AXIS));
        navContainer.setBackground(AppTheme.SIDEBAR_BG);
        navContainer.setBorder(BorderFactory.createEmptyBorder(
                AppTheme.SPACING_MD - 2,
                AppTheme.SPACING_MD - 4,
                AppTheme.SPACING_MD - 2,
                AppTheme.SPACING_MD - 4
        ));

        navContainer.add(createSectionGroupLabel("QUẢN LÝ HỆ THỐNG"));
        for (String item : MANAGEMENT_NAV_ITEMS) {
            addNavButton(navContainer, item);
        }

        navContainer.add(Box.createVerticalStrut(AppTheme.SPACING_SM));
        navContainer.add(createSectionGroupLabel("THỐNG KÊ & BIỂU ĐỒ"));
        for (String item : ANALYTICS_NAV_ITEMS) {
            addNavButton(navContainer, item);
        }

        navContainer.add(Box.createVerticalGlue());
        return navContainer;
    }

    private JLabel createSectionGroupLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.FONT_CAPTION_BOLD);
        label.setForeground(AppTheme.SIDEBAR_TEXT_MUTED);
        label.setBorder(BorderFactory.createEmptyBorder(2, AppTheme.SPACING_SM, 6, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void addNavButton(JPanel navContainer, String item) {
        NavButton btn = new NavButton(item, item.equals(selectedItem));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> {
            setSelectedItem(item);
            if (navigationListener != null) {
                navigationListener.accept(item);
            }
        });
        navButtons.add(btn);
        navContainer.add(btn);
        navContainer.add(Box.createVerticalStrut(4));
    }

    private JPanel createProfileFooter() {
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setBackground(AppTheme.SIDEBAR_BG);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.SIDEBAR_BORDER),
                BorderFactory.createEmptyBorder(
                        AppTheme.SPACING_MD - 4,
                        AppTheme.SPACING_MD,
                        AppTheme.SPACING_MD - 4,
                        AppTheme.SPACING_MD
                )
        ));

        JPanel profileRow = new JPanel(new BorderLayout(AppTheme.SPACING_SM + 2, 0));
        profileRow.setOpaque(false);
        profileRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel avatarBadge = new RoundedBadgePanel("AD", AppTheme.SIDEBAR_SURFACE, AppTheme.SIDEBAR_TEXT, 36);

        JPanel infoGroup = new JPanel();
        infoGroup.setLayout(new BoxLayout(infoGroup, BoxLayout.Y_AXIS));
        infoGroup.setOpaque(false);

        JLabel adminTitleLabel = new JLabel("Administrator");
        adminTitleLabel.setFont(AppTheme.FONT_BODY_BOLD);
        adminTitleLabel.setForeground(AppTheme.SIDEBAR_TEXT);
        adminTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel adminRoleLabel = new JLabel("Quản trị viên");
        adminRoleLabel.setFont(AppTheme.FONT_CAPTION);
        adminRoleLabel.setForeground(AppTheme.SIDEBAR_TEXT_MUTED);
        adminRoleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoGroup.add(adminTitleLabel);
        infoGroup.add(Box.createVerticalStrut(2));
        infoGroup.add(adminRoleLabel);

        profileRow.add(avatarBadge, BorderLayout.WEST);
        profileRow.add(infoGroup, BorderLayout.CENTER);

        JButton logoutButton = createLogoutButton();
        logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        footerPanel.add(profileRow);
        footerPanel.add(Box.createVerticalStrut(AppTheme.SPACING_SM + 2));
        footerPanel.add(logoutButton);

        return footerPanel;
    }

    private JButton createLogoutButton() {
        return new JButton("Đăng xuất") {
            private boolean hovered = false;

            {
                setFont(AppTheme.FONT_BODY_BOLD);
                setForeground(AppTheme.SIDEBAR_TEXT);
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setHorizontalAlignment(SwingConstants.CENTER);
                Dimension size = new Dimension(
                        AppTheme.SIDEBAR_WIDTH - (AppTheme.SPACING_MD * 2),
                        36
                );
                setPreferredSize(size);
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                setMinimumSize(new Dimension(120, 36));

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = hovered ? AppTheme.DANGER : AppTheme.SIDEBAR_SURFACE;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.CORNER_RADIUS, AppTheme.CORNER_RADIUS);
                if (!hovered) {
                    g2.setColor(AppTheme.SIDEBAR_BORDER);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppTheme.CORNER_RADIUS, AppTheme.CORNER_RADIUS);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
    }

    /**
     * Custom rounded navigation item button for the dark sidebar.
     */
    private static final class NavButton extends JButton {

        private final String navLabel;
        private boolean selectedState;
        private boolean hovered;

        private NavButton(String navLabel, boolean selectedState) {
            super(navLabel);
            this.navLabel = navLabel;
            this.selectedState = selectedState;

            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(0, AppTheme.SPACING_MD - 2, 0, AppTheme.SPACING_MD - 2));

            Dimension targetSize = new Dimension(
                    AppTheme.SIDEBAR_WIDTH - ((AppTheme.SPACING_MD - 4) * 2),
                    NAV_BUTTON_HEIGHT
            );
            setPreferredSize(targetSize);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, NAV_BUTTON_HEIGHT));
            setMinimumSize(new Dimension(140, NAV_BUTTON_HEIGHT));

            updateTypography();

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        private String getNavLabel() {
            return navLabel;
        }

        private void setSelectedState(boolean selectedState) {
            this.selectedState = selectedState;
            updateTypography();
            repaint();
        }

        private void updateTypography() {
            if (selectedState) {
                setFont(AppTheme.FONT_BODY_BOLD);
                setForeground(AppTheme.SIDEBAR_TEXT);
            } else {
                setFont(AppTheme.FONT_BODY);
                setForeground(AppTheme.SIDEBAR_TEXT_MUTED);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (selectedState) {
                g2.setColor(hovered ? AppTheme.PRIMARY_HOVER : AppTheme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.CORNER_RADIUS, AppTheme.CORNER_RADIUS);
            } else if (hovered) {
                g2.setColor(AppTheme.SIDEBAR_SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.CORNER_RADIUS, AppTheme.CORNER_RADIUS);
                setForeground(AppTheme.SIDEBAR_TEXT);
            } else {
                setForeground(AppTheme.SIDEBAR_TEXT_MUTED);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Compact rounded badge painted with Java2D (no external icon dependency).
     */
    private static final class RoundedBadgePanel extends JPanel {

        private final Color badgeBg;
        private final int diameter;

        private RoundedBadgePanel(String initials, Color badgeBg, Color textColor, int diameter) {
            super(new FlowLayout(FlowLayout.CENTER, 0, 0));
            this.badgeBg = badgeBg;
            this.diameter = diameter;
            setOpaque(false);
            Dimension size = new Dimension(diameter, diameter);
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);

            setLayout(new BorderLayout());
            JLabel label = new JLabel(initials, SwingConstants.CENTER);
            label.setFont(AppTheme.FONT_CAPTION_BOLD);
            label.setForeground(textColor);
            add(label, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(badgeBg);
            g2.fillRoundRect(0, 0, diameter, diameter, AppTheme.CORNER_RADIUS, AppTheme.CORNER_RADIUS);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
