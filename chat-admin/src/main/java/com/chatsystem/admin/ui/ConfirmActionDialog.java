package com.chatsystem.admin.ui;

import com.chatsystem.admin.model.SpamReportRecord;
import com.chatsystem.admin.model.User;
import com.chatsystem.admin.theme.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Reusable modal confirmation dialog for User Management actions
 * (Delete account confirmation and Lock/Unlock account confirmation).
 */
public class ConfirmActionDialog extends JDialog {

    private boolean confirmed = false;

    public ConfirmActionDialog(
            Window owner,
            String dialogTitle,
            String headingText,
            String descriptionText,
            User targetUser,
            String confirmButtonText,
            Color confirmAccentColor
    ) {
        super(owner, dialogTitle, ModalityType.APPLICATION_MODAL);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.SURFACE_CARD);

        root.add(createHeader(headingText, confirmAccentColor), BorderLayout.NORTH);
        root.add(createBody(descriptionText, targetUser), BorderLayout.CENTER);
        root.add(createFooter(confirmButtonText, confirmAccentColor), BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setMinimumSize(new Dimension(560, getPreferredSize().height));
        setSize(new Dimension(580, Math.max(320, getPreferredSize().height)));
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public static boolean showDeleteUserConfirm(Window owner, User user) {
        ConfirmActionDialog dialog = new ConfirmActionDialog(
                owner,
                "Xác nhận xóa người dùng",
                "Xóa tài khoản người dùng?",
                "Tài khoản sẽ được đánh dấu xóa mềm (deleted_at) và ẩn khỏi danh sách hiện hành. Dữ liệu thống kê lịch sử vẫn được bảo toàn.",
                user,
                "Xác nhận xóa",
                AppTheme.DANGER
        );
        dialog.setVisible(true);
        return dialog.isConfirmed();
    }

    public static boolean showLockToggleConfirm(Window owner, User user) {
        boolean willLock = !user.isLocked();
        String title = willLock ? "Xác nhận khóa tài khoản" : "Xác nhận mở khóa tài khoản";
        String heading = willLock ? "Khóa tài khoản người dùng?" : "Mở khóa tài khoản người dùng?";
        String desc = willLock
                ? "Người dùng bị khóa sẽ không thể đăng nhập hoặc gửi tin nhắn mới cho đến khi được quản trị viên mở khóa."
                : "Tài khoản sẽ được khôi phục trạng thái Hoạt động và có thể đăng nhập trở lại bình thường.";
        String actionText = willLock ? "Khóa tài khoản" : "Mở khóa tài khoản";
        Color accent = willLock ? AppTheme.WARNING : AppTheme.SUCCESS;

        ConfirmActionDialog dialog = new ConfirmActionDialog(
                owner,
                title,
                heading,
                desc,
                user,
                actionText,
                accent
        );
        dialog.setVisible(true);
        return dialog.isConfirmed();
    }

    public static boolean showSpamReportLockConfirm(
            Window owner,
            User reportedUser,
            SpamReportRecord report
    ) {
        boolean willLock = !reportedUser.isLocked();
        String title = willLock
                ? "Xác nhận khóa tài khoản bị báo cáo spam"
                : "Xác nhận mở khóa tài khoản bị báo cáo";
        String heading = willLock
                ? "Khóa tài khoản @" + reportedUser.getUsername() + " từ báo cáo #" + report.getId() + "?"
                : "Mở khóa tài khoản @" + reportedUser.getUsername() + "?";
        String desc = willLock
                ? "Báo cáo #" + report.getId() + " (" + report.getReason() + "): Tài khoản @"
                + reportedUser.getUsername() + " sẽ bị chuyển sang trạng thái Đã khóa và báo cáo được đánh dấu Đã xử lý (chỉ áp dụng in-memory)."
                : "Tài khoản @" + reportedUser.getUsername()
                + " sẽ được khôi phục trạng thái Hoạt động trong bộ nhớ xem trước (mock-only).";
        String actionText = willLock ? "Khóa tài khoản" : "Mở khóa TK";
        Color accent = willLock ? AppTheme.DANGER : AppTheme.SUCCESS;

        ConfirmActionDialog dialog = new ConfirmActionDialog(
                owner,
                title,
                heading,
                desc,
                reportedUser,
                actionText,
                accent
        );
        dialog.setVisible(true);
        return dialog.isConfirmed();
    }

    private JPanel createHeader(String headingText, Color accentColor) {
        JPanel header = new JPanel(new BorderLayout());
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

        JLabel titleLabel = new JLabel(headingText);
        titleLabel.setFont(AppTheme.FONT_SECTION_TITLE);
        titleLabel.setForeground(accentColor.equals(AppTheme.DANGER) ? AppTheme.DANGER : AppTheme.TEXT_PRIMARY);
        header.add(titleLabel, BorderLayout.CENTER);
        return header;
    }

    private JPanel createBody(String descriptionText, User user) {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(AppTheme.SURFACE_CARD);
        body.setBorder(BorderFactory.createEmptyBorder(
                AppTheme.SPACING_MD + 2,
                AppTheme.CONTENT_PADDING,
                AppTheme.SPACING_MD + 2,
                AppTheme.CONTENT_PADDING
        ));

        JPanel userSummaryCard = new JPanel();
        userSummaryCard.setLayout(new BoxLayout(userSummaryCard, BoxLayout.Y_AXIS));
        userSummaryCard.setBackground(AppTheme.PAGE_BG);
        userSummaryCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        userSummaryCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(user.getFullName() + " (@" + user.getUsername() + ")");
        nameLabel.setFont(AppTheme.FONT_BODY_BOLD);
        nameLabel.setForeground(AppTheme.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel metaLabel = new JLabel(
                "Email: " + user.getEmail()
                        + "  •  TK: " + user.getAccountStatusLabel()
                        + "  •  Phiên: " + user.getPresenceStatusLabel()
        );
        metaLabel.setFont(AppTheme.FONT_CAPTION);
        metaLabel.setForeground(AppTheme.TEXT_SECONDARY);
        metaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        userSummaryCard.add(nameLabel);
        userSummaryCard.add(Box.createVerticalStrut(4));
        userSummaryCard.add(metaLabel);

        JLabel descLabel = new JLabel("<html><div style='width:380px;'>" + descriptionText + "</div></html>");
        descLabel.setFont(AppTheme.FONT_BODY);
        descLabel.setForeground(AppTheme.TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(userSummaryCard);
        body.add(Box.createVerticalStrut(AppTheme.SPACING_MD));
        body.add(descLabel);
        return body;
    }

    private JPanel createFooter(String confirmButtonText, Color confirmAccentColor) {
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

        JLabel noteLabel = new JLabel("Chế độ xem trước (in-memory)");
        noteLabel.setFont(AppTheme.FONT_CAPTION);
        noteLabel.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SPACING_SM, 0));
        buttons.setOpaque(false);

        JButton cancelButton = new JButton("Hủy");
        cancelButton.setFont(AppTheme.FONT_BODY_BOLD);
        cancelButton.setForeground(AppTheme.TEXT_PRIMARY);
        cancelButton.setBackground(AppTheme.SURFACE_CARD);
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.setPreferredSize(new Dimension(96, AppTheme.CONTROL_HEIGHT));
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        JButton confirmButton = new JButton(confirmButtonText) {
            private boolean hovered = false;

            {
                setFont(AppTheme.FONT_BODY_BOLD);
                setForeground(AppTheme.SURFACE_CARD);
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setPreferredSize(new Dimension(164, AppTheme.CONTROL_HEIGHT));
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
                Color fill = hovered ? confirmAccentColor.darker() : confirmAccentColor;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.CORNER_RADIUS, AppTheme.CORNER_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        confirmButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        buttons.add(cancelButton);
        buttons.add(confirmButton);

        footer.add(noteLabel, BorderLayout.WEST);
        footer.add(buttons, BorderLayout.EAST);
        return footer;
    }
}
