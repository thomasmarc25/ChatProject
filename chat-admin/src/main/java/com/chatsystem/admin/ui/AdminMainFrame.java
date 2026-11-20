package com.chatsystem.admin.ui;

import com.chatsystem.admin.components.HeaderPanel;
import com.chatsystem.admin.components.SidebarPanel;
import com.chatsystem.admin.data.MockUserRepository;
import com.chatsystem.admin.theme.AppTheme;
import com.chatsystem.user.UserSessionProfile;
import com.chatsystem.user.UserWorkspaceHost;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;

/**
 * Main application frame for ChatSystem Admin (Version 1 Mock-Data Preview).
 * Hosts the dark {@link SidebarPanel} organized into {@code Cá nhân} (6 embedded User pages
 * reused directly from {@code com.chatsystem.user.UserWorkspaceHost}) and {@code Quản trị}
 * (all 9 Administrator screens), the 64px white {@link HeaderPanel}, and the {@link CardLayout}
 * content deck preserving active conversation and draft state across switches.
 */
public class AdminMainFrame extends JFrame {

    private static final String CARD_PERSONAL_WORKSPACE = "PERSONAL_WORKSPACE";
    private static final String CARD_USER_MANAGEMENT = "USER_MANAGEMENT";
    private static final String CARD_LOGIN_HISTORY = "LOGIN_HISTORY";
    private static final String CARD_CHAT_GROUPS = "CHAT_GROUPS";
    private static final String CARD_SPAM_REPORTS = "SPAM_REPORTS";
    private static final String CARD_NEW_REGISTRATIONS = "NEW_REGISTRATIONS";
    private static final String CARD_REGISTRATION_CHART = "REGISTRATION_CHART";
    private static final String CARD_FRIEND_STATS = "FRIEND_STATS";
    private static final String CARD_USER_ACTIVITY = "USER_ACTIVITY";
    private static final String CARD_ACTIVITY_CHART = "ACTIVITY_CHART";

    private final SidebarPanel sidebarPanel;
    private final HeaderPanel headerPanel;
    private final CardLayout contentCardLayout;
    private final JPanel contentDeckPanel;

    private UserWorkspaceHost personalWorkspace;
    private RegistrationChartPanel registrationChartPanel;
    private ActivityChartPanel activityChartPanel;

    public AdminMainFrame() {
        super("ChatSystem Admin (Version 1 Mock Preview) — Quản lý người dùng");
        this.sidebarPanel = new SidebarPanel();
        this.headerPanel = new HeaderPanel();
        this.contentCardLayout = new CardLayout();
        this.contentDeckPanel = new JPanel(contentCardLayout);

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

        // Reuse Member 1's actual Swing panels for all 6 personal destinations
        this.personalWorkspace = new UserWorkspaceHost(
                UserSessionProfile.DEMO_ADMIN,
                true,
                this::navigateTo,
                null
        );

        MockUserRepository userRepository = new MockUserRepository();
        UserManagementPanel userManagementPanel = new UserManagementPanel(userRepository);
        LoginHistoryPanel loginHistoryPanel = new LoginHistoryPanel(userRepository);
        ChatGroupManagementPanel chatGroupManagementPanel = new ChatGroupManagementPanel(userRepository);
        SpamReportPanel spamReportPanel = new SpamReportPanel(userRepository);
        NewUserRegistrationPanel newUserRegistrationPanel = new NewUserRegistrationPanel(userRepository);
        this.registrationChartPanel = new RegistrationChartPanel(userRepository);
        FriendStatisticsPanel friendStatisticsPanel = new FriendStatisticsPanel(userRepository);
        UserActivityPanel userActivityPanel = new UserActivityPanel(userRepository);
        this.activityChartPanel = new ActivityChartPanel(userRepository);

        contentDeckPanel.setBackground(AppTheme.PAGE_BG);
        contentDeckPanel.add(personalWorkspace, CARD_PERSONAL_WORKSPACE);
        contentDeckPanel.add(userManagementPanel, CARD_USER_MANAGEMENT);
        contentDeckPanel.add(loginHistoryPanel, CARD_LOGIN_HISTORY);
        contentDeckPanel.add(chatGroupManagementPanel, CARD_CHAT_GROUPS);
        contentDeckPanel.add(spamReportPanel, CARD_SPAM_REPORTS);
        contentDeckPanel.add(newUserRegistrationPanel, CARD_NEW_REGISTRATIONS);
        contentDeckPanel.add(registrationChartPanel, CARD_REGISTRATION_CHART);
        contentDeckPanel.add(friendStatisticsPanel, CARD_FRIEND_STATS);
        contentDeckPanel.add(userActivityPanel, CARD_USER_ACTIVITY);
        contentDeckPanel.add(activityChartPanel, CARD_ACTIVITY_CHART);
        contentCardLayout.show(contentDeckPanel, CARD_USER_MANAGEMENT);

        mainColumn.add(headerPanel, BorderLayout.NORTH);
        mainColumn.add(contentDeckPanel, BorderLayout.CENTER);

        rootPanel.add(sidebarPanel, BorderLayout.WEST);
        rootPanel.add(mainColumn, BorderLayout.CENTER);

        setContentPane(rootPanel);
    }

    private void bindNavigation() {
        sidebarPanel.setNavigationListener(this::navigateTo);
    }

    public void navigateTo(String selectedSection) {
        sidebarPanel.setSelectedItem(selectedSection);
        boolean isPersonal = SidebarPanel.isPersonalDestination(selectedSection);
        headerPanel.setSection(isPersonal ? "Cá nhân" : "Quản trị", selectedSection);
        setTitle("ChatSystem Admin (Version 1 Mock Preview) — " + selectedSection);

        if (isPersonal) {
            personalWorkspace.showPersonalPage(selectedSection);
            contentCardLayout.show(contentDeckPanel, CARD_PERSONAL_WORKSPACE);
            return;
        }

        switch (selectedSection) {
            case SidebarPanel.NAV_USERS -> contentCardLayout.show(contentDeckPanel, CARD_USER_MANAGEMENT);
            case SidebarPanel.NAV_LOGIN_HISTORY -> contentCardLayout.show(contentDeckPanel, CARD_LOGIN_HISTORY);
            case SidebarPanel.NAV_GROUPS -> contentCardLayout.show(contentDeckPanel, CARD_CHAT_GROUPS);
            case SidebarPanel.NAV_SPAM -> contentCardLayout.show(contentDeckPanel, CARD_SPAM_REPORTS);
            case SidebarPanel.NAV_NEW_REGISTRATIONS -> contentCardLayout.show(contentDeckPanel, CARD_NEW_REGISTRATIONS);
            case SidebarPanel.NAV_REGISTRATION_CHART -> contentCardLayout.show(contentDeckPanel, CARD_REGISTRATION_CHART);
            case SidebarPanel.NAV_FRIEND_STATS -> contentCardLayout.show(contentDeckPanel, CARD_FRIEND_STATS);
            case SidebarPanel.NAV_USER_ACTIVITY -> contentCardLayout.show(contentDeckPanel, CARD_USER_ACTIVITY);
            case SidebarPanel.NAV_ACTIVITY_CHART -> contentCardLayout.show(contentDeckPanel, CARD_ACTIVITY_CHART);
            default -> contentCardLayout.show(contentDeckPanel, CARD_USER_MANAGEMENT);
        }
    }

    public UserWorkspaceHost getPersonalWorkspace() {
        return personalWorkspace;
    }

    public RegistrationChartPanel getRegistrationChartPanel() {
        return registrationChartPanel;
    }

    public ActivityChartPanel getActivityChartPanel() {
        return activityChartPanel;
    }
}
