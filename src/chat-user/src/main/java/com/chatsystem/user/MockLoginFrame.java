package com.chatsystem.user;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

/**
 * Unified Version 1 mock login entry window styled with {@link Ui} tokens.
 * Routes {@code USER} demo accounts (e.g. {@code nguyenvanan}) to {@link UserApp}
 * and {@code ADMIN} demo accounts ({@code admin}) to {@code com.chatsystem.admin.ui.AdminMainFrame}
 * via reflection so {@code chat-user} and {@code chat-admin} remain independently buildable
 * with zero circular Maven dependencies.
 */
public final class MockLoginFrame extends JFrame {

    final JTextField usernameField = Ui.input("Tên đăng nhập (VD: nguyenvanan hoặc admin)");
    final JPasswordField passwordField = new JPasswordField(22);
    final JLabel feedbackLabel = new JLabel(" ");

    public MockLoginFrame() {
        super("ChatSystem — Đăng nhập (Phiên bản 1 · Dữ liệu mẫu)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 530);
        setMinimumSize(new Dimension(820, 490));
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Ui.BG);

        // Left dark brand panel matching UserApp sidebar
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(Ui.NAV);
        left.setPreferredSize(new Dimension(330, 0));
        left.setBorder(BorderFactory.createEmptyBorder(34, 28, 28, 28));

        JPanel brandStack = new JPanel();
        brandStack.setOpaque(false);
        brandStack.setLayout(new BoxLayout(brandStack, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("ChatSystem");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26));
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("<html><div style='width:200px;'>Ứng dụng Desktop Java Swing — Tích hợp phân hệ Người dùng (User) và Quản trị viên (Admin).</div></html>");
        subtitle.setForeground(new Color(0xCBD5E1));
        subtitle.setFont(subtitle.getFont().deriveFont(13f));
        subtitle.setAlignmentX(LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x374151));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(LEFT_ALIGNMENT);

        JLabel demoTitle = new JLabel("Tài khoản demo (Phiên bản 1)");
        demoTitle.setForeground(Color.WHITE);
        demoTitle.setFont(demoTitle.getFont().deriveFont(Font.BOLD, 13f));
        demoTitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel demoInfo = new JLabel(
            "<html><div style='width:205px; line-height:1.45;'>"
            + "• <b>USER:</b> <code>nguyenvanan</code><br>"
            + "  Mở giao diện ChatSystem User<br><br>"
            + "• <b>ADMIN:</b> <code>admin</code><br>"
            + "  Mở giao diện ChatSystem Admin<br><br>"
            + "<i>Dữ liệu mẫu trong bộ nhớ — chưa kết nối máy chủ hoặc PostgreSQL.</i>"
            + "</div></html>"
        );
        demoInfo.setForeground(new Color(0x9CA3AF));
        demoInfo.setFont(demoInfo.getFont().deriveFont(12f));
        demoInfo.setAlignmentX(LEFT_ALIGNMENT);

        brandStack.add(title);
        brandStack.add(Box.createVerticalStrut(10));
        brandStack.add(subtitle);
        brandStack.add(Box.createVerticalStrut(22));
        brandStack.add(sep);
        brandStack.add(Box.createVerticalStrut(20));
        brandStack.add(demoTitle);
        brandStack.add(Box.createVerticalStrut(10));
        brandStack.add(demoInfo);
        left.add(brandStack, BorderLayout.NORTH);

        JLabel footerNote = new JLabel("Phiên bản 1 · Swing UI + FlatLaf");
        footerNote.setForeground(new Color(0x9CA3AF));
        footerNote.setFont(footerNote.getFont().deriveFont(11f));
        left.add(footerNote, BorderLayout.SOUTH);

        // Right login form card
        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setBackground(Ui.BG);
        rightWrapper.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Ui.BORDER, 1, true),
            BorderFactory.createEmptyBorder(26, 28, 24, 28)
        ));

        JLabel formHeading = new JLabel("Đăng nhập hệ thống");
        formHeading.setFont(formHeading.getFont().deriveFont(Font.BOLD, 22));
        formHeading.setForeground(Ui.TEXT);
        formHeading.setAlignmentX(LEFT_ALIGNMENT);

        JLabel formSub = new JLabel("Nhập tài khoản mẫu USER hoặc ADMIN để vào giao diện tương ứng");
        formSub.setForeground(Ui.MUTED);
        formSub.setFont(formSub.getFont().deriveFont(13f));
        formSub.setAlignmentX(LEFT_ALIGNMENT);

        usernameField.setText(ChatData.CURRENT_USERNAME);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        usernameField.setPreferredSize(new Dimension(380, 40));
        usernameField.setAlignmentX(LEFT_ALIGNMENT);

        passwordField.setText("UserDemo#2026");
        passwordField.putClientProperty("JTextField.placeholderText", "Mật khẩu mẫu (không kiểm tra máy chủ)");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setPreferredSize(new Dimension(380, 40));
        passwordField.setAlignmentX(LEFT_ALIGNMENT);

        JLabel userLbl = new JLabel("Tên đăng nhập");
        userLbl.setFont(userLbl.getFont().deriveFont(Font.BOLD, 13f));
        userLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel passLbl = new JLabel("Mật khẩu");
        passLbl.setFont(passLbl.getFont().deriveFont(Font.BOLD, 13f));
        passLbl.setAlignmentX(LEFT_ALIGNMENT);

        feedbackLabel.setForeground(Ui.DANGER);
        feedbackLabel.setFont(feedbackLabel.getFont().deriveFont(12f));
        feedbackLabel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel quickFillRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        quickFillRow.setOpaque(false);
        quickFillRow.setAlignmentX(LEFT_ALIGNMENT);
        JButton fillUser = Ui.button("Điền mẫu USER", () -> {
            usernameField.setText(ChatData.CURRENT_USERNAME);
            passwordField.setText("UserDemo#2026");
            feedbackLabel.setText(" ");
        });
        JButton fillAdmin = Ui.button("Điền mẫu ADMIN", () -> {
            usernameField.setText("admin");
            passwordField.setText("AdminDemo#2026");
            feedbackLabel.setText(" ");
        });
        quickFillRow.add(fillUser);
        quickFillRow.add(fillAdmin);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionRow.setOpaque(false);
        actionRow.setAlignmentX(LEFT_ALIGNMENT);
        JButton loginBtn = Ui.primaryButton("Đăng nhập", this::handleLogin);
        JButton registerBtn = Ui.button("Đăng ký / Quên mật khẩu", this::openRegistrationTab);
        actionRow.add(loginBtn);
        actionRow.add(registerBtn);

        usernameField.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin());

        card.add(formHeading);
        card.add(Box.createVerticalStrut(4));
        card.add(formSub);
        card.add(Box.createVerticalStrut(20));
        card.add(userLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(14));
        card.add(passLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(10));
        card.add(feedbackLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(quickFillRow);
        card.add(Box.createVerticalStrut(14));
        card.add(actionRow);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        rightWrapper.add(card, gbc);

        root.add(left, BorderLayout.WEST);
        root.add(rightWrapper, BorderLayout.CENTER);
        setContentPane(root);
        getRootPane().setDefaultButton(loginBtn);
    }

    /**
     * Attempts mock login routing based on username:
     * {@code admin} opens {@code AdminMainFrame}; other non-empty usernames open {@link UserApp}.
     */
    boolean loginAs(String rawUsername) {
        String username = rawUsername == null ? "" : rawUsername.strip().toLowerCase(Locale.ROOT);
        if (username.isEmpty()) {
            feedbackLabel.setText("Vui lòng nhập tên đăng nhập mẫu (VD: nguyenvanan hoặc admin).");
            return false;
        }
        if ("admin".equals(username)) {
            if (launchAdminWorkspace(this)) {
                dispose();
                return true;
            }
            return false;
        }
        UserApp userApp = new UserApp();
        userApp.setVisible(true);
        dispose();
        return true;
    }

    private void handleLogin() {
        loginAs(usernameField.getText());
    }

    private void openRegistrationTab() {
        UserApp userApp = new UserApp();
        userApp.account.tabs.setSelectedIndex(1);
        userApp.showPage("Tài khoản");
        userApp.setVisible(true);
        dispose();
    }

    /**
     * Launches {@code com.chatsystem.admin.ui.AdminMainFrame} via reflection when {@code chat-admin}
     * is present on the runtime classpath (no compile-time Maven dependency).
     */
    static boolean launchAdminWorkspace(Component owner) {
        try {
            Class<?> themeClass = Class.forName("com.chatsystem.admin.theme.AppTheme");
            themeClass.getMethod("setupLookAndFeel").invoke(null);
            Class<?> frameClass = Class.forName("com.chatsystem.admin.ui.AdminMainFrame");
            JFrame adminFrame = (JFrame) frameClass.getConstructor().newInstance();
            adminFrame.setVisible(true);
            return true;
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(
                owner,
                "Không tìm thấy phân hệ Admin trên classpath hiện tại.\n"
                    + "Hãy chạy lệnh khởi chạy hợp nhất (gồm cả chat-user và chat-admin) hoặc chạy riêng chat-admin.",
                "Phân hệ Admin chưa được nạp",
                JOptionPane.WARNING_MESSAGE
            );
            return false;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                owner,
                "Không thể mở giao diện Admin: " + ex.getMessage(),
                "Lỗi khởi chạy Admin",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }
}
