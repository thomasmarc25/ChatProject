package com.chatsystem.admin.ui;

import com.chatsystem.admin.model.User;
import com.chatsystem.admin.theme.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

/**
 * UI prototype modal dialog for updating a user's password using masked input.
 * Never displays existing passwords or password hashes, and immediately clears
 * sensitive character buffers without persisting password contents.
 */
public class PasswordUpdateDialog extends JDialog {

    private final JPasswordField newPasswordField;
    private final JPasswordField confirmPasswordField;
    private final JLabel feedbackLabel;
    private final char defaultEchoChar;

    public PasswordUpdateDialog(Window owner, User targetUser) {
        super(owner, "Cập nhật mật khẩu người dùng", ModalityType.APPLICATION_MODAL);

        this.newPasswordField = createMaskedPasswordField("Nhập mật khẩu mới (tối thiểu 8 ký tự)");
        this.confirmPasswordField = createMaskedPasswordField("Nhập lại mật khẩu mới để xác nhận");
        this.defaultEchoChar = this.newPasswordField.getEchoChar();
        this.feedbackLabel = new JLabel(" ");
        this.feedbackLabel.setFont(AppTheme.FONT_CAPTION_BOLD);
        this.feedbackLabel.setForeground(AppTheme.DANGER);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.SURFACE_CARD);

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createBody(targetUser), BorderLayout.CENTER);
        root.add(createFooter(), BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setMinimumSize(new Dimension(560, getPreferredSize().height));
        setSize(new Dimension(580, Math.max(440, getPreferredSize().height)));
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(AppTheme.SURFACE_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER),
                BorderFactory.createEmptyBorder(
                        AppTheme.SPACING_MD + 2,
                        AppTheme.CONTENT_PADDING,
                        AppTheme.SPACING_MD,
                        AppTheme.CONTENT_PADDING
                )
        ));

        JLabel titleLabel = new JLabel("Cập nhật mật khẩu tài khoản");
        titleLabel.setFont(AppTheme.FONT_SECTION_TITLE);
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(
                "Nhập mật khẩu mới bên dưới (dữ liệu được che ký tự và không lưu bản rõ)"
        );
        subtitleLabel.setFont(AppTheme.FONT_CAPTION);
        subtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitleLabel);
        return header;
    }

    private JPanel createBody(User user) {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(AppTheme.SURFACE_CARD);
        formPanel.setBorder(BorderFactory.createEmptyBorder(
                AppTheme.SPACING_MD,
                AppTheme.CONTENT_PADDING,
                AppTheme.SPACING_MD,
                AppTheme.CONTENT_PADDING
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Target user summary card
        JPanel userCard = new JPanel();
        userCard.setLayout(new BoxLayout(userCard, BoxLayout.Y_AXIS));
        userCard.setBackground(AppTheme.PAGE_BG);
        userCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        JLabel userTitle = new JLabel(
                "Tài khoản: " + user.getFullName() + " (@" + user.getUsername() + ")"
        );
        userTitle.setFont(AppTheme.FONT_BODY_BOLD);
        userTitle.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel userMeta = new JLabel(
                "Email: " + user.getEmail() + "  •  TT Tài khoản: " + user.getAccountStatusLabel()
        );
        userMeta.setFont(AppTheme.FONT_CAPTION);
        userMeta.setForeground(AppTheme.TEXT_SECONDARY);

        userCard.add(userTitle);
        userCard.add(Box.createVerticalStrut(3));
        userCard.add(userMeta);

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 14, 0);
        formPanel.add(userCard, gbc);

        // New password label & masked field
        JLabel newPwLabel = new JLabel("Mật khẩu mới *");
        newPwLabel.setFont(AppTheme.FONT_BODY_BOLD);
        newPwLabel.setForeground(AppTheme.TEXT_PRIMARY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 6, 0);
        formPanel.add(newPwLabel, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 12, 0);
        formPanel.add(newPasswordField, gbc);

        // Confirm password label & masked field
        JLabel confirmPwLabel = new JLabel("Xác nhận mật khẩu mới *");
        confirmPwLabel.setFont(AppTheme.FONT_BODY_BOLD);
        confirmPwLabel.setForeground(AppTheme.TEXT_PRIMARY);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 6, 0);
        formPanel.add(confirmPwLabel, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 8, 0);
        formPanel.add(confirmPasswordField, gbc);

        // Show password toggle checkbox
        JCheckBox showPasswordCheck = new JCheckBox("Hiển thị ký tự mật khẩu khi nhập");
        showPasswordCheck.setFont(AppTheme.FONT_CAPTION);
        showPasswordCheck.setForeground(AppTheme.TEXT_SECONDARY);
        showPasswordCheck.setOpaque(false);
        showPasswordCheck.setFocusPainted(false);
        showPasswordCheck.addActionListener(e -> {
            char echo = showPasswordCheck.isSelected() ? (char) 0 : defaultEchoChar;
            newPasswordField.setEchoChar(echo);
            confirmPasswordField.setEchoChar(echo);
        });
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 6, 0);
        formPanel.add(showPasswordCheck, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        formPanel.add(feedbackLabel, gbc);

        return formPanel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AppTheme.PAGE_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER),
                BorderFactory.createEmptyBorder(
                        AppTheme.SPACING_MD - 2,
                        AppTheme.CONTENT_PADDING,
                        AppTheme.SPACING_MD - 2,
                        AppTheme.CONTENT_PADDING
                )
        ));

        JLabel noteLabel = new JLabel("Nguyên mẫu (không lưu mật khẩu)");
        noteLabel.setFont(AppTheme.FONT_CAPTION);
        noteLabel.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SPACING_SM, 0));
        buttonGroup.setOpaque(false);

        JButton cancelButton = AppTheme.createSecondaryButton("Hủy", 96);
        cancelButton.addActionListener(e -> {
            clearSensitiveBuffers();
            dispose();
        });

        JButton updateButton = AppTheme.createPrimaryButton("Cập nhật mật khẩu", 168);
        updateButton.addActionListener(e -> handleValidateAndClose());

        buttonGroup.add(cancelButton);
        buttonGroup.add(updateButton);

        footer.add(noteLabel, BorderLayout.WEST);
        footer.add(buttonGroup, BorderLayout.EAST);
        return footer;
    }

    private void handleValidateAndClose() {
        char[] pw1 = newPasswordField.getPassword();
        char[] pw2 = confirmPasswordField.getPassword();
        try {
            if (pw1.length == 0 || pw2.length == 0) {
                feedbackLabel.setForeground(AppTheme.DANGER);
                feedbackLabel.setText("Vui lòng nhập đầy đủ mật khẩu mới và xác nhận mật khẩu.");
                return;
            }
            if (pw1.length < 8) {
                feedbackLabel.setForeground(AppTheme.DANGER);
                feedbackLabel.setText("Mật khẩu mới phải có độ dài tối thiểu 8 ký tự.");
                return;
            }
            if (!Arrays.equals(pw1, pw2)) {
                feedbackLabel.setForeground(AppTheme.DANGER);
                feedbackLabel.setText("Mật khẩu xác nhận không khớp với mật khẩu mới.");
                return;
            }
            clearSensitiveBuffers();
            dispose();
        } finally {
            Arrays.fill(pw1, '\0');
            Arrays.fill(pw2, '\0');
        }
    }

    private void clearSensitiveBuffers() {
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    private JPasswordField createMaskedPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setFont(AppTheme.FONT_BODY);
        field.setForeground(AppTheme.TEXT_PRIMARY);
        field.setPreferredSize(new Dimension(260, AppTheme.CONTROL_HEIGHT));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        return field;
    }
}
