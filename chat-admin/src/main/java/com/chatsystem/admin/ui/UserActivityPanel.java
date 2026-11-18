package com.chatsystem.admin.ui;

import com.chatsystem.admin.components.StatCard;
import com.chatsystem.admin.data.MockUserRepository;
import com.chatsystem.admin.model.UserActivityRecord;
import com.chatsystem.admin.theme.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.text.Collator;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Active Users & Activity Metrics screen panel (Requirement 8 / Step G - D).
 * Displays date-range controls, name, username, account creation time, app-open count,
 * distinct direct-message recipients, and distinct groups messaged, with name filter,
 * sorting by name/creation time, and a metric filter selector containing strictly the
 * 3 concrete metrics ({@code Số lần mở ứng dụng}, {@code Số người đã chat}, {@code Số nhóm đã chat})
 * paired with {@code =}, {@code <}, {@code >} and numeric input (no undefined total activity metric).
 */
public class UserActivityPanel extends JPanel {

    private static final String CARD_TABLE = "TABLE";
    private static final String CARD_EMPTY = "EMPTY";

    private static final String METRIC_APP_OPENS = "Số lần mở ứng dụng";
    private static final String METRIC_PEOPLE_CHATTED = "Số người đã chat";
    private static final String METRIC_GROUPS_CHATTED = "Số nhóm đã chat";

    // Strictly the 3 concrete metrics per Business Rule 4 (no undefined "total activity" metric)
    private static final String[] METRIC_OPTIONS = {
            METRIC_APP_OPENS,
            METRIC_PEOPLE_CHATTED,
            METRIC_GROUPS_CHATTED
    };

    private static final String[] OPERATOR_OPTIONS = {"=", "<", ">"};

    private static final String[] SORT_OPTIONS = {
            "Ngày tạo: Cũ nhất",
            "Ngày tạo: Mới nhất",
            "Họ tên: A–Z",
            "Họ tên: Z–A"
    };

    private final MockUserRepository userRepository;
    private final UserActivityTableModel tableModel;
    private final JTable activityTable;
    private final CardLayout viewportCardLayout;
    private final JPanel viewportDeck;

    private StatCard activeUsersCountCard;
    private StatCard totalAppOpensCard;
    private StatCard totalDmPartnersCard;
    private StatCard totalGroupsMessagedCard;

    private JTextField nameFilterField;
    private JTextField fromDateField;
    private JTextField toDateField;
    private JComboBox<String> metricCombo;
    private JComboBox<String> operatorCombo;
    private JTextField metricValueField;
    private JComboBox<String> sortCombo;
    private JPanel validationBanner;
    private JLabel validationMessageLabel;
    private JLabel selectionStatusLabel;
    private JLabel recordCountLabel;
    private JLabel emptyStateDetailLabel;

    public UserActivityPanel(MockUserRepository userRepository) {
        super(new BorderLayout(0, AppTheme.SPACING_MD - 2));
        this.userRepository = userRepository;
        this.tableModel = new UserActivityTableModel(new ArrayList<>());
        this.activityTable = new JTable(tableModel);
        this.viewportCardLayout = new CardLayout();
        this.viewportDeck = new JPanel(viewportCardLayout);

        setBackground(AppTheme.PAGE_BG);
        setBorder(BorderFactory.createEmptyBorder(
                AppTheme.CONTENT_PADDING - 6,
                AppTheme.CONTENT_PADDING,
                AppTheme.CONTENT_PADDING - 6,
                AppTheme.CONTENT_PADDING
        ));

        JPanel topSection = new JPanel(new BorderLayout(0, AppTheme.SPACING_SM + 2));
        topSection.setOpaque(false);
        topSection.add(createPageIntroBar(), BorderLayout.NORTH);
        topSection.add(createSummaryCardsRow(), BorderLayout.CENTER);

        add(topSection, BorderLayout.NORTH);
        add(createMainTableCard(), BorderLayout.CENTER);

        this.userRepository.addDataChangeListener(this::applyFiltersAndSort);
        applyFiltersAndSort();
    }

    private JPanel createPageIntroBar() {
        JPanel introPanel = new JPanel(new BorderLayout(AppTheme.SPACING_MD, 0));
        introPanel.setOpaque(false);

        JPanel textGroup = new JPanel();
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));
        textGroup.setOpaque(false);

        JLabel titleLabel = new JLabel("Người dùng hoạt động & Chỉ số tương tác");
        titleLabel.setFont(AppTheme.FONT_PAGE_TITLE);
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(
                "Thống kê 3 chỉ số độc lập theo khoảng thời gian: Số lần mở ứng dụng, Số người đã chat, Số nhóm đã chat (QUERY 9A & 9B)"
        );
        subtitleLabel.setFont(AppTheme.FONT_BODY);
        subtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textGroup.add(titleLabel);
        textGroup.add(Box.createVerticalStrut(2));
        textGroup.add(subtitleLabel);

        JPanel rightPresetGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SPACING_SM, 4));
        rightPresetGroup.setOpaque(false);

        JButton marchPresetBtn = createPrimaryOutlineButton("Mẫu Tháng 03/2026 (QUERY 9)", 202);
        marchPresetBtn.setToolTipText("Áp dụng khoảng thời gian 2026-03-01 đến 2026-03-31 khớp với Test Case 9 trong SQL");
        marchPresetBtn.addActionListener(e -> {
            fromDateField.setText("2026-03-01");
            toDateField.setText("2026-03-31");
            metricValueField.setText("");
            applyFiltersAndSort();
        });

        JButton allYearBtn = createSecondaryButton("Cả năm 2026", 112);
        allYearBtn.addActionListener(e -> {
            fromDateField.setText("2026-01-01");
            toDateField.setText("2026-12-31");
            metricValueField.setText("");
            applyFiltersAndSort();
        });

        rightPresetGroup.add(marchPresetBtn);
        rightPresetGroup.add(allYearBtn);

        introPanel.add(textGroup, BorderLayout.WEST);
        introPanel.add(rightPresetGroup, BorderLayout.EAST);
        return introPanel;
    }

    private JPanel createSummaryCardsRow() {
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, AppTheme.SPACING_MD, 0));
        cardsGrid.setOpaque(false);

        activeUsersCountCard = new StatCard(
                "Người dùng hoạt động",
                7,
                "Khớp bộ lọc hiện tại",
                AppTheme.PRIMARY,
                AppTheme.PRIMARY_SOFT_BG
        );
        totalAppOpensCard = new StatCard(
                "Tổng lượt mở ứng dụng",
                8,
                "Bảng app_open_events",
                AppTheme.SUCCESS,
                AppTheme.SUCCESS_BG
        );
        totalDmPartnersCard = new StatCard(
                "Tổng lượt người đã chat",
                5,
                "DISTINCT người nhận DM",
                AppTheme.WARNING,
                AppTheme.WARNING_BG
        );
        totalGroupsMessagedCard = new StatCard(
                "Tổng lượt nhóm đã chat",
                5,
                "DISTINCT nhóm gửi tin",
                AppTheme.TEXT_SECONDARY,
                AppTheme.NEUTRAL_BG
        );

        cardsGrid.add(activeUsersCountCard);
        cardsGrid.add(totalAppOpensCard);
        cardsGrid.add(totalDmPartnersCard);
        cardsGrid.add(totalGroupsMessagedCard);
        return cardsGrid;
    }

    private JPanel createMainTableCard() {
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(AppTheme.SURFACE_CARD);
        tableCard.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1, true));

        JPanel topBarWrapper = new JPanel(new BorderLayout());
        topBarWrapper.setOpaque(false);
        topBarWrapper.add(createTwoRowFilterToolbar(), BorderLayout.NORTH);
        topBarWrapper.add(createValidationBanner(), BorderLayout.SOUTH);

        viewportDeck.setBackground(AppTheme.SURFACE_CARD);
        viewportDeck.add(createTableScrollPane(), CARD_TABLE);
        viewportDeck.add(createEmptyResultPanel(), CARD_EMPTY);

        tableCard.add(topBarWrapper, BorderLayout.NORTH);
        tableCard.add(viewportDeck, BorderLayout.CENTER);
        tableCard.add(createSelectionAndFooterBar(), BorderLayout.SOUTH);

        return tableCard;
    }

    private JPanel createTwoRowFilterToolbar() {
        JPanel container = new JPanel(new GridLayout(2, 1, 0, 8));
        container.setBackground(AppTheme.SURFACE_CARD);
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER),
                BorderFactory.createEmptyBorder(10, AppTheme.SPACING_MD, 10, AppTheme.SPACING_MD)
        ));

        // Row 1: Name filter + Date range (From / To / Apply) + Sort combo
        JPanel row1 = new JPanel(new GridBagLayout());
        row1.setOpaque(false);

        nameFilterField = new JTextField();
        nameFilterField.setFont(AppTheme.FONT_BODY);
        nameFilterField.setForeground(AppTheme.TEXT_PRIMARY);
        nameFilterField.setPreferredSize(new Dimension(200, AppTheme.CONTROL_HEIGHT));
        nameFilterField.setMinimumSize(new Dimension(130, AppTheme.CONTROL_HEIGHT));
        nameFilterField.putClientProperty("JTextField.placeholderText", "Lọc theo họ tên hoặc username...");
        nameFilterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFiltersAndSort();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFiltersAndSort();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFiltersAndSort();
            }
        });

        fromDateField = new JTextField("2026-03-01");
        fromDateField.setFont(AppTheme.FONT_BODY);
        fromDateField.setPreferredSize(new Dimension(112, AppTheme.CONTROL_HEIGHT));
        fromDateField.setMinimumSize(new Dimension(112, AppTheme.CONTROL_HEIGHT));
        fromDateField.addActionListener(e -> applyFiltersAndSort());

        toDateField = new JTextField("2026-03-31");
        toDateField.setFont(AppTheme.FONT_BODY);
        toDateField.setPreferredSize(new Dimension(112, AppTheme.CONTROL_HEIGHT));
        toDateField.setMinimumSize(new Dimension(112, AppTheme.CONTROL_HEIGHT));
        toDateField.addActionListener(e -> applyFiltersAndSort());

        JButton applyDateBtn = createSecondaryButton("Lọc ngày", 88);
        applyDateBtn.addActionListener(e -> applyFiltersAndSort());

        sortCombo = new JComboBox<>(SORT_OPTIONS);
        sortCombo.setFont(AppTheme.FONT_BODY);
        sortCombo.setPreferredSize(new Dimension(176, AppTheme.CONTROL_HEIGHT));
        sortCombo.setMinimumSize(new Dimension(176, AppTheme.CONTROL_HEIGHT));
        sortCombo.addActionListener(e -> applyFiltersAndSort());

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridy = 0;

        gbc1.gridx = 0;
        gbc1.weightx = 1.0;
        gbc1.fill = GridBagConstraints.HORIZONTAL;
        gbc1.insets = new Insets(0, 0, 0, 8);
        row1.add(nameFilterField, gbc1);

        gbc1.gridx = 1;
        gbc1.weightx = 0.0;
        gbc1.fill = GridBagConstraints.NONE;
        gbc1.insets = new Insets(0, 2, 0, 4);
        row1.add(createToolbarLabel("Từ ngày:"), gbc1);

        gbc1.gridx = 2;
        gbc1.insets = new Insets(0, 0, 0, 6);
        row1.add(fromDateField, gbc1);

        gbc1.gridx = 3;
        gbc1.insets = new Insets(0, 2, 0, 4);
        row1.add(createToolbarLabel("Đến ngày:"), gbc1);

        gbc1.gridx = 4;
        gbc1.insets = new Insets(0, 0, 0, 6);
        row1.add(toDateField, gbc1);

        gbc1.gridx = 5;
        gbc1.insets = new Insets(0, 0, 0, 10);
        row1.add(applyDateBtn, gbc1);

        gbc1.gridx = 6;
        gbc1.insets = new Insets(0, 2, 0, 4);
        row1.add(createToolbarLabel("Sắp xếp:"), gbc1);

        gbc1.gridx = 7;
        gbc1.insets = new Insets(0, 0, 0, 0);
        row1.add(sortCombo, gbc1);

        // Row 2: Metric selector (strictly 3 metrics) + Operator (=, <, >) + Numeric input + Clear selection + Reset
        JPanel row2 = new JPanel(new GridBagLayout());
        row2.setOpaque(false);

        metricCombo = new JComboBox<>(METRIC_OPTIONS);
        metricCombo.setFont(AppTheme.FONT_BODY_BOLD);
        metricCombo.setPreferredSize(new Dimension(188, AppTheme.CONTROL_HEIGHT));
        metricCombo.setMinimumSize(new Dimension(188, AppTheme.CONTROL_HEIGHT));
        metricCombo.setToolTipText("Chọn 1 trong 3 chỉ số hoạt động cụ thể (Không dùng chỉ số tổng gộp không xác định)");
        metricCombo.addActionListener(e -> applyFiltersAndSort());

        operatorCombo = new JComboBox<>(OPERATOR_OPTIONS);
        operatorCombo.setFont(AppTheme.FONT_BODY_BOLD);
        operatorCombo.setPreferredSize(new Dimension(64, AppTheme.CONTROL_HEIGHT));
        operatorCombo.setMinimumSize(new Dimension(64, AppTheme.CONTROL_HEIGHT));
        operatorCombo.addActionListener(e -> applyFiltersAndSort());

        metricValueField = new JTextField();
        metricValueField.setFont(AppTheme.FONT_BODY);
        metricValueField.setPreferredSize(new Dimension(104, AppTheme.CONTROL_HEIGHT));
        metricValueField.setMinimumSize(new Dimension(104, AppTheme.CONTROL_HEIGHT));
        metricValueField.putClientProperty("JTextField.placeholderText", "Giá trị số...");
        metricValueField.setToolTipText("Nhập số nguyên không âm để lọc theo chỉ số đã chọn (để trống để hiển thị tất cả)");
        metricValueField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFiltersAndSort();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFiltersAndSort();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFiltersAndSort();
            }
        });

        JLabel ruleHintLabel = new JLabel(
                "Chọn 1 trong 3 chỉ số cụ thể để so sánh (=, <, >) • Không cộng gộp chỉ số tổng"
        );
        ruleHintLabel.setFont(AppTheme.FONT_CAPTION);
        ruleHintLabel.setForeground(AppTheme.TEXT_SECONDARY);

        JButton clearSelectionBtn = createSecondaryButton("Bỏ chọn", 84);
        clearSelectionBtn.addActionListener(e -> activityTable.clearSelection());

        JButton resetBtn = createSecondaryButton("Làm mới", 88);
        resetBtn.addActionListener(e -> resetFilters());

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridy = 0;

        gbc2.gridx = 0;
        gbc2.insets = new Insets(0, 0, 0, 6);
        row2.add(createToolbarLabel("Lọc theo chỉ số:"), gbc2);

        gbc2.gridx = 1;
        gbc2.insets = new Insets(0, 0, 0, 6);
        row2.add(metricCombo, gbc2);

        gbc2.gridx = 2;
        gbc2.insets = new Insets(0, 0, 0, 6);
        row2.add(operatorCombo, gbc2);

        gbc2.gridx = 3;
        gbc2.insets = new Insets(0, 0, 0, 12);
        row2.add(metricValueField, gbc2);

        gbc2.gridx = 4;
        gbc2.weightx = 1.0;
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.insets = new Insets(0, 0, 0, 8);
        row2.add(ruleHintLabel, gbc2);

        gbc2.gridx = 5;
        gbc2.weightx = 0.0;
        gbc2.fill = GridBagConstraints.NONE;
        gbc2.insets = new Insets(0, 0, 0, 6);
        row2.add(clearSelectionBtn, gbc2);

        gbc2.gridx = 6;
        gbc2.insets = new Insets(0, 0, 0, 0);
        row2.add(resetBtn, gbc2);

        container.add(row1);
        container.add(row2);
        return container;
    }

    private JPanel createValidationBanner() {
        validationBanner = new JPanel(new BorderLayout(AppTheme.SPACING_SM, 0));
        validationBanner.setBackground(AppTheme.DANGER_BG);
        validationBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.DANGER),
                BorderFactory.createEmptyBorder(8, AppTheme.SPACING_MD, 8, AppTheme.SPACING_MD)
        ));
        validationMessageLabel = new JLabel();
        validationMessageLabel.setFont(AppTheme.FONT_CAPTION_BOLD);
        validationMessageLabel.setForeground(AppTheme.DANGER);
        validationBanner.add(validationMessageLabel, BorderLayout.CENTER);
        validationBanner.setVisible(false);
        return validationBanner;
    }

    private JScrollPane createTableScrollPane() {
        activityTable.setFont(AppTheme.FONT_BODY);
        activityTable.setForeground(AppTheme.TEXT_PRIMARY);
        activityTable.setBackground(AppTheme.SURFACE_CARD);
        activityTable.setRowHeight(AppTheme.TABLE_ROW_HEIGHT);
        activityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        activityTable.setSelectionBackground(AppTheme.PRIMARY_SOFT_BG);
        activityTable.setSelectionForeground(AppTheme.TEXT_PRIMARY);
        activityTable.setShowHorizontalLines(true);
        activityTable.setShowVerticalLines(false);
        activityTable.setGridColor(AppTheme.BORDER);
        activityTable.setIntercellSpacing(new Dimension(0, 1));
        activityTable.setFillsViewportHeight(true);

        JTableHeader header = activityTable.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 40));
        header.setFont(AppTheme.FONT_CAPTION_BOLD);
        header.setBackground(AppTheme.PAGE_BG);
        header.setForeground(AppTheme.TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER));

        DefaultTableCellRenderer centerRenderer = createPaddedRenderer(SwingConstants.CENTER, AppTheme.FONT_BODY, AppTheme.TEXT_SECONDARY);
        DefaultTableCellRenderer boldNameRenderer = createPaddedRenderer(SwingConstants.LEFT, AppTheme.FONT_BODY_BOLD, AppTheme.TEXT_PRIMARY);
        DefaultTableCellRenderer usernameRenderer = createPaddedRenderer(SwingConstants.LEFT, AppTheme.FONT_BODY, AppTheme.PRIMARY);

        TableColumnModel cm = activityTable.getColumnModel();
        cm.getColumn(0).setCellRenderer(centerRenderer);
        cm.getColumn(0).setMaxWidth(44);

        cm.getColumn(1).setCellRenderer(boldNameRenderer);
        cm.getColumn(1).setPreferredWidth(185);

        cm.getColumn(2).setCellRenderer(usernameRenderer);
        cm.getColumn(2).setPreferredWidth(135);

        cm.getColumn(3).setCellRenderer(centerRenderer);
        cm.getColumn(3).setPreferredWidth(145);

        cm.getColumn(4).setCellRenderer(new MetricPillRenderer(AppTheme.PRIMARY, AppTheme.PRIMARY_SOFT_BG));
        cm.getColumn(4).setPreferredWidth(155);

        cm.getColumn(5).setCellRenderer(new MetricPillRenderer(AppTheme.SUCCESS, AppTheme.SUCCESS_BG));
        cm.getColumn(5).setPreferredWidth(155);

        cm.getColumn(6).setCellRenderer(new MetricPillRenderer(AppTheme.WARNING, AppTheme.WARNING_BG));
        cm.getColumn(6).setPreferredWidth(155);

        activityTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectionStatus();
            }
        });

        JScrollPane scrollPane = new JScrollPane(activityTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(AppTheme.SURFACE_CARD);
        return scrollPane;
    }

    private JPanel createEmptyResultPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppTheme.SURFACE_CARD);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);

        JLabel badge = new JLabel("TRẠNG THÁI KẾT QUẢ RỖNG (EMPTY RESULT)");
        badge.setFont(AppTheme.FONT_CAPTION_BOLD);
        badge.setForeground(AppTheme.WARNING);
        badge.setOpaque(true);
        badge.setBackground(AppTheme.WARNING_BG);
        badge.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Không tìm thấy người dùng hoạt động thỏa mãn bộ lọc");
        title.setFont(AppTheme.FONT_SECTION_TITLE);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyStateDetailLabel = new JLabel("Không có người dùng nào khớp với khoảng thời gian hoặc điều kiện chỉ số đã chọn.");
        emptyStateDetailLabel.setFont(AppTheme.FONT_BODY);
        emptyStateDetailLabel.setForeground(AppTheme.TEXT_SECONDARY);
        emptyStateDetailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton resetBtn = createSecondaryButton("Đặt lại bộ lọc", 140);
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetBtn.addActionListener(e -> resetFilters());

        box.add(badge);
        box.add(Box.createVerticalStrut(AppTheme.SPACING_SM));
        box.add(title);
        box.add(Box.createVerticalStrut(6));
        box.add(emptyStateDetailLabel);
        box.add(Box.createVerticalStrut(AppTheme.SPACING_MD));
        box.add(resetBtn);

        panel.add(box);
        return panel;
    }

    private JPanel createSelectionAndFooterBar() {
        JPanel footer = new JPanel(new BorderLayout(AppTheme.SPACING_MD, 0));
        footer.setBackground(AppTheme.SURFACE_CARD);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER),
                BorderFactory.createEmptyBorder(10, AppTheme.SPACING_MD, 10, AppTheme.SPACING_MD)
        ));

        selectionStatusLabel = new JLabel();
        selectionStatusLabel.setFont(AppTheme.FONT_CAPTION_BOLD);
        selectionStatusLabel.setForeground(AppTheme.TEXT_SECONDARY);

        recordCountLabel = new JLabel();
        recordCountLabel.setFont(AppTheme.FONT_CAPTION);
        recordCountLabel.setForeground(AppTheme.TEXT_SECONDARY);

        footer.add(selectionStatusLabel, BorderLayout.WEST);
        footer.add(recordCountLabel, BorderLayout.EAST);
        return footer;
    }

    private void resetFilters() {
        nameFilterField.setText("");
        fromDateField.setText("2026-03-01");
        toDateField.setText("2026-03-31");
        metricCombo.setSelectedIndex(0);
        operatorCombo.setSelectedIndex(0);
        metricValueField.setText("");
        sortCombo.setSelectedIndex(0);
        validationBanner.setVisible(false);
        applyFiltersAndSort();
    }

    private void applyFiltersAndSort() {
        String keyword = nameFilterField != null ? nameFilterField.getText().trim().toLowerCase(Locale.ROOT) : "";
        String fromText = fromDateField != null ? fromDateField.getText().trim() : "";
        String toText = toDateField != null ? toDateField.getText().trim() : "";
        String selectedMetric = metricCombo != null ? (String) metricCombo.getSelectedItem() : METRIC_APP_OPENS;
        String op = operatorCombo != null ? (String) operatorCombo.getSelectedItem() : "=";
        String numText = metricValueField != null ? metricValueField.getText().trim() : "";

        LocalDate fromDate = null;
        LocalDate toDate = null;

        if (!fromText.isEmpty()) {
            try {
                fromDate = LocalDate.parse(fromText);
            } catch (DateTimeParseException ex) {
                showValidationError("Lỗi khoảng thời gian: 'Từ ngày' (\"" + fromText
                        + "\") không đúng định dạng YYYY-MM-DD (ví dụ: 2026-03-01).");
                return;
            }
        }
        if (!toText.isEmpty()) {
            try {
                toDate = LocalDate.parse(toText);
            } catch (DateTimeParseException ex) {
                showValidationError("Lỗi khoảng thời gian: 'Đến ngày' (\"" + toText
                        + "\") không đúng định dạng YYYY-MM-DD (ví dụ: 2026-03-31).");
                return;
            }
        }
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            showValidationError("Lỗi khoảng thời gian: 'Từ ngày' (" + fromDate
                    + ") không được lớn hơn 'Đến ngày' (" + toDate + ").");
            return;
        }

        Integer targetMetricVal = null;
        if (!numText.isEmpty()) {
            try {
                targetMetricVal = Integer.parseInt(numText);
                if (targetMetricVal < 0) {
                    showValidationError("Lỗi bộ lọc chỉ số hoạt động: Giá trị so sánh không được là số âm (" + numText + ").");
                    return;
                }
            } catch (NumberFormatException ex) {
                showValidationError("Lỗi bộ lọc chỉ số hoạt động: \"" + numText
                        + "\" không phải là số nguyên hợp lệ (ví dụ: 0, 1, 2).");
                return;
            }
        }

        if (validationBanner != null) {
            validationBanner.setVisible(false);
        }

        List<UserActivityRecord> activeInPeriod = userRepository.getUserActivityRecords(fromDate, toDate);
        List<UserActivityRecord> filtered = new ArrayList<>();
        for (UserActivityRecord r : activeInPeriod) {
            if (!keyword.isEmpty()) {
                boolean matchName = r.getFullName().toLowerCase(Locale.ROOT).contains(keyword);
                boolean matchUser = r.getUsername().toLowerCase(Locale.ROOT).contains(keyword);
                if (!matchName && !matchUser) {
                    continue;
                }
            }
            if (targetMetricVal != null) {
                int actualVal = switch (selectedMetric != null ? selectedMetric : METRIC_APP_OPENS) {
                    case METRIC_PEOPLE_CHATTED -> r.getDistinctDirectRecipientsCount();
                    case METRIC_GROUPS_CHATTED -> r.getDistinctGroupsMessagedCount();
                    default -> r.getAppOpenCount();
                };
                boolean matchesOp = switch (op != null ? op : "=") {
                    case "<" -> actualVal < targetMetricVal;
                    case ">" -> actualVal > targetMetricVal;
                    default -> actualVal == targetMetricVal;
                };
                if (!matchesOp) {
                    continue;
                }
            }
            filtered.add(r);
        }

        String sortOption = sortCombo != null ? (String) sortCombo.getSelectedItem() : SORT_OPTIONS[0];
        Collator viCollator = Collator.getInstance(Locale.of("vi", "VN"));
        Comparator<UserActivityRecord> cmp = switch (sortOption != null ? sortOption : SORT_OPTIONS[0]) {
            case "Họ tên: A–Z" -> (a, b) -> {
                int c = viCollator.compare(a.getFullName(), b.getFullName());
                return c != 0 ? c : Long.compare(a.getUserId(), b.getUserId());
            };
            case "Họ tên: Z–A" -> (a, b) -> {
                int c = viCollator.compare(b.getFullName(), a.getFullName());
                return c != 0 ? c : Long.compare(a.getUserId(), b.getUserId());
            };
            case "Ngày tạo: Mới nhất" -> Comparator.comparing(UserActivityRecord::getCreatedAt).reversed()
                    .thenComparing(Comparator.comparingLong(UserActivityRecord::getUserId).reversed());
            default -> Comparator.comparing(UserActivityRecord::getCreatedAt)
                    .thenComparingLong(UserActivityRecord::getUserId);
        };
        filtered.sort(cmp);

        tableModel.setRecords(filtered);

        if (filtered.isEmpty()) {
            viewportCardLayout.show(viewportDeck, CARD_EMPTY);
            if (emptyStateDetailLabel != null) {
                emptyStateDetailLabel.setText(
                        "Không có người dùng hoạt động nào thỏa mãn khoảng ngày hoặc điều kiện chỉ số hiện tại."
                );
            }
        } else {
            viewportCardLayout.show(viewportDeck, CARD_TABLE);
            activityTable.setRowSelectionInterval(0, 0);
        }

        if (activeUsersCountCard != null) {
            int sumOpens = filtered.stream().mapToInt(UserActivityRecord::getAppOpenCount).sum();
            int sumDm = filtered.stream().mapToInt(UserActivityRecord::getDistinctDirectRecipientsCount).sum();
            int sumGm = filtered.stream().mapToInt(UserActivityRecord::getDistinctGroupsMessagedCount).sum();
            activeUsersCountCard.setCount(filtered.size());
            totalAppOpensCard.setCount(sumOpens);
            totalDmPartnersCard.setCount(sumDm);
            totalGroupsMessagedCard.setCount(sumGm);
        }
        if (recordCountLabel != null) {
            recordCountLabel.setText(
                    "Hiển thị " + filtered.size() + " / " + activeInPeriod.size()
                            + " người dùng hoạt động trong kỳ (Khớp QUERY 9A/9B)"
            );
        }
        updateSelectionStatus();
    }

    private void showValidationError(String message) {
        if (validationBanner != null && validationMessageLabel != null) {
            validationMessageLabel.setText(message);
            validationBanner.setVisible(true);
            revalidate();
            repaint();
        }
    }

    private void updateSelectionStatus() {
        if (selectionStatusLabel == null) {
            return;
        }
        int row = activityTable.getSelectedRow();
        if (row < 0 || row >= tableModel.getRowCount()) {
            selectionStatusLabel.setText(
                    "Trạng thái chọn: Chưa chọn dòng nào (No-selection) — Chọn một người dùng để xem 3 chỉ số hoạt động chi tiết."
            );
            selectionStatusLabel.setForeground(AppTheme.TEXT_SECONDARY);
        } else {
            UserActivityRecord r = tableModel.getRecordAt(row);
            selectionStatusLabel.setText(
                    "Đang chọn: " + r.getFullName() + " (@" + r.getUsername()
                            + ")  •  Mở app: " + r.getAppOpenCount()
                            + "  •  Người đã chat: " + r.getDistinctDirectRecipientsCount()
                            + "  •  Nhóm đã chat: " + r.getDistinctGroupsMessagedCount()
            );
            selectionStatusLabel.setForeground(AppTheme.PRIMARY);
        }
    }

    private JLabel createToolbarLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.FONT_CAPTION_BOLD);
        label.setForeground(AppTheme.TEXT_SECONDARY);
        return label;
    }

    private JButton createPrimaryOutlineButton(String text, int width) {
        JButton btn = new JButton(text);
        btn.setFont(AppTheme.FONT_CAPTION_BOLD);
        btn.setForeground(AppTheme.PRIMARY);
        btn.setBackground(AppTheme.PRIMARY_SOFT_BG);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(0, 6, 0, 6));
        btn.setPreferredSize(new Dimension(width, AppTheme.CONTROL_HEIGHT));
        btn.setMinimumSize(new Dimension(width, AppTheme.CONTROL_HEIGHT));
        return btn;
    }

    private JButton createSecondaryButton(String text, int width) {
        JButton btn = new JButton(text);
        btn.setFont(AppTheme.FONT_BODY_BOLD);
        btn.setForeground(AppTheme.TEXT_PRIMARY);
        btn.setBackground(AppTheme.SURFACE_CARD);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(0, 6, 0, 6));
        btn.setPreferredSize(new Dimension(width, AppTheme.CONTROL_HEIGHT));
        btn.setMinimumSize(new Dimension(width, AppTheme.CONTROL_HEIGHT));
        return btn;
    }

    private DefaultTableCellRenderer createPaddedRenderer(int alignment, java.awt.Font font, Color textColor) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
            ) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column
                );
                label.setFont(font);
                label.setHorizontalAlignment(alignment);
                label.setForeground(textColor);
                label.setBackground(isSelected ? AppTheme.PRIMARY_SOFT_BG : AppTheme.SURFACE_CARD);
                if (alignment == SwingConstants.CENTER) {
                    label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                } else {
                    label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
                }
                return label;
            }
        };
    }

    private static final class MetricPillRenderer extends JPanel implements javax.swing.table.TableCellRenderer {

        private final Color accentFg;
        private final Color accentBg;
        private String valueText = "0";
        private boolean zeroValue = true;
        private boolean rowSelected = false;

        private MetricPillRenderer(Color accentFg, Color accentBg) {
            this.accentFg = accentFg;
            this.accentBg = accentBg;
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            this.valueText = value != null ? value.toString() : "0";
            this.zeroValue = "0".equals(this.valueText);
            this.rowSelected = isSelected;
            setBackground(isSelected ? AppTheme.PRIMARY_SOFT_BG : AppTheme.SURFACE_CARD);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg = zeroValue ? AppTheme.NEUTRAL_BG : accentBg;
            Color fg = zeroValue ? AppTheme.TEXT_SECONDARY : accentFg;

            g2.setFont(AppTheme.FONT_BODY_BOLD);
            FontMetrics fm = g2.getFontMetrics();
            int pillW = Math.max(44, fm.stringWidth(valueText) + 20);
            int pillH = 24;
            int x = (getWidth() - pillW) / 2;
            int y = (getHeight() - pillH) / 2;

            g2.setColor(bg);
            g2.fillRoundRect(x, y, pillW, pillH, 12, 12);
            g2.setColor(fg);
            int textX = x + (pillW - fm.stringWidth(valueText)) / 2;
            int textY = y + ((pillH - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(valueText, textX, textY);

            if (!rowSelected) {
                g2.setColor(AppTheme.BORDER);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
            g2.dispose();
        }
    }

    private static final class UserActivityTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {
                "STT",
                "Họ và tên",
                "Tên đăng nhập",
                "Thời gian tạo TK",
                "Số lần mở ứng dụng",
                "Số người đã chat",
                "Số nhóm đã chat"
        };

        private final List<UserActivityRecord> records;

        private UserActivityTableModel(List<UserActivityRecord> initialRecords) {
            this.records = new ArrayList<>(initialRecords);
        }

        public void setRecords(List<UserActivityRecord> updated) {
            this.records.clear();
            if (updated != null) {
                this.records.addAll(updated);
            }
            fireTableDataChanged();
        }

        public UserActivityRecord getRecordAt(int rowIndex) {
            if (rowIndex < 0 || rowIndex >= records.size()) {
                return null;
            }
            return records.get(rowIndex);
        }

        @Override
        public int getRowCount() {
            return records.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            UserActivityRecord r = records.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> rowIndex + 1;
                case 1 -> r.getFullName();
                case 2 -> "@" + r.getUsername();
                case 3 -> r.getFormattedCreatedAt();
                case 4 -> String.valueOf(r.getAppOpenCount());
                case 5 -> String.valueOf(r.getDistinctDirectRecipientsCount());
                case 6 -> String.valueOf(r.getDistinctGroupsMessagedCount());
                default -> "";
            };
        }
    }
}
