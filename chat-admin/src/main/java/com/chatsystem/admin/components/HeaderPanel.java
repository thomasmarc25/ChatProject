package com.chatsystem.admin.components;

import com.chatsystem.admin.theme.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;

/**
 * Top 64px white header bar displaying the active section breadcrumb/title,
 * Version 1 mock-data indicator, and administrator profile badge aligned
 * with Member 1's User application header style ({@code ChatPanel} & {@code AvatarIcon}).
 */
public class HeaderPanel extends JPanel {

    private final JLabel sectionTitleLabel;
    private final JLabel breadcrumbLabel;

    public HeaderPanel() {
        super(new BorderLayout());
        setBackground(AppTheme.SURFACE_CARD);
        setPreferredSize(new Dimension(0, AppTheme.HEADER_HEIGHT));
        setMinimumSize(new Dimension(0, AppTheme.HEADER_HEIGHT));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER),
                BorderFactory.createEmptyBorder(0, AppTheme.CONTENT_PADDING, 0, AppTheme.CONTENT_PADDING)
        ));

        // Left: Breadcrumb and Active Section Title
        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);

        breadcrumbLabel = new JLabel("ChatSystem Admin  /  Quản trị  /  Quản lý người dùng");
        breadcrumbLabel.setFont(AppTheme.FONT_CAPTION);
        breadcrumbLabel.setForeground(AppTheme.TEXT_SECONDARY);
        breadcrumbLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sectionTitleLabel = new JLabel("Quản lý người dùng");
        sectionTitleLabel.setFont(AppTheme.FONT_SECTION_TITLE);
        sectionTitleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        sectionTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleStack.add(Box.createVerticalGlue());
        titleStack.add(breadcrumbLabel);
        titleStack.add(Box.createVerticalStrut(2));
        titleStack.add(sectionTitleLabel);
        titleStack.add(Box.createVerticalGlue());

        // Right: Version 1 Mock Badge + Administrator Avatar & Presence Indicator
        JPanel rightContainer = new JPanel(new GridBagLayout());
        rightContainer.setOpaque(false);
        rightContainer.add(createProfileIndicator());

        add(titleStack, BorderLayout.WEST);
        add(rightContainer, BorderLayout.EAST);
    }

    public void setSection(String sectionName) {
        setSection("Quản trị", sectionName);
    }

    public void setSection(String groupLabel, String sectionName) {
        sectionTitleLabel.setText(sectionName);
        breadcrumbLabel.setText("ChatSystem Admin  /  " + groupLabel + "  /  " + sectionName);
    }

    private JPanel createProfileIndicator() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.setOpaque(false);

        JLabel mockPreviewBadge = new JLabel("Phiên bản 1 · Dữ liệu mẫu");
        mockPreviewBadge.setFont(AppTheme.FONT_CAPTION_BOLD);
        mockPreviewBadge.setForeground(AppTheme.PRIMARY);
        mockPreviewBadge.setOpaque(true);
        mockPreviewBadge.setBackground(AppTheme.PRIMARY_SOFT_BG);
        mockPreviewBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.PRIMARY_BORDER, 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        mockPreviewBadge.setToolTipText(
                "Phiên bản 1: Ứng dụng giao diện quản trị sử dụng dữ liệu mẫu cố định trong bộ nhớ (In-memory mock fixtures)"
        );
        mockPreviewBadge.setAlignmentY(Component.CENTER_ALIGNMENT);

        AvatarCircle avatar = new AvatarCircle("A", 38);
        avatar.setAlignmentY(Component.CENTER_ALIGNMENT);

        JPanel textColumn = new JPanel();
        textColumn.setLayout(new BoxLayout(textColumn, BoxLayout.Y_AXIS));
        textColumn.setOpaque(false);
        textColumn.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel("Quản trị viên");
        nameLabel.setFont(AppTheme.FONT_BODY_BOLD);
        nameLabel.setForeground(AppTheme.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel statusLabel = new JLabel("● Đang hoạt động");
        statusLabel.setFont(AppTheme.FONT_CAPTION);
        statusLabel.setForeground(AppTheme.SUCCESS);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textColumn.add(Box.createVerticalGlue());
        textColumn.add(nameLabel);
        textColumn.add(Box.createVerticalStrut(1));
        textColumn.add(statusLabel);
        textColumn.add(Box.createVerticalGlue());

        container.add(mockPreviewBadge);
        container.add(Box.createHorizontalStrut(AppTheme.SPACING_MD));
        container.add(avatar);
        container.add(Box.createHorizontalStrut(AppTheme.SPACING_SM + 2));
        container.add(textColumn);

        return container;
    }

    /**
     * Circular avatar icon matching {@code com.chatsystem.user.AvatarIcon}.
     */
    private static final class AvatarCircle extends JPanel {

        private final int size;

        private AvatarCircle(String initial, int size) {
            super(new BorderLayout());
            this.size = size;
            setOpaque(false);
            Dimension dim = new Dimension(size, size);
            setPreferredSize(dim);
            setMinimumSize(dim);
            setMaximumSize(dim);

            JLabel label = new JLabel(initial, SwingConstants.CENTER);
            label.setFont(AppTheme.FONT_BODY_BOLD);
            label.setForeground(AppTheme.SURFACE_CARD);
            add(label, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(AppTheme.PRIMARY);
            g2.fillOval(0, 0, size - 1, size - 1);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
