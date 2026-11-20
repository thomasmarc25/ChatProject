package com.chatsystem.admin.components;

import com.chatsystem.admin.theme.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Reusable summary statistic card for the User Management dashboard.
 */
public class StatCard extends JPanel {

    private final Color accentColor;
    private final Color badgeBackground;
    private final JLabel valueLabel;

    public StatCard(String title, int count, String subtitle, Color accentColor, Color badgeBackground) {
        super(new BorderLayout(AppTheme.SPACING_SM, 0));
        this.accentColor = accentColor;
        this.badgeBackground = badgeBackground;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(
                AppTheme.SPACING_MD - 2,
                AppTheme.SPACING_MD + 4,
                AppTheme.SPACING_MD - 2,
                AppTheme.SPACING_MD
        ));

        JPanel leftColumn = new JPanel();
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(AppTheme.FONT_CAPTION_BOLD);
        titleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel = new JLabel(String.valueOf(count));
        valueLabel.setFont(AppTheme.FONT_PAGE_TITLE);
        valueLabel.setForeground(AppTheme.TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(AppTheme.FONT_CAPTION);
        subtitleLabel.setForeground(accentColor);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftColumn.add(titleLabel);
        leftColumn.add(Box.createVerticalStrut(4));
        leftColumn.add(valueLabel);
        leftColumn.add(Box.createVerticalStrut(2));
        leftColumn.add(subtitleLabel);

        JPanel badgeWrapper = new JPanel(new BorderLayout());
        badgeWrapper.setOpaque(false);
        AccentIndicator indicator = new AccentIndicator(accentColor, this.badgeBackground);
        badgeWrapper.add(indicator, BorderLayout.NORTH);

        add(leftColumn, BorderLayout.CENTER);
        add(badgeWrapper, BorderLayout.EAST);
    }

    public void setCount(int count) {
        valueLabel.setText(String.valueOf(count));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int arc = AppTheme.CORNER_RADIUS;

        // Card surface
        g2.setColor(AppTheme.SURFACE_CARD);
        g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

        // Left accent stripe
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 0, 5, h - 1, arc, arc);
        g2.fillRect(3, 0, 2, h - 1);

        // Card border
        g2.setColor(AppTheme.BORDER);
        g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }

    private static final class AccentIndicator extends JPanel {

        private final Color dotColor;
        private final Color bgColor;

        private AccentIndicator(Color dotColor, Color bgColor) {
            super(new BorderLayout());
            this.dotColor = dotColor;
            this.bgColor = bgColor;
            setOpaque(false);
            Dimension dim = new Dimension(32, 32);
            setPreferredSize(dim);
            setMinimumSize(dim);
            setMaximumSize(dim);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.CORNER_RADIUS, AppTheme.CORNER_RADIUS);
            int dotSize = 10;
            g2.setColor(dotColor);
            g2.fillOval((getWidth() - dotSize) / 2, (getHeight() - dotSize) / 2, dotSize, dotSize);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
