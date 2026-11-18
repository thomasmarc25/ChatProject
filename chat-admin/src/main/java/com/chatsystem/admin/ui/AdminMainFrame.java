package com.chatsystem.admin.ui;

import com.chatsystem.admin.components.HeaderPanel;
import com.chatsystem.admin.components.SidebarPanel;
import com.chatsystem.admin.data.MockUserRepository;
import com.chatsystem.admin.theme.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagLayout;

/**
 * Main application frame for ChatSystem Admin.
 * Hosts the 240px dark SidebarPanel (9 navigation destinations), 64px white HeaderPanel,
 * and the CardLayout content deck.
 */
public class AdminMainFrame extends JFrame {

    private static final String CARD_USER_MANAGEMENT = "USER_MANAGEMENT";
    private static final String CARD_LOGIN_HISTORY = "LOGIN_HISTORY";
    private static final String CARD_CHAT_GROUPS = "CHAT_GROUPS";
    private static final String CARD_SPAM_REPORTS = "SPAM_REPORTS";
    private static final String CARD_PLACEHOLDER = "PLACEHOLDER";

    private final SidebarPanel sidebarPanel;
    private final HeaderPanel headerPanel;
    private final CardLayout contentCardLayout;
    private final JPanel contentDeckPanel;
    private final JLabel placeholderBadgeLabel;
    private final JLabel placeholderTitleLabel;
    private final JLabel placeholderSubtitleLabel;

    public AdminMainFrame() {
        super("ChatSystem Admin — Quản lý người dùng");
        this.sidebarPanel = new SidebarPanel();
        this.headerPanel = new HeaderPanel();
        this.contentCardLayout = new CardLayout();
        this.contentDeckPanel = new JPanel(contentCardLayout);
        this.placeholderBadgeLabel = new JLabel("MÀN HÌNH ĐANG CHỜ TRIỂN KHAI (PLACEHOLDER)", SwingConstants.CENTER);
        this.placeholderTitleLabel = new JLabel("Tính năng đang phát triển", SwingConstants.CENTER);
        this.placeholderSubtitleLabel = new JLabel("", SwingConstants.CENTER);

        initWindow();
        initShellLayout();
        bindNavigation();
    }

    private void initWindow() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(AppTheme.DEFAULT_WINDOW_SIZE);
        setMinimumSize(AppTheme.MIN_WINDOW_SIZE);
        setResizable(true);
        setLocationRelativeTo(null);
    }

    private void initShellLayout() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(AppTheme.PAGE_BG);

        JPanel mainColumn = new JPanel(new BorderLayout());
        mainColumn.setBackground(AppTheme.PAGE_BG);

        MockUserRepository userRepository = new MockUserRepository();
        UserManagementPanel userManagementPanel = new UserManagementPanel(userRepository);
        LoginHistoryPanel loginHistoryPanel = new LoginHistoryPanel(userRepository);
        ChatGroupManagementPanel chatGroupManagementPanel = new ChatGroupManagementPanel(userRepository);
        SpamReportPanel spamReportPanel = new SpamReportPanel(userRepository);
        JPanel placeholderPanel = createSectionPlaceholderPanel();

        contentDeckPanel.setBackground(AppTheme.PAGE_BG);
        contentDeckPanel.add(userManagementPanel, CARD_USER_MANAGEMENT);
        contentDeckPanel.add(loginHistoryPanel, CARD_LOGIN_HISTORY);
        contentDeckPanel.add(chatGroupManagementPanel, CARD_CHAT_GROUPS);
        contentDeckPanel.add(spamReportPanel, CARD_SPAM_REPORTS);
        contentDeckPanel.add(placeholderPanel, CARD_PLACEHOLDER);
        contentCardLayout.show(contentDeckPanel, CARD_USER_MANAGEMENT);

        mainColumn.add(headerPanel, BorderLayout.NORTH);
        mainColumn.add(contentDeckPanel, BorderLayout.CENTER);

        rootPanel.add(sidebarPanel, BorderLayout.WEST);
        rootPanel.add(mainColumn, BorderLayout.CENTER);

        setContentPane(rootPanel);
    }

    private JPanel createSectionPlaceholderPanel() {
        JPanel contentSurface = new JPanel(new BorderLayout());
        contentSurface.setBackground(AppTheme.PAGE_BG);
        contentSurface.setBorder(BorderFactory.createEmptyBorder(
                AppTheme.CONTENT_PADDING,
                AppTheme.CONTENT_PADDING,
                AppTheme.CONTENT_PADDING,
                AppTheme.CONTENT_PADDING
        ));

        JPanel cardSurface = new JPanel(new GridBagLayout());
        cardSurface.setBackground(AppTheme.SURFACE_CARD);
        cardSurface.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(
                        AppTheme.SPACING_LG * 2,
                        AppTheme.SPACING_LG * 2,
                        AppTheme.SPACING_LG * 2,
                        AppTheme.SPACING_LG * 2
                )
        ));

        JPanel textBox = new JPanel();
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        textBox.setOpaque(false);

        placeholderBadgeLabel.setFont(AppTheme.FONT_CAPTION_BOLD);
        placeholderBadgeLabel.setForeground(AppTheme.WARNING);
        placeholderBadgeLabel.setOpaque(true);
        placeholderBadgeLabel.setBackground(AppTheme.WARNING_BG);
        placeholderBadgeLabel.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        placeholderBadgeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        placeholderTitleLabel.setFont(AppTheme.FONT_PAGE_TITLE);
        placeholderTitleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        placeholderTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        placeholderSubtitleLabel.setFont(AppTheme.FONT_BODY);
        placeholderSubtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        placeholderSubtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        textBox.add(placeholderBadgeLabel);
        textBox.add(Box.createVerticalStrut(AppTheme.SPACING_MD));
        textBox.add(placeholderTitleLabel);
        textBox.add(Box.createVerticalStrut(AppTheme.SPACING_SM));
        textBox.add(placeholderSubtitleLabel);

        cardSurface.add(textBox);
        contentSurface.add(cardSurface, BorderLayout.CENTER);

        return contentSurface;
    }

    private void bindNavigation() {
        sidebarPanel.setNavigationListener(this::navigateTo);
    }

    public void navigateTo(String selectedSection) {
        sidebarPanel.setSelectedItem(selectedSection);
        headerPanel.setSection(selectedSection);
        setTitle("ChatSystem Admin — " + selectedSection);
        switch (selectedSection) {
            case SidebarPanel.NAV_USERS -> contentCardLayout.show(contentDeckPanel, CARD_USER_MANAGEMENT);
            case SidebarPanel.NAV_LOGIN_HISTORY -> contentCardLayout.show(contentDeckPanel, CARD_LOGIN_HISTORY);
            case SidebarPanel.NAV_GROUPS -> contentCardLayout.show(contentDeckPanel, CARD_CHAT_GROUPS);
            case SidebarPanel.NAV_SPAM -> contentCardLayout.show(contentDeckPanel, CARD_SPAM_REPORTS);
            default -> {
                placeholderTitleLabel.setText(selectedSection);
                placeholderSubtitleLabel.setText(describePlaceholderDestination(selectedSection));
                contentCardLayout.show(contentDeckPanel, CARD_PLACEHOLDER);
            }
        }
    }

    private String describePlaceholderDestination(String section) {
        return switch (section) {
            case SidebarPanel.NAV_NEW_REGISTRATIONS ->
                    "Màn hình danh sách người dùng đăng ký mới theo khoảng thời gian (Sẽ triển khai ở Bước G).";
            case SidebarPanel.NAV_REGISTRATION_CHART ->
                    "Màn hình biểu đồ số lượng người đăng ký mới 12 tháng theo năm (Sẽ triển khai ở Bước G).";
            case SidebarPanel.NAV_FRIEND_STATS ->
                    "Màn hình thống kê bạn bè trực tiếp và bạn của bạn (Sẽ triển khai ở Bước G).";
            case SidebarPanel.NAV_USER_ACTIVITY ->
                    "Màn hình danh sách người dùng hoạt động và số liệu mở ứng dụng / chat (Sẽ triển khai ở Bước G).";
            case SidebarPanel.NAV_ACTIVITY_CHART ->
                    "Màn hình biểu đồ số lượng người mở ứng dụng 12 tháng theo năm (Sẽ triển khai ở Bước G).";
            default -> "Phân hệ \"" + section + "\" đang chờ triển khai.";
        };
    }
}
