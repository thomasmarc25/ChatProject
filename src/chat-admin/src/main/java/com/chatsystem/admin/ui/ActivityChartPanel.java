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
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/**
 * Annual Activity Chart screen panel (Requirement 9 / Step G - E).
 * Displays a year selector ({@code 2026}, {@code 2025}, {@code 2024}), a clear definition callout
 * explaining that the vertical axis plots monthly DISTINCT users who opened the User application
 * ({@code COUNT(DISTINCT aoe.user_id)} from {@code app_open_events}, never login count, message count,
 * or total app-open count), 4 summary stat cards, and a 12-month custom Swing bar chart
 * including zero-count Month 8 in 2026 and an Empty-Year State in 2024.
 */
public class ActivityChartPanel extends JPanel {

    private static final Integer[] YEAR_OPTIONS = {2026, 2025, 2024};

    private final MockUserRepository userRepository;
    private final JComboBox<Integer> yearCombo;
    private final MonthlyBarChartPanel barChartPanel;
    private final JLabel[] monthSummaryValueLabels = new JLabel[12];

    private StatCard peakMonthlyUsersCard;
    private StatCard activeMonthsCard;
    private StatCard zeroMonthsCard;
    private StatCard sumUserMonthsCard;

    public ActivityChartPanel(MockUserRepository userRepository) {
        super(new BorderLayout(0, AppTheme.SPACING_MD - 2));
        this.userRepository = userRepository;
        this.yearCombo = new JComboBox<>(YEAR_OPTIONS);

        int initialYear = 2026;
        int[] initialCounts = userRepository.getMonthlyDistinctAppOpeningUsers(initialYear);
        this.barChartPanel = new MonthlyBarChartPanel(
                "Biểu đồ hoạt động 12 tháng — Năm " + initialYear,
                "Chỉ số: COUNT(DISTINCT aoe.user_id) từ bảng app_open_events theo từng tháng (1..12) tại Asia/Ho_Chi_Minh (QUERY 10)",
                "Người dùng duy nhất mở app",
                "Trục hoành (X): 12 tháng trong năm (Tháng 1 – Tháng 12 • Múi giờ Asia/Ho_Chi_Minh)",
                AppTheme.SUCCESS,
                AppTheme.SUCCESS_HOVER,
                AppTheme.SUCCESS_BG,
                initialYear,
                initialCounts
        );

        setBackground(AppTheme.PAGE_BG);
        setBorder(BorderFactory.createEmptyBorder(
                AppTheme.CONTENT_PADDING - 6,
                AppTheme.CONTENT_PADDING,
                AppTheme.CONTENT_PADDING - 6,
                AppTheme.CONTENT_PADDING
        ));

        JPanel topSection = new JPanel(new BorderLayout(0, AppTheme.SPACING_SM + 2));
        topSection.setOpaque(false);
        topSection.add(createPageIntroAndDefinitionBanner(), BorderLayout.NORTH);
        topSection.add(createSummaryCardsRow(), BorderLayout.CENTER);

        JPanel centerSection = new JPanel(new BorderLayout(0, AppTheme.SPACING_SM + 2));
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

    private JPanel createPageIntroAndDefinitionBanner() {
        JPanel wrapper = new JPanel(new BorderLayout(0, AppTheme.SPACING_SM));
        wrapper.setOpaque(false);

        JPanel introPanel = new JPanel(new BorderLayout(AppTheme.SPACING_MD, 0));
        introPanel.setOpaque(false);

        JPanel textGroup = new JPanel();
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));
        textGroup.setOpaque(false);

        JLabel titleLabel = new JLabel("Biểu đồ hoạt động theo năm");
        titleLabel.setFont(AppTheme.FONT_PAGE_TITLE);
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(
                "Thống kê số người dùng DUY NHẤT mở ứng dụng (COUNT(DISTINCT user_id)) qua 12 tháng"
        );
        subtitleLabel.setFont(AppTheme.FONT_BODY);
        subtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textGroup.add(titleLabel);
        textGroup.add(Box.createVerticalStrut(2));
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
        emptyYearBtn.setToolTipText("Chuyển sang năm 2024 để kiểm tra trạng thái năm không có hoạt động (Empty-Year State)");
        emptyYearBtn.addActionListener(e -> selectYear(2024));

        controls.add(yearLabel);
        controls.add(yearCombo);
        controls.add(year2026Btn);
        controls.add(emptyYearBtn);

        introPanel.add(textGroup, BorderLayout.CENTER);
        introPanel.add(controls, BorderLayout.EAST);

        JPanel defBanner = new JPanel(new BorderLayout());
        defBanner.setBackground(AppTheme.SUCCESS_BG);
        defBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.SUCCESS),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));

        JLabel defLabel = new JLabel(
                "Quy tắc (QUERY 10): Đếm người dùng DUY NHẤT mở app mỗi tháng (COUNT(DISTINCT aoe.user_id)) — "
                        + "KHÔNG dùng số lượt đăng nhập, tin nhắn hay tổng lượt mở app (VD Tháng 3/2026: 8 lượt mở = 5 người)."
        );
        defLabel.setFont(AppTheme.FONT_CAPTION);
        defLabel.setForeground(AppTheme.TEXT_PRIMARY);
        defBanner.add(defLabel, BorderLayout.CENTER);

        wrapper.add(introPanel, BorderLayout.NORTH);
        wrapper.add(defBanner, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createSummaryCardsRow() {
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, AppTheme.SPACING_MD, 0));
        cardsGrid.setOpaque(false);

        peakMonthlyUsersCard = new StatCard(
                "Đỉnh người dùng / tháng",
                5,
                "DISTINCT user_id cao nhất",
                AppTheme.SUCCESS,
                AppTheme.SUCCESS_BG
        );
        activeMonthsCard = new StatCard(
                "Số tháng có hoạt động",
                11,
                "Tháng có giá trị > 0",
                AppTheme.PRIMARY,
                AppTheme.PRIMARY_SOFT_BG
        );
        zeroMonthsCard = new StatCard(
                "Số tháng bằng 0",
                1,
                "Tháng không có mở app (0)",
                AppTheme.WARNING,
                AppTheme.WARNING_BG
        );
        sumUserMonthsCard = new StatCard(
                "Tổng người-tháng (12T)",
                22,
                "Tổng 12 cột trong năm",
                AppTheme.TEXT_SECONDARY,
                AppTheme.NEUTRAL_BG
        );

        cardsGrid.add(peakMonthlyUsersCard);
        cardsGrid.add(activeMonthsCard);
        cardsGrid.add(zeroMonthsCard);
        cardsGrid.add(sumUserMonthsCard);
        return cardsGrid;
    }

    private JPanel createMonthlyNumericBreakdownStrip() {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(AppTheme.SURFACE_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, AppTheme.SPACING_MD, 8, AppTheme.SPACING_MD)
        ));

        JLabel stripHeader = new JLabel(
                "Bảng đối chiếu số người dùng duy nhất mở app qua 12 tháng (Khớp 100% với kết quả QUERY 10 trong script/03_admin_queries.sql):"
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
        int[] counts = userRepository.getMonthlyDistinctAppOpeningUsers(year);

        int sumUserMonths = 0;
        int peak = 0;
        int nonZeroMonths = 0;
        int zeroMonths = 0;
        for (int i = 0; i < 12; i++) {
            int c = counts[i];
            sumUserMonths += c;
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
                monthSummaryValueLabels[i].setForeground(c > 0 ? AppTheme.SUCCESS : AppTheme.TEXT_SECONDARY);
            }
        }

        if (peakMonthlyUsersCard != null) {
            peakMonthlyUsersCard.setCount(peak);
            activeMonthsCard.setCount(nonZeroMonths);
            zeroMonthsCard.setCount(zeroMonths);
            sumUserMonthsCard.setCount(sumUserMonths);
        }

        String title = "Biểu đồ hoạt động 12 tháng — Năm " + year;
        String subtitle = sumUserMonths == 0
                ? "Năm " + year + ": Trạng thái năm rỗng (Empty-Year State) — cả 12 tháng đều có 0 người mở ứng dụng"
                : "Năm " + year + ": COUNT(DISTINCT aoe.user_id) theo từng tháng ("
                + nonZeroMonths + " tháng có hoạt động, " + zeroMonths + " tháng bằng 0 • QUERY 10)";
        barChartPanel.setChartData(year, counts, title, subtitle);
    }

    private JButton createSecondaryButton(String text, int width) {
        return AppTheme.createSecondaryButton(text, width);
    }
}
