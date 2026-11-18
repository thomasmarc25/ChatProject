package com.chatsystem.admin.ui;

import com.chatsystem.admin.components.MonthlyBarChartPanel;
import com.chatsystem.admin.components.StatCard;
import com.chatsystem.admin.data.MockUserRepository;
import com.chatsystem.admin.theme.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/**
 * Annual Registration Chart screen panel (Requirement 6 / Step G - B).
 * Provides a year selector ({@code 2026}, {@code 2025}, {@code 2024}), 4 summary stat cards,
 * a 12-month bar chart ({@link MonthlyBarChartPanel}) with visible axes, month labels,
 * numeric scale, zero-count months (Months 7..12 in 2026), and an Empty-Year State (2024/2025).
 */
public class RegistrationChartPanel extends JPanel {

    private static final Integer[] YEAR_OPTIONS = {2026, 2025, 2024};

    private final MockUserRepository userRepository;
    private final JComboBox<Integer> yearCombo;
    private final MonthlyBarChartPanel barChartPanel;
    private final JLabel[] monthSummaryValueLabels = new JLabel[12];

    private StatCard yearTotalCard;
    private StatCard peakMonthCard;
    private StatCard activeMonthsCard;
    private StatCard zeroMonthsCard;

    public RegistrationChartPanel(MockUserRepository userRepository) {
        super(new BorderLayout(0, AppTheme.SPACING_MD));
        this.userRepository = userRepository;
        this.yearCombo = new JComboBox<>(YEAR_OPTIONS);

        int initialYear = 2026;
        int[] initialCounts = userRepository.getMonthlyRegistrationCounts(initialYear);
        this.barChartPanel = new MonthlyBarChartPanel(
                "Biểu đồ đăng ký mới 12 tháng — Năm " + initialYear,
                "Nguồn dữ liệu: COUNT(users.id) WHERE role = 'USER' theo từng tháng (1..12) tại múi giờ Asia/Ho_Chi_Minh (QUERY 7)",
                "Số lượng đăng ký mới",
                "Trục hoành (X): 12 tháng trong năm (Tháng 1 – Tháng 12 • Múi giờ Asia/Ho_Chi_Minh)",
                AppTheme.PRIMARY,
                AppTheme.PRIMARY_HOVER,
                AppTheme.PRIMARY_SOFT_BG,
                initialYear,
                initialCounts
        );

        setBackground(AppTheme.PAGE_BG);
        setBorder(BorderFactory.createEmptyBorder(
                AppTheme.CONTENT_PADDING - 4,
                AppTheme.CONTENT_PADDING,
                AppTheme.CONTENT_PADDING - 4,
                AppTheme.CONTENT_PADDING
        ));

        JPanel topSection = new JPanel(new BorderLayout(0, AppTheme.SPACING_MD));
        topSection.setOpaque(false);
        topSection.add(createPageIntroBar(), BorderLayout.NORTH);
        topSection.add(createSummaryCardsRow(), BorderLayout.CENTER);

        JPanel centerSection = new JPanel(new BorderLayout(0, AppTheme.SPACING_SM + 4));
        centerSection.setOpaque(false);
        centerSection.add(barChartPanel, BorderLayout.CENTER);
        centerSection.add(createMonthlyNumericBreakdownStrip(), BorderLayout.SOUTH);

        add(topSection, BorderLayout.NORTH);
        add(centerSection, BorderLayout.CENTER);

        this.userRepository.addDataChangeListener(this::refreshChartForSelectedYear);
        refreshChartForSelectedYear();
    }

    public void selectYear(int year) {
        yearCombo.setSelectedItem(year);
        refreshChartForSelectedYear();
    }

    private JPanel createPageIntroBar() {
        JPanel introPanel = new JPanel(new BorderLayout(AppTheme.SPACING_MD, 0));
        introPanel.setOpaque(false);

        JPanel textGroup = new JPanel();
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));
        textGroup.setOpaque(false);

        JLabel titleLabel = new JLabel("Biểu đồ đăng ký mới theo năm");
        titleLabel.setFont(AppTheme.FONT_PAGE_TITLE);
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(
                "Thống kê số lượng tài khoản đăng ký mới qua 12 tháng (gồm tháng bằng 0 & năm rỗng)"
        );
        subtitleLabel.setFont(AppTheme.FONT_BODY);
        subtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textGroup.add(titleLabel);
        textGroup.add(Box.createVerticalStrut(4));
        textGroup.add(subtitleLabel);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SPACING_SM, 4));
        controls.setOpaque(false);

        JLabel yearLabel = new JLabel("Năm:");
        yearLabel.setFont(AppTheme.FONT_BODY_BOLD);
        yearLabel.setForeground(AppTheme.TEXT_PRIMARY);

        yearCombo.setFont(AppTheme.FONT_BODY_BOLD);
        yearCombo.setPreferredSize(new Dimension(92, AppTheme.CONTROL_HEIGHT));
        yearCombo.addActionListener(e -> refreshChartForSelectedYear());

        JButton year2026Btn = createSecondaryButton("Năm 2026", 98);
        year2026Btn.addActionListener(e -> selectYear(2026));

        JButton emptyYearBtn = createSecondaryButton("Năm 2024 (Rỗng)", 136);
        emptyYearBtn.setToolTipText("Chuyển sang năm 2024 để kiểm tra trạng thái năm không có dữ liệu (Empty-Year State)");
        emptyYearBtn.addActionListener(e -> selectYear(2024));

        controls.add(yearLabel);
        controls.add(yearCombo);
        controls.add(year2026Btn);
        controls.add(emptyYearBtn);

        introPanel.add(textGroup, BorderLayout.CENTER);
        introPanel.add(controls, BorderLayout.EAST);
        return introPanel;
    }

    private JPanel createSummaryCardsRow() {
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, AppTheme.SPACING_MD, 0));
        cardsGrid.setOpaque(false);

        yearTotalCard = new StatCard(
                "Tổng đăng ký trong năm",
                25,
                "Tổng cả 12 tháng",
                AppTheme.PRIMARY,
                AppTheme.PRIMARY_SOFT_BG
        );
        peakMonthCard = new StatCard(
                "Mức cao nhất / tháng",
                5,
                "Số đăng ký cao nhất",
                AppTheme.SUCCESS,
                AppTheme.SUCCESS_BG
        );
        activeMonthsCard = new StatCard(
                "Số tháng có đăng ký",
                6,
                "Tháng có giá trị > 0",
                AppTheme.TEXT_SECONDARY,
                AppTheme.NEUTRAL_BG
        );
        zeroMonthsCard = new StatCard(
                "Số tháng bằng 0",
                6,
                "Tháng không phát sinh (0)",
                AppTheme.WARNING,
                AppTheme.WARNING_BG
        );

        cardsGrid.add(yearTotalCard);
        cardsGrid.add(peakMonthCard);
        cardsGrid.add(activeMonthsCard);
        cardsGrid.add(zeroMonthsCard);
        return cardsGrid;
    }

    private JPanel createMonthlyNumericBreakdownStrip() {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(AppTheme.SURFACE_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, AppTheme.SPACING_MD, 10, AppTheme.SPACING_MD)
        ));

        JLabel stripHeader = new JLabel(
                "Bảng đối chiếu số liệu chi tiết 12 tháng (Khớp 100% với kết quả QUERY 7 trong script/03_admin_queries.sql):"
        );
        stripHeader.setFont(AppTheme.FONT_CAPTION_BOLD);
        stripHeader.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel grid = new JPanel(new GridLayout(1, 12, 6, 0));
        grid.setOpaque(false);

        for (int i = 0; i < 12; i++) {
            JPanel cell = new JPanel(new BorderLayout());
            cell.setBackground(AppTheme.PAGE_BG);
            cell.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                    BorderFactory.createEmptyBorder(4, 4, 4, 4)
            ));

            JLabel mLbl = new JLabel("T" + (i + 1), SwingConstants.CENTER);
            mLbl.setFont(AppTheme.FONT_CAPTION);
            mLbl.setForeground(AppTheme.TEXT_SECONDARY);

            JLabel vLbl = new JLabel("0", SwingConstants.CENTER);
            vLbl.setFont(AppTheme.FONT_BODY_BOLD);
            vLbl.setForeground(AppTheme.TEXT_PRIMARY);
            monthSummaryValueLabels[i] = vLbl;

            cell.add(mLbl, BorderLayout.NORTH);
            cell.add(vLbl, BorderLayout.CENTER);
            grid.add(cell);
        }

        card.add(stripHeader, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private void refreshChartForSelectedYear() {
        Integer selected = (Integer) yearCombo.getSelectedItem();
        int year = selected != null ? selected : 2026;
        int[] counts = userRepository.getMonthlyRegistrationCounts(year);

        int total = 0;
        int peak = 0;
        int nonZeroMonths = 0;
        int zeroMonths = 0;
        for (int i = 0; i < 12; i++) {
            int c = counts[i];
            total += c;
            if (c > peak) {
                peak = c;
            }
            if (c > 0) {
                nonZeroMonths++;
            } else {
                zeroMonths++;
            }
            if (monthSummaryValueLabels[i] != null) {
                monthSummaryValueLabels[i].setText(String.valueOf(c));
                monthSummaryValueLabels[i].setForeground(c > 0 ? AppTheme.PRIMARY : AppTheme.TEXT_SECONDARY);
            }
        }

        if (yearTotalCard != null) {
            yearTotalCard.setCount(total);
            peakMonthCard.setCount(peak);
            activeMonthsCard.setCount(nonZeroMonths);
            zeroMonthsCard.setCount(zeroMonths);
        }

        String title = "Biểu đồ đăng ký mới 12 tháng — Năm " + year;
        String subtitle = total == 0
                ? "Năm " + year + ": Trạng thái năm rỗng (Empty-Year State) — cả 12 tháng đều có 0 lượt đăng ký mới"
                : "Năm " + year + ": Tổng cộng " + total + " tài khoản đăng ký mới ("
                + nonZeroMonths + " tháng có phát sinh, " + zeroMonths + " tháng bằng 0 • QUERY 7)";
        barChartPanel.setChartData(year, counts, title, subtitle);
    }

    private JButton createSecondaryButton(String text, int width) {
        JButton btn = new JButton(text);
        btn.setFont(AppTheme.FONT_CAPTION_BOLD);
        btn.setForeground(AppTheme.TEXT_PRIMARY);
        btn.setBackground(AppTheme.SURFACE_CARD);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(0, 6, 0, 6));
        btn.setPreferredSize(new Dimension(width, AppTheme.CONTROL_HEIGHT));
        btn.setMinimumSize(new Dimension(width, AppTheme.CONTROL_HEIGHT));
        return btn;
    }
}
