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
 * Hosts the 240px dark SidebarPanel, 64px white HeaderPanel, and the UserManagementPanel screen.
 */
public class AdminMainFrame extends JFrame {

    private static final String CARD_USER_MANAGEMENT = "USER_MANAGEMENT";
    private static final String CARD_PLACEHOLDER = "PLACEHOLDER";

    private final SidebarPanel sidebarPanel;
    private final HeaderPanel headerPanel;
    private final CardLayout contentCardLayout;
    private final JPanel contentDeckPanel;
    private final JLabel placeholderTitleLabel;
    private final JLabel placeholderSubtitleLabel;

    public AdminMainFrame() {
        super("ChatSystem Admin — Quản lý người dùng");
        this.sidebarPanel = new SidebarPanel();
        this.headerPanel = new HeaderPanel();
        this.contentCardLayout = new CardLayout();
        this.contentDeckPanel = new JPanel(contentCardLayout);
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
        JPanel placeholderPanel = createSectionPlaceholderPanel();

        contentDeckPanel.setBackground(AppTheme.PAGE_BG);
        contentDeckPanel.add(userManagementPanel, CARD_USER_MANAGEMENT);
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

        placeholderTitleLabel.setFont(AppTheme.FONT_PAGE_TITLE);
        placeholderTitleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        placeholderTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        placeholderSubtitleLabel.setFont(AppTheme.FONT_BODY);
        placeholderSubtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        placeholderSubtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        textBox.add(placeholderTitleLabel);
        textBox.add(Box.createVerticalStrut(AppTheme.SPACING_SM));
        textBox.add(placeholderSubtitleLabel);

        cardSurface.add(textBox);
        contentSurface.add(cardSurface, BorderLayout.CENTER);

        return contentSurface;
    }

    private void bindNavigation() {
        sidebarPanel.setNavigationListener(selectedSection -> {
            headerPanel.setSection(selectedSection);
            if (SidebarPanel.NAV_USERS.equals(selectedSection)) {
                contentCardLayout.show(contentDeckPanel, CARD_USER_MANAGEMENT);
            } else {
                placeholderTitleLabel.setText("Tính năng đang phát triển");
                placeholderSubtitleLabel.setText(
                        "Phân hệ \"" + selectedSection + "\" hiện là mục điều hướng minh họa."
                );
                contentCardLayout.show(contentDeckPanel, CARD_PLACEHOLDER);
            }
        });
    }
}
