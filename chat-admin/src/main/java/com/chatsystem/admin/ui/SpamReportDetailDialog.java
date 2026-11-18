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
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modal dialog displaying full details of a selected spam report ({@code spam_reports}),
 * including reporter, reported user, report timestamp, reason, resolution metadata,
 * the synthetic direct-message snapshot ({@code message_snapshot}), and a direct button
 * to trigger lock-account confirmation targeting the reported user.
 */
public class SpamReportDetailDialog extends JDialog {

    public SpamReportDetailDialog(
            Window owner,
            SpamReportRecord report,
            User reportedUser,
            Runnable onTriggerLockConfirmation
    ) {
        super(
                owner,
                "Chi tiết báo cáo spam #" + report.getId() + " — @" + report.getReportedUsername(),
                ModalityType.APPLICATION_MODAL
        );

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.SURFACE_CARD);

        root.add(createHeader(report), BorderLayout.NORTH);
        root.add(createBody(report, reportedUser), BorderLayout.CENTER);
        root.add(createFooter(report, reportedUser, onTriggerLockConfirmation), BorderLayout.SOUTH);

        setContentPane(root);
        setSize(new Dimension(680, 535));
        setMinimumSize(new Dimension(620, 480));
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private JPanel createHeader(SpamReportRecord report) {
        JPanel header = new JPanel(new BorderLayout(AppTheme.SPACING_MD, 0));
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

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        JLabel titleLabel = new JLabel("Chi tiết báo cáo spam #" + report.getId());
        titleLabel.setFont(AppTheme.FONT_SECTION_TITLE);
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLabel = new JLabel(
                "Thời gian gửi báo cáo: " + report.getFormattedCreatedAt()
                        + "  •  Mã tin nhắn gốc (direct_message_id): #" + report.getDirectMessageId()
        );
        subLabel.setFont(AppTheme.FONT_CAPTION);
        subLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textCol.add(titleLabel);
        textCol.add(Box.createVerticalStrut(4));
        textCol.add(subLabel);

        boolean pending = report.isPending();
        JLabel statusBadge = new JLabel(report.getStatusDisplay());
        statusBadge.setFont(AppTheme.FONT_CAPTION_BOLD);
        statusBadge.setForeground(pending ? AppTheme.WARNING : AppTheme.SUCCESS);
        statusBadge.setOpaque(true);
        statusBadge.setBackground(pending ? AppTheme.WARNING_BG : AppTheme.SUCCESS_BG);
        statusBadge.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(statusBadge);

        header.add(textCol, BorderLayout.CENTER);
        header.add(badgeWrapper, BorderLayout.EAST);
        return header;
    }

    private JPanel createBody(SpamReportRecord report, User reportedUser) {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(AppTheme.SURFACE_CARD);
        body.setBorder(BorderFactory.createEmptyBorder(
                AppTheme.SPACING_MD,
                AppTheme.CONTENT_PADDING,
                AppTheme.SPACING_MD,
                AppTheme.CONTENT_PADDING
        ));

        // 1. Reporter & Reported User Grid
        JPanel participantsGrid = new JPanel(new GridLayout(1, 2, AppTheme.SPACING_MD, 0));
        participantsGrid.setOpaque(false);
        participantsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        participantsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        JPanel reporterCard = createParticipantBox(
                "NGƯỜI GỬI BÁO CÁO (REPORTER)",
                report.getReporterFullName() + " (@" + report.getReporterUsername() + ")",
                "ID người báo cáo: #" + report.getReporterUserId(),
                AppTheme.PRIMARY,
                AppTheme.PRIMARY_SOFT_BG
        );

        boolean currentlyLocked = reportedUser != null ? reportedUser.isLocked() : report.isReportedUserLocked();
        String reportedStatusText = currentlyLocked
                ? "Trạng thái TK hiện tại: Đã khóa (is_locked = TRUE)"
                : "Trạng thái TK hiện tại: Hoạt động (is_locked = FALSE)";
        JPanel reportedCard = createParticipantBox(
                "TÀI KHOẢN BỊ BÁO CÁO (REPORTED USER)",
                report.getReportedFullName() + " (@" + report.getReportedUsername() + ")",
                reportedStatusText,
                currentlyLocked ? AppTheme.DANGER : AppTheme.WARNING,
                currentlyLocked ? AppTheme.DANGER_BG : AppTheme.WARNING_BG
        );

        participantsGrid.add(reporterCard);
        participantsGrid.add(reportedCard);

        // 2. Reason & Resolution Card
        JPanel metaCard = new JPanel();
        metaCard.setLayout(new BoxLayout(metaCard, BoxLayout.Y_AXIS));
        metaCard.setBackground(AppTheme.PAGE_BG);
        metaCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        metaCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        metaCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

        JLabel reasonLabel = new JLabel("Lý do báo cáo: " + report.getReason());
        reasonLabel.setFont(AppTheme.FONT_BODY_BOLD);
        reasonLabel.setForeground(AppTheme.TEXT_PRIMARY);
        reasonLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String resolutionInfo = report.isPending()
                ? "Thông tin xử lý: Đang chờ quản trị viên xét duyệt (resolved_by = NULL)"
                : "Thông tin xử lý: Đã xử lý bởi @" + report.getResolvedByUsername()
                + " lúc " + report.getFormattedResolvedAt();
        JLabel resolutionLabel = new JLabel(resolutionInfo);
        resolutionLabel.setFont(AppTheme.FONT_CAPTION);
        resolutionLabel.setForeground(AppTheme.TEXT_SECONDARY);
        resolutionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        metaCard.add(reasonLabel);
        metaCard.add(Box.createVerticalStrut(4));
        metaCard.add(resolutionLabel);

        // 3. Synthetic Message Snapshot Box
        JPanel snapshotSection = new JPanel(new BorderLayout(0, 6));
        snapshotSection.setOpaque(false);
        snapshotSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel snapshotHeader = new JPanel(new BorderLayout());
        snapshotHeader.setOpaque(false);

        JLabel snapshotTitle = new JLabel("Bản chụp nội dung tin nhắn vi phạm (Synthetic Message Snapshot)");
        snapshotTitle.setFont(AppTheme.FONT_BODY_BOLD);
        snapshotTitle.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel snapshotMeta = new JLabel(
                "Tin nhắn #" + report.getDirectMessageId()
                        + "  •  Gửi lúc: " + report.getFormattedMessageSentAt()
                        + "  •  Từ @" + report.getReportedUsername() + " đến @" + report.getReporterUsername()
        );
        snapshotMeta.setFont(AppTheme.FONT_CAPTION);
        snapshotMeta.setForeground(AppTheme.TEXT_SECONDARY);

        snapshotHeader.add(snapshotTitle, BorderLayout.NORTH);
        snapshotHeader.add(snapshotMeta, BorderLayout.SOUTH);

        JTextArea snapshotArea = new JTextArea(
                "\"" + report.getMessageSnapshot() + "\"\n\n"
                        + "[Bản chụp lưu cố định trong cột spam_reports.message_snapshot — "
                        + "bảo toàn bằng chứng ngay cả khi người dùng ẩn tin nhắn cá nhân trong direct_message_hidden]"
        );
        snapshotArea.setFont(AppTheme.FONT_BODY);
        snapshotArea.setForeground(AppTheme.TEXT_PRIMARY);
        snapshotArea.setBackground(new Color(0xFF, 0xFB, 0xEB));
        snapshotArea.setLineWrap(true);
        snapshotArea.setWrapStyleWord(true);
        snapshotArea.setEditable(false);
        snapshotArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xFD, 0xE6, 0x8A), 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        snapshotSection.add(snapshotHeader, BorderLayout.NORTH);
        snapshotSection.add(snapshotArea, BorderLayout.CENTER);

        body.add(participantsGrid);
        body.add(Box.createVerticalStrut(AppTheme.SPACING_SM + 4));
        body.add(metaCard);
        body.add(Box.createVerticalStrut(AppTheme.SPACING_SM + 4));
        body.add(snapshotSection);
        return body;
    }

    private JPanel createParticipantBox(
            String roleHeading,
            String userLine,
            String subLine,
            Color accentColor,
            Color badgeBg
    ) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(AppTheme.PAGE_BG);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel heading = new JLabel(roleHeading);
        heading.setFont(AppTheme.FONT_CAPTION_BOLD);
        heading.setForeground(accentColor);
        heading.setOpaque(true);
        heading.setBackground(badgeBg);
        heading.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel name = new JLabel(userLine);
        name.setFont(AppTheme.FONT_BODY_BOLD);
        name.setForeground(AppTheme.TEXT_PRIMARY);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel(subLine);
        sub.setFont(AppTheme.FONT_CAPTION);
        sub.setForeground(AppTheme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        box.add(heading);
        box.add(Box.createVerticalStrut(6));
        box.add(name);
        box.add(Box.createVerticalStrut(2));
        box.add(sub);
        return box;
    }

    private JPanel createFooter(
            SpamReportRecord report,
            User reportedUser,
            Runnable onTriggerLockConfirmation
    ) {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AppTheme.PAGE_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER),
                BorderFactory.createEmptyBorder(
                        AppTheme.SPACING_SM + 4,
                        AppTheme.CONTENT_PADDING,
                        AppTheme.SPACING_SM + 4,
                        AppTheme.CONTENT_PADDING
                )
        ));

        JLabel noteLabel = new JLabel("Thao tác khóa TK chỉ mô phỏng trên bộ nhớ (mock-only).");
        noteLabel.setFont(AppTheme.FONT_CAPTION);
        noteLabel.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SPACING_SM, 0));
        actions.setOpaque(false);

        JButton closeBtn = new JButton("Đóng");
        closeBtn.setFont(AppTheme.FONT_BODY_BOLD);
        closeBtn.setForeground(AppTheme.TEXT_PRIMARY);
        closeBtn.setBackground(AppTheme.SURFACE_CARD);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.setPreferredSize(new Dimension(96, AppTheme.CONTROL_HEIGHT));
        closeBtn.addActionListener(e -> dispose());

        boolean currentlyLocked = reportedUser != null ? reportedUser.isLocked() : report.isReportedUserLocked();
        String lockActionText = currentlyLocked
                ? "Mở khóa TK @" + report.getReportedUsername()
                : "Khóa TK @" + report.getReportedUsername();
        Color accent = currentlyLocked ? AppTheme.SUCCESS : AppTheme.DANGER;

        JButton lockBtn = new JButton(lockActionText) {
            private boolean hovered = false;

            {
                setFont(AppTheme.FONT_BODY_BOLD);
                setForeground(AppTheme.SURFACE_CARD);
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setPreferredSize(new Dimension(210, AppTheme.CONTROL_HEIGHT));
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
                g2.setColor(hovered ? accent.darker() : accent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.CORNER_RADIUS, AppTheme.CORNER_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lockBtn.addActionListener(e -> {
            dispose();
            if (onTriggerLockConfirmation != null) {
                onTriggerLockConfirmation.run();
            }
        });

        actions.add(closeBtn);
        actions.add(lockBtn);

        footer.add(noteLabel, BorderLayout.WEST);
        footer.add(actions, BorderLayout.EAST);
        return footer;
    }
}
