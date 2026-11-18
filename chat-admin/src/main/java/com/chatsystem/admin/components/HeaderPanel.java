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
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;

/**
 * Top 64px white header bar displaying the active section title, breadcrumb trail,
 * and administrator avatar/profile indicator.
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

        // Left: Breadcrumb and Section Title
        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);

        breadcrumbLabel = new JLabel("Trang chủ / Quản lý người dùng");
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

        // Right: Administrator Avatar / Profile Indicator
        JPanel rightContainer = new JPanel(new GridBagLayout());
        rightContainer.setOpaque(false);

        JPanel profileIndicator = createProfileIndicator();
        rightContainer.add(profileIndicator);

        add(titleStack, BorderLayout.WEST);
        add(rightContainer, BorderLayout.EAST);
    }

    public void setSection(String sectionName) {
        sectionTitleLabel.setText(sectionName);
        breadcrumbLabel.setText("Trang chủ / " + sectionName);
    }

    private JPanel createProfileIndicator() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.setOpaque(false);

        JPanel textColumn = new JPanel();
        textColumn.setLayout(new BoxLayout(textColumn, BoxLayout.Y_AXIS));
        textColumn.setOpaque(false);
        textColumn.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel("Quản trị viên");
        nameLabel.setFont(AppTheme.FONT_BODY_BOLD);
        nameLabel.setForeground(AppTheme.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel roleLabel = new JLabel("Administrator");
        roleLabel.setFont(AppTheme.FONT_CAPTION);
        roleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        roleLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        textColumn.add(Box.createVerticalGlue());
        textColumn.add(nameLabel);
        textColumn.add(Box.createVerticalStrut(1));
        textColumn.add(roleLabel);
        textColumn.add(Box.createVerticalGlue());

        AvatarIndicator avatar = new AvatarIndicator("AD", 36);
        avatar.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel mockPreviewBadge = new JLabel("Version 1 • Mock Data Preview");
        mockPreviewBadge.setFont(AppTheme.FONT_CAPTION_BOLD);
        mockPreviewBadge.setForeground(AppTheme.TEXT_SECONDARY);
        mockPreviewBadge.setOpaque(true);
        mockPreviewBadge.setBackground(AppTheme.NEUTRAL_BG);
        mockPreviewBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        mockPreviewBadge.setToolTipText("Phiên bản 1: Ứng dụng giao diện quản trị sử dụng dữ liệu mẫu cố định trong bộ nhớ (In-memory mock fixtures)");
        mockPreviewBadge.setAlignmentY(Component.CENTER_ALIGNMENT);

        container.add(mockPreviewBadge);
        container.add(Box.createHorizontalStrut(AppTheme.SPACING_MD));
        container.add(textColumn);
        container.add(Box.createHorizontalStrut(AppTheme.SPACING_SM + 4));
        container.add(avatar);

        return container;
    }

    /**
     * Circular avatar indicator with online status badge drawn via Java2D.
     */
    private static final class AvatarIndicator extends JPanel {

        private final int size;

        private AvatarIndicator(String initials, int size) {
            super(new BorderLayout());
            this.size = size;
            setOpaque(false);
            Dimension dim = new Dimension(size, size);
            setPreferredSize(dim);
            setMinimumSize(dim);
            setMaximumSize(dim);

            JLabel initialsLabel = new JLabel(initials, SwingConstants.CENTER);
            initialsLabel.setFont(AppTheme.FONT_CAPTION_BOLD);
            initialsLabel.setForeground(AppTheme.SURFACE_CARD);
            add(initialsLabel, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Main avatar circle
            g2.setColor(AppTheme.PRIMARY);
            g2.fillOval(0, 0, size - 1, size - 1);

            // Online indicator dot in bottom-right corner
            int dotSize = 10;
            int dotX = size - dotSize;
            int dotY = size - dotSize;
            g2.setColor(AppTheme.SURFACE_CARD);
            g2.fillOval(dotX - 2, dotY - 2, dotSize + 3, dotSize + 3);
            g2.setColor(AppTheme.SUCCESS);
            g2.fillOval(dotX, dotY, dotSize - 1, dotSize - 1);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
