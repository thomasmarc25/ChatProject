package com.chatsystem.admin.components;

import com.chatsystem.admin.theme.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Arrays;

/**
 * Custom anti-aliased Java2D 12-month bar chart component for the
 * Annual Registration Chart (Requirement 6 / Step G - B) and
 * Annual Activity Chart (Requirement 9 / Step G - E).
 * Renders visible horizontal and vertical axes, month labels (Tháng 1..12),
 * integer numeric scale, value badges above all 12 bars (including zero-count months),
 * and an explicit Empty-Year State banner when all 12 months equal zero.
 */
public class MonthlyBarChartPanel extends JPanel {

    private static final String[] MONTH_LABELS = {
            "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4",
            "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8",
            "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
    };

    private String chartTitle;
    private String chartSubtitle;
    private String yAxisUnitLabel;
    private String xAxisTitleLabel;
    private Color barPrimaryColor;
    private Color barHoverColor;
    private Color barSoftBgColor;
    private int selectedYear;
    private int[] monthlyValues = new int[12];
    private int hoveredMonthIndex = -1;

    public MonthlyBarChartPanel(
            String chartTitle,
            String chartSubtitle,
            String yAxisUnitLabel,
            String xAxisTitleLabel,
            Color barPrimaryColor,
            Color barHoverColor,
            Color barSoftBgColor,
            int initialYear,
            int[] initialValues
    ) {
        this.chartTitle = chartTitle;
        this.chartSubtitle = chartSubtitle;
        this.yAxisUnitLabel = yAxisUnitLabel;
        this.xAxisTitleLabel = xAxisTitleLabel;
        this.barPrimaryColor = barPrimaryColor;
        this.barHoverColor = barHoverColor;
        this.barSoftBgColor = barSoftBgColor;
        this.selectedYear = initialYear;
        setChartData(initialYear, initialValues, chartTitle, chartSubtitle);

        setBackground(AppTheme.SURFACE_CARD);
        setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1, true));
        setPreferredSize(new Dimension(760, 380));
        setMinimumSize(new Dimension(600, 300));

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHoveredBar(e.getX(), e.getY());
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (hoveredMonthIndex != -1) {
                    hoveredMonthIndex = -1;
                    setToolTipText(null);
                    repaint();
                }
            }
        });
    }

    public void setChartData(int year, int[] values, String newTitle, String newSubtitle) {
        this.selectedYear = year;
        this.monthlyValues = new int[12];
        if (values != null) {
            System.arraycopy(values, 0, this.monthlyValues, 0, Math.min(12, values.length));
        }
        if (newTitle != null) {
            this.chartTitle = newTitle;
        }
        if (newSubtitle != null) {
            this.chartSubtitle = newSubtitle;
        }
        repaint();
    }

    public int[] getMonthlyValues() {
        return Arrays.copyOf(monthlyValues, 12);
    }

    public int getSelectedYear() {
        return selectedYear;
    }

    public boolean isEmptyYear() {
        for (int v : monthlyValues) {
            if (v > 0) {
                return false;
            }
        }
        return true;
    }

    private void updateHoveredBar(int mx, int my) {
        int w = getWidth();
        int h = getHeight();
        int leftMargin = 68;
        int rightMargin = 28;
        int topMargin = 88;
        int bottomMargin = 64;
        int plotW = Math.max(120, w - leftMargin - rightMargin);
        int plotH = Math.max(80, h - topMargin - bottomMargin);

        if (mx < leftMargin || mx > leftMargin + plotW || my < topMargin - 20 || my > topMargin + plotH + 24) {
            if (hoveredMonthIndex != -1) {
                hoveredMonthIndex = -1;
                setToolTipText(null);
                repaint();
            }
            return;
        }

        double slotWidth = plotW / 12.0;
        int index = (int) ((mx - leftMargin) / slotWidth);
        if (index >= 0 && index < 12) {
            if (hoveredMonthIndex != index) {
                hoveredMonthIndex = index;
                setToolTipText(
                        MONTH_LABELS[index] + " năm " + selectedYear + ": "
                                + monthlyValues[index] + " (" + yAxisUnitLabel + ")"
                );
                repaint();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w = getWidth();
        int h = getHeight();

        // 1. Header title & subtitle inside chart card
        int padX = 24;
        g2.setFont(AppTheme.FONT_SECTION_TITLE);
        g2.setColor(AppTheme.TEXT_PRIMARY);
        g2.drawString(chartTitle, padX, 32);

        g2.setFont(AppTheme.FONT_CAPTION);
        g2.setColor(AppTheme.TEXT_SECONDARY);
        g2.drawString(chartSubtitle, padX, 52);

        // Y-axis unit badge on top-right of chart header
        String yUnitBadge = "Trục tung (Y): " + yAxisUnitLabel;
        g2.setFont(AppTheme.FONT_CAPTION_BOLD);
        FontMetrics badgeFm = g2.getFontMetrics();
        int badgeW = badgeFm.stringWidth(yUnitBadge) + 20;
        int badgeH = 24;
        int badgeX = w - padX - badgeW;
        int badgeY = 16;
        g2.setColor(barSoftBgColor);
        g2.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 10, 10);
        g2.setColor(barPrimaryColor);
        g2.drawString(yUnitBadge, badgeX + 10, badgeY + ((badgeH - badgeFm.getHeight()) / 2) + badgeFm.getAscent());

        // 2. Plot geometry
        int leftMargin = 68;
        int rightMargin = 28;
        int topMargin = 86;
        int bottomMargin = 64;
        int plotW = Math.max(120, w - leftMargin - rightMargin);
        int plotH = Math.max(80, h - topMargin - bottomMargin);
        int originX = leftMargin;
        int originY = topMargin + plotH;

        int maxDataVal = 0;
        for (int v : monthlyValues) {
            if (v > maxDataVal) {
                maxDataVal = v;
            }
        }
        // Choose clean integer scale maximum (at least 6)
        int yMax = Math.max(6, ((maxDataVal + 1)));
        int tickStep = yMax <= 8 ? 1 : (int) Math.ceil(yMax / 6.0);
        yMax = tickStep * (int) Math.ceil((double) yMax / tickStep);

        // 3. Horizontal gridlines & Y-axis numeric scale
        Stroke oldStroke = g2.getStroke();
        Stroke dashedGrid = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10.0f, new float[]{4.0f, 4.0f}, 0.0f);

        g2.setFont(AppTheme.FONT_CAPTION_BOLD);
        FontMetrics yFm = g2.getFontMetrics();

        for (int tick = 0; tick <= yMax; tick += tickStep) {
            double ratio = (double) tick / yMax;
            int y = originY - (int) Math.round(ratio * plotH);

            if (tick > 0) {
                g2.setStroke(dashedGrid);
                g2.setColor(AppTheme.BORDER);
                g2.drawLine(originX, y, originX + plotW, y);
            }

            g2.setStroke(oldStroke);
            g2.setColor(AppTheme.TEXT_SECONDARY);
            String tickStr = String.valueOf(tick);
            int strW = yFm.stringWidth(tickStr);
            g2.drawString(tickStr, originX - 12 - strW, y + (yFm.getAscent() / 2) - 1);
            // Small tick mark on Y-axis
            g2.setColor(AppTheme.BORDER);
            g2.drawLine(originX - 4, y, originX, y);
        }

        // 4. Draw 12 monthly bars, value badges, and X-axis month labels
        double slotWidth = plotW / 12.0;
        int barWidth = Math.max(22, Math.min(48, (int) (slotWidth * 0.54)));

        for (int i = 0; i < 12; i++) {
            int val = monthlyValues[i];
            int slotCenterX = originX + (int) Math.round((i + 0.5) * slotWidth);
            int barX = slotCenterX - (barWidth / 2);

            // Subtle column hover highlight
            if (i == hoveredMonthIndex) {
                g2.setColor(new Color(0xF8, 0xFA, 0xFC));
                int slotLeft = originX + (int) Math.round(i * slotWidth);
                g2.fillRect(slotLeft + 2, topMargin, (int) slotWidth - 4, plotH);
            }

            if (val > 0) {
                int barH = Math.max(6, (int) Math.round(((double) val / yMax) * plotH));
                int barY = originY - barH;

                Color fill = (i == hoveredMonthIndex) ? barHoverColor : barPrimaryColor;
                g2.setColor(fill);
                g2.fillRoundRect(barX, barY, barWidth, barH, 8, 8);
                // Square off the bottom corners touching the X-axis
                if (barH > 6) {
                    g2.fillRect(barX, originY - 5, barWidth, 5);
                }

                // Value badge above non-zero bar
                String valText = String.valueOf(val);
                g2.setFont(AppTheme.FONT_BODY_BOLD);
                FontMetrics vFm = g2.getFontMetrics();
                int pillW = Math.max(26, vFm.stringWidth(valText) + 14);
                int pillH = 22;
                int pillX = slotCenterX - (pillW / 2);
                int pillY = Math.max(topMargin - 20, barY - pillH - 5);

                g2.setColor(barSoftBgColor);
                g2.fillRoundRect(pillX, pillY, pillW, pillH, 10, 10);
                g2.setColor(barPrimaryColor);
                int textX = slotCenterX - (vFm.stringWidth(valText) / 2);
                int textY = pillY + ((pillH - vFm.getHeight()) / 2) + vFm.getAscent();
                g2.drawString(valText, textX, textY);
            } else {
                // Zero-count month indicator on baseline + "0" label
                g2.setColor(new Color(0xD1, 0xD5, 0xDB));
                g2.fillRoundRect(barX, originY - 4, barWidth, 4, 3, 3);

                String zeroText = "0";
                g2.setFont(AppTheme.FONT_CAPTION_BOLD);
                FontMetrics zFm = g2.getFontMetrics();
                int zX = slotCenterX - (zFm.stringWidth(zeroText) / 2);
                g2.setColor(AppTheme.TEXT_SECONDARY);
                g2.drawString(zeroText, zX, originY - 9);
            }

            // Month label below X-axis
            g2.setFont(i == hoveredMonthIndex ? AppTheme.FONT_CAPTION_BOLD : AppTheme.FONT_CAPTION);
            FontMetrics mFm = g2.getFontMetrics();
            String mLabel = MONTH_LABELS[i];
            int mX = slotCenterX - (mFm.stringWidth(mLabel) / 2);
            g2.setColor(i == hoveredMonthIndex ? AppTheme.TEXT_PRIMARY : AppTheme.TEXT_SECONDARY);
            g2.drawString(mLabel, mX, originY + 22);
        }

        // 5. Solid Y-axis and X-axis lines
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(new Color(0x9C, 0xA3, 0xAF));
        g2.drawLine(originX, topMargin - 6, originX, originY);
        g2.drawLine(originX, originY, originX + plotW, originY);
        g2.setStroke(oldStroke);

        // 6. X-axis title caption at the bottom
        g2.setFont(AppTheme.FONT_CAPTION_BOLD);
        FontMetrics xTitleFm = g2.getFontMetrics();
        int xTitleX = originX + (plotW - xTitleFm.stringWidth(xAxisTitleLabel)) / 2;
        g2.setColor(AppTheme.TEXT_SECONDARY);
        g2.drawString(xAxisTitleLabel, Math.max(originX, xTitleX), originY + 46);

        // 7. Empty-year state callout overlay when all 12 months are 0
        if (isEmptyYear()) {
            String emptyHeadline = "TRẠNG THÁI NĂM RỖNG (EMPTY-YEAR STATE) — NĂM " + selectedYear;
            String emptySubline = "Tất cả 12 tháng (Tháng 1 – Tháng 12) trong năm " + selectedYear
                    + " đều có giá trị bằng 0.";

            g2.setFont(AppTheme.FONT_BODY_BOLD);
            FontMetrics hFm = g2.getFontMetrics();
            g2.setFont(AppTheme.FONT_CAPTION);
            FontMetrics sFm = g2.getFontMetrics();

            int boxW = Math.max(hFm.stringWidth(emptyHeadline), sFm.stringWidth(emptySubline)) + 44;
            int boxH = 66;
            int boxX = originX + (plotW - boxW) / 2;
            int boxY = topMargin + (plotH - boxH) / 2 - 8;

            g2.setColor(new Color(255, 251, 235, 242));
            g2.fillRoundRect(boxX, boxY, boxW, boxH, 12, 12);
            g2.setColor(AppTheme.WARNING);
            g2.drawRoundRect(boxX, boxY, boxW - 1, boxH - 1, 12, 12);

            g2.setFont(AppTheme.FONT_BODY_BOLD);
            g2.setColor(AppTheme.WARNING);
            int hX = boxX + (boxW - hFm.stringWidth(emptyHeadline)) / 2;
            g2.drawString(emptyHeadline, hX, boxY + 26);

            g2.setFont(AppTheme.FONT_CAPTION);
            g2.setColor(AppTheme.TEXT_PRIMARY);
            int sX = boxX + (boxW - sFm.stringWidth(emptySubline)) / 2;
            g2.drawString(emptySubline, sX, boxY + 48);
        }

        g2.dispose();
    }
}
