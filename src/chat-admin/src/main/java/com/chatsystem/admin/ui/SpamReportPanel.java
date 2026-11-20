package com.chatsystem.admin.ui;

import com.chatsystem.admin.components.StatCard;
import com.chatsystem.admin.data.MockUserRepository;
import com.chatsystem.admin.model.SpamReportRecord;
import com.chatsystem.admin.model.User;
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
import javax.swing.SwingUtilities;
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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Spam Reports Management screen panel (Requirement 4 / Step F - C).
 * Displays reporter, reported user, report time, reason, and status, with filtering by
 * validated date range and reported username, sorting by report time and reported username,
 * a detail dialog showing the synthetic message snapshot, and lock-account confirmation
 * targeting the reported user (in-memory mock mutation only).
 */
public class SpamReportPanel extends JPanel {

    private static final String CARD_TABLE = "TABLE";
    private static final String CARD_EMPTY = "EMPTY";

    private static final String[] SORT_OPTIONS = {
            "Mới nhất",
            "Cũ nhất",
            "Username A–Z",
            "Username Z–A"
    };

    private static final int COL_STATUS = 5;
    private static final int COL_ACTIONS = 6;

    private final MockUserRepository userRepository;
    private final SpamReportTableModel tableModel;
    private final JTable reportTable;
    private final CardLayout viewportCardLayout;
    private final JPanel viewportDeck;

    private StatCard totalReportsCard;
    private StatCard pendingReportsCard;
    private StatCard resolvedReportsCard;
    private StatCard lockedOffendersCard;

    private JTextField reportedUserFilterField;
    private JTextField fromDateField;
    private JTextField toDateField;
    private JComboBox<String> sortCombo;
    private JPanel validationBanner;
    private JLabel validationMessageLabel;
    private JLabel selectionStatusLabel;
    private JLabel recordCountLabel;
    private JLabel emptyStateDetailLabel;

    public SpamReportPanel(MockUserRepository userRepository) {
        super(new BorderLayout(0, AppTheme.SPACING_MD));
        this.userRepository = userRepository;
        this.tableModel = new SpamReportTableModel(userRepository.getSpamReports());
        this.reportTable = new JTable(tableModel);
        this.viewportCardLayout = new CardLayout();
        this.viewportDeck = new JPanel(viewportCardLayout);

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

        JLabel titleLabel = new JLabel("Quản lý báo cáo spam");
        titleLabel.setFont(AppTheme.FONT_PAGE_TITLE);
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(
                "Tra cứu báo cáo tin nhắn rác, kiểm tra bản chụp tin nhắn (message_snapshot) và khóa tài khoản vi phạm"
        );
        subtitleLabel.setFont(AppTheme.FONT_BODY);
        subtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textGroup.add(titleLabel);
        textGroup.add(Box.createVerticalStrut(4));
        textGroup.add(subtitleLabel);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SPACING_SM, 4));
        rightActions.setOpaque(false);

        JButton detailBtn = createPrimaryOutlineButton("Xem chi tiết & Snapshot", 172);
        detailBtn.addActionListener(e -> openDetailForSelectedRow());

        JButton lockBtn = createDangerActionButton("Khóa/Mở TK bị báo cáo", 174);
        lockBtn.addActionListener(e -> openLockConfirmForSelectedRow());

        rightActions.add(detailBtn);
        rightActions.add(lockBtn);

        introPanel.add(textGroup, BorderLayout.WEST);
        introPanel.add(rightActions, BorderLayout.EAST);
        return introPanel;
    }

    private JPanel createSummaryCardsRow() {
        List<SpamReportRecord> all = userRepository.getSpamReports();
        int total = all.size();
        int pending = (int) all.stream().filter(SpamReportRecord::isPending).count();
        int resolved = total - pending;
        int lockedReported = (int) all.stream()
                .filter(SpamReportRecord::isReportedUserLocked)
                .map(SpamReportRecord::getReportedUserId)
                .distinct()
                .count();

        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, AppTheme.SPACING_MD, 0));
        cardsGrid.setOpaque(false);

        totalReportsCard = new StatCard(
                "Tổng số báo cáo",
                total,
                "Bản ghi trong spam_reports",
                AppTheme.PRIMARY,
                AppTheme.PRIMARY_SOFT_BG
        );
        pendingReportsCard = new StatCard(
                "Đang chờ xử lý",
                pending,
                "status = PENDING",
                AppTheme.WARNING,
                AppTheme.WARNING_BG
        );
        resolvedReportsCard = new StatCard(
                "Đã xử lý",
                resolved,
                "status = RESOLVED",
                AppTheme.SUCCESS,
                AppTheme.SUCCESS_BG
        );
        lockedOffendersCard = new StatCard(
                "TK bị báo cáo đã khóa",
                lockedReported,
                "Tài khoản đang bị khóa",
                AppTheme.DANGER,
                AppTheme.DANGER_BG
        );

        cardsGrid.add(totalReportsCard);
        cardsGrid.add(pendingReportsCard);
        cardsGrid.add(resolvedReportsCard);
        cardsGrid.add(lockedOffendersCard);
        return cardsGrid;
    }

    private JPanel createMainTableCard() {
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(AppTheme.SURFACE_CARD);
        tableCard.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1, true));

        JPanel topBarWrapper = new JPanel(new BorderLayout());
        topBarWrapper.setOpaque(false);
        topBarWrapper.add(createFilterToolbar(), BorderLayout.NORTH);
        topBarWrapper.add(createValidationBanner(), BorderLayout.SOUTH);

        viewportDeck.setBackground(AppTheme.SURFACE_CARD);
        viewportDeck.add(createTableScrollPane(), CARD_TABLE);
        viewportDeck.add(createEmptyResultPanel(), CARD_EMPTY);

        tableCard.add(topBarWrapper, BorderLayout.NORTH);
        tableCard.add(viewportDeck, BorderLayout.CENTER);
        tableCard.add(createSelectionAndFooterBar(), BorderLayout.SOUTH);

        return tableCard;
    }

    private JPanel createFilterToolbar() {
        JPanel toolbar = new JPanel(new GridBagLayout());
        toolbar.setBackground(AppTheme.SURFACE_CARD);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER),
                BorderFactory.createEmptyBorder(10, AppTheme.SPACING_MD, 10, AppTheme.SPACING_MD)
        ));

        reportedUserFilterField = new JTextField();
        reportedUserFilterField.setFont(AppTheme.FONT_BODY);
        reportedUserFilterField.setForeground(AppTheme.TEXT_PRIMARY);
        reportedUserFilterField.setPreferredSize(new Dimension(200, AppTheme.CONTROL_HEIGHT));
        reportedUserFilterField.setMinimumSize(new Dimension(130, AppTheme.CONTROL_HEIGHT));
        reportedUserFilterField.putClientProperty(
                "JTextField.placeholderText",
                "Lọc theo username bị báo cáo..."
        );
        reportedUserFilterField.getDocument().addDocumentListener(new DocumentListener() {
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

        fromDateField = new JTextField();
        fromDateField.setFont(AppTheme.FONT_BODY);
        fromDateField.setPreferredSize(new Dimension(110, AppTheme.CONTROL_HEIGHT));
        fromDateField.setMinimumSize(new Dimension(110, AppTheme.CONTROL_HEIGHT));
        fromDateField.putClientProperty("JTextField.placeholderText", "2026-01-01");
        fromDateField.setToolTipText("Từ ngày báo cáo (YYYY-MM-DD, ví dụ: 2026-03-01)");
        fromDateField.addActionListener(e -> applyFiltersAndSort());

        toDateField = new JTextField();
        toDateField.setFont(AppTheme.FONT_BODY);
        toDateField.setPreferredSize(new Dimension(110, AppTheme.CONTROL_HEIGHT));
        toDateField.setMinimumSize(new Dimension(110, AppTheme.CONTROL_HEIGHT));
        toDateField.putClientProperty("JTextField.placeholderText", "2026-12-31");
        toDateField.setToolTipText("Đến ngày báo cáo (YYYY-MM-DD, ví dụ: 2026-05-31)");
        toDateField.addActionListener(e -> applyFiltersAndSort());

        JButton applyDateBtn = createSecondaryButton("Lọc ngày", 88);
        applyDateBtn.addActionListener(e -> applyFiltersAndSort());

        sortCombo = new JComboBox<>(SORT_OPTIONS);
        sortCombo.setFont(AppTheme.FONT_BODY);
        sortCombo.setPreferredSize(new Dimension(142, AppTheme.CONTROL_HEIGHT));
        sortCombo.setMinimumSize(new Dimension(142, AppTheme.CONTROL_HEIGHT));
        sortCombo.addActionListener(e -> applyFiltersAndSort());

        JButton clearSelectionBtn = createSecondaryButton("Bỏ chọn", 84);
        clearSelectionBtn.setToolTipText("Bỏ chọn dòng hiện tại để kiểm tra trạng thái chưa chọn (no-selection)");
        clearSelectionBtn.addActionListener(e -> reportTable.clearSelection());

        JButton resetBtn = createSecondaryButton("Làm mới", 88);
        resetBtn.addActionListener(e -> resetFilters());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;

        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 8);
        toolbar.add(reportedUserFilterField, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 2, 0, 4);
        toolbar.add(createToolbarLabel("Từ ngày:"), gbc);

        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 6);
        toolbar.add(fromDateField, gbc);

        gbc.gridx = 3;
        gbc.insets = new Insets(0, 2, 0, 4);
        toolbar.add(createToolbarLabel("Đến ngày:"), gbc);

        gbc.gridx = 4;
        gbc.insets = new Insets(0, 0, 0, 6);
        toolbar.add(toDateField, gbc);

        gbc.gridx = 5;
        gbc.insets = new Insets(0, 0, 0, 8);
        toolbar.add(applyDateBtn, gbc);

        gbc.gridx = 6;
        gbc.insets = new Insets(0, 2, 0, 4);
        toolbar.add(createToolbarLabel("Sắp xếp:"), gbc);

        gbc.gridx = 7;
        gbc.insets = new Insets(0, 0, 0, 6);
        toolbar.add(sortCombo, gbc);

        gbc.gridx = 8;
        gbc.insets = new Insets(0, 0, 0, 6);
        toolbar.add(clearSelectionBtn, gbc);

        gbc.gridx = 9;
        gbc.insets = new Insets(0, 0, 0, 0);
        toolbar.add(resetBtn, gbc);

        return toolbar;
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
        AppTheme.configureSharedTable(reportTable);

        DefaultTableCellRenderer centerRenderer = createPaddedRenderer(SwingConstants.CENTER, AppTheme.FONT_BODY, AppTheme.TEXT_SECONDARY);
        DefaultTableCellRenderer reporterRenderer = createPaddedRenderer(SwingConstants.LEFT, AppTheme.FONT_BODY, AppTheme.TEXT_PRIMARY);
        DefaultTableCellRenderer reportedRenderer = createPaddedRenderer(SwingConstants.LEFT, AppTheme.FONT_BODY_BOLD, AppTheme.TEXT_PRIMARY);
        DefaultTableCellRenderer reasonRenderer = createPaddedRenderer(SwingConstants.LEFT, AppTheme.FONT_BODY, AppTheme.TEXT_SECONDARY);

        TableColumnModel cm = reportTable.getColumnModel();
        cm.getColumn(0).setCellRenderer(centerRenderer);
        cm.getColumn(0).setMaxWidth(42);
        cm.getColumn(0).setMinWidth(38);

        cm.getColumn(1).setCellRenderer(centerRenderer);
        cm.getColumn(1).setMinWidth(150);
        cm.getColumn(1).setPreferredWidth(156);

        cm.getColumn(2).setCellRenderer(reporterRenderer);
        cm.getColumn(2).setPreferredWidth(175);

        cm.getColumn(3).setCellRenderer(reportedRenderer);
        cm.getColumn(3).setPreferredWidth(185);

        cm.getColumn(4).setCellRenderer(reasonRenderer);
        cm.getColumn(4).setPreferredWidth(200);

        cm.getColumn(COL_STATUS).setCellRenderer(new SpamStatusAndLockRenderer(tableModel));
        cm.getColumn(COL_STATUS).setMinWidth(156);
        cm.getColumn(COL_STATUS).setPreferredWidth(164);

        cm.getColumn(COL_ACTIONS).setCellRenderer(new SpamActionButtonsRenderer(tableModel));
        cm.getColumn(COL_ACTIONS).setPreferredWidth(172);
        cm.getColumn(COL_ACTIONS).setMinWidth(164);

        reportTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectionStatus();
            }
        });

        reportTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = reportTable.columnAtPoint(e.getPoint());
                if (col == COL_ACTIONS) {
                    reportTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    reportTable.setCursor(Cursor.getDefaultCursor());
                }
            }
        });

        reportTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point pt = e.getPoint();
                int row = reportTable.rowAtPoint(pt);
                int col = reportTable.columnAtPoint(pt);
                if (row >= 0 && col == COL_ACTIONS) {
                    reportTable.setRowSelectionInterval(row, row);
                    Rectangle rect = reportTable.getCellRect(row, col, false);
                    int relX = pt.x - rect.x;
                    if (relX < rect.width * 0.46) {
                        openDetailForRow(row);
                    } else {
                        openLockConfirmForRow(row);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(reportTable);
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

        JLabel title = new JLabel("Không tìm thấy báo cáo spam phù hợp");
        title.setFont(AppTheme.FONT_SECTION_TITLE);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyStateDetailLabel = new JLabel("Không có báo cáo nào khớp với khoảng thời gian hoặc username bị báo cáo.");
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
        reportedUserFilterField.setText("");
        fromDateField.setText("");
        toDateField.setText("");
        sortCombo.setSelectedIndex(0);
        validationBanner.setVisible(false);
        applyFiltersAndSort();
    }

    private void applyFiltersAndSort() {
        String keyword = reportedUserFilterField != null
                ? reportedUserFilterField.getText().trim().toLowerCase(Locale.ROOT)
                : "";
        String fromText = fromDateField != null ? fromDateField.getText().trim() : "";
        String toText = toDateField != null ? toDateField.getText().trim() : "";

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
                        + "\") không đúng định dạng YYYY-MM-DD (ví dụ: 2026-05-31).");
                return;
            }
        }
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            showValidationError("Lỗi khoảng thời gian: 'Từ ngày' (" + fromDate
                    + ") không được lớn hơn 'Đến ngày' (" + toDate + ").");
            return;
        }

        if (validationBanner != null) {
            validationBanner.setVisible(false);
        }

        List<SpamReportRecord> all = userRepository.getSpamReports();
        List<SpamReportRecord> filtered = new ArrayList<>();
        for (SpamReportRecord r : all) {
            if (!keyword.isEmpty()) {
                boolean matchReportedUser = r.getReportedUsername().toLowerCase(Locale.ROOT).contains(keyword);
                boolean matchReportedName = r.getReportedFullName().toLowerCase(Locale.ROOT).contains(keyword);
                if (!matchReportedUser && !matchReportedName) {
                    continue;
                }
            }
            LocalDate reportDate = r.getCreatedAt().toLocalDate();
            if (fromDate != null && reportDate.isBefore(fromDate)) {
                continue;
            }
            if (toDate != null && reportDate.isAfter(toDate)) {
                continue;
            }
            filtered.add(r);
        }

        String sortOption = sortCombo != null ? (String) sortCombo.getSelectedItem() : SORT_OPTIONS[0];
        Comparator<SpamReportRecord> cmp = switch (sortOption != null ? sortOption : SORT_OPTIONS[0]) {
            case "Cũ nhất" -> Comparator.comparing(SpamReportRecord::getCreatedAt)
                    .thenComparingLong(SpamReportRecord::getId);
            case "Username A–Z" -> Comparator.comparing(
                            (SpamReportRecord r) -> r.getReportedUsername().toLowerCase(Locale.ROOT)
                    )
                    .thenComparing(Comparator.comparing(SpamReportRecord::getCreatedAt).reversed());
            case "Username Z–A" -> Comparator.comparing(
                            (SpamReportRecord r) -> r.getReportedUsername().toLowerCase(Locale.ROOT),
                            Comparator.reverseOrder()
                    )
                    .thenComparing(Comparator.comparing(SpamReportRecord::getCreatedAt).reversed());
            default -> Comparator.comparing(SpamReportRecord::getCreatedAt).reversed()
                    .thenComparing(Comparator.comparingLong(SpamReportRecord::getId).reversed());
        };
        filtered.sort(cmp);

        tableModel.setReports(filtered);

        if (filtered.isEmpty()) {
            viewportCardLayout.show(viewportDeck, CARD_EMPTY);
            if (emptyStateDetailLabel != null) {
                emptyStateDetailLabel.setText(
                        "Không tìm thấy báo cáo spam nào khớp với điều kiện lọc hiện tại."
                );
            }
        } else {
            viewportCardLayout.show(viewportDeck, CARD_TABLE);
            reportTable.setRowSelectionInterval(0, 0);
        }

        if (totalReportsCard != null) {
            int total = all.size();
            int pending = (int) all.stream().filter(SpamReportRecord::isPending).count();
            int resolved = total - pending;
            int lockedReported = (int) all.stream()
                    .filter(SpamReportRecord::isReportedUserLocked)
                    .map(SpamReportRecord::getReportedUserId)
                    .distinct()
                    .count();
            totalReportsCard.setCount(total);
            pendingReportsCard.setCount(pending);
            resolvedReportsCard.setCount(resolved);
            lockedOffendersCard.setCount(lockedReported);
        }
        if (recordCountLabel != null) {
            recordCountLabel.setText(
                    "Hiển thị " + filtered.size() + " / " + all.size() + " báo cáo spam (Mô phỏng in-memory)"
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
        int row = reportTable.getSelectedRow();
        if (row < 0 || row >= tableModel.getRowCount()) {
            selectionStatusLabel.setText(
                    "Trạng thái chọn: Chưa chọn báo cáo nào (No-selection) — Chọn một dòng để xem bản chụp tin nhắn hoặc khóa tài khoản."
            );
            selectionStatusLabel.setForeground(AppTheme.TEXT_SECONDARY);
        } else {
            SpamReportRecord r = tableModel.getReportAt(row);
            selectionStatusLabel.setText(
                    "Đang chọn báo cáo #" + r.getId()
                            + ": @" + r.getReporterUsername() + " báo cáo @" + r.getReportedUsername()
                            + "  •  TT: " + r.getStatusDisplay()
            );
            selectionStatusLabel.setForeground(AppTheme.PRIMARY);
        }
    }

    private void openDetailForSelectedRow() {
        int row = reportTable.getSelectedRow();
        if (row < 0 || row >= tableModel.getRowCount()) {
            selectionStatusLabel.setText(
                    "Lưu ý: Vui lòng chọn một báo cáo spam trong bảng trước khi xem chi tiết bản chụp tin nhắn."
            );
            selectionStatusLabel.setForeground(AppTheme.WARNING);
            return;
        }
        openDetailForRow(row);
    }

    private void openDetailForRow(int row) {
        SpamReportRecord report = tableModel.getReportAt(row);
        if (report == null) {
            return;
        }
        User reportedUser = userRepository.findUserById(report.getReportedUserId()).orElse(null);
        Window owner = SwingUtilities.getWindowAncestor(this);
        SpamReportDetailDialog dialog = new SpamReportDetailDialog(
                owner,
                report,
                reportedUser,
                () -> openLockConfirmForReport(report)
        );
        dialog.setVisible(true);
    }

    private void openLockConfirmForSelectedRow() {
        int row = reportTable.getSelectedRow();
        if (row < 0 || row >= tableModel.getRowCount()) {
            selectionStatusLabel.setText(
                    "Lưu ý: Vui lòng chọn một báo cáo spam trong bảng trước khi thực hiện khóa/mở khóa tài khoản bị báo cáo."
            );
            selectionStatusLabel.setForeground(AppTheme.WARNING);
            return;
        }
        openLockConfirmForRow(row);
    }

    private void openLockConfirmForRow(int row) {
        SpamReportRecord report = tableModel.getReportAt(row);
        if (report != null) {
            openLockConfirmForReport(report);
        }
    }

    private void openLockConfirmForReport(SpamReportRecord report) {
        User reportedUser = userRepository.findUserById(report.getReportedUserId()).orElse(null);
        if (reportedUser == null) {
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (ConfirmActionDialog.showSpamReportLockConfirm(owner, reportedUser, report)) {
            userRepository.toggleLockReportedUserFromSpamReport(report.getId());
        }
    }

    private JLabel createToolbarLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.FONT_CAPTION_BOLD);
        label.setForeground(AppTheme.TEXT_SECONDARY);
        return label;
    }

    private JButton createPrimaryOutlineButton(String text, int width) {
        return AppTheme.createPrimaryOutlineButton(text, width);
    }

    private JButton createDangerActionButton(String text, int width) {
        return AppTheme.createAccentButton(text, width, AppTheme.DANGER);
    }

    private JButton createSecondaryButton(String text, int width) {
        return AppTheme.createSecondaryButton(text, width);
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

    private static final class SpamStatusAndLockRenderer extends JPanel implements javax.swing.table.TableCellRenderer {

        private final SpamReportTableModel model;
        private boolean pending = false;
        private boolean reportedLocked = false;
        private boolean rowSelected = false;

        private SpamStatusAndLockRenderer(SpamReportTableModel model) {
            this.model = model;
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            SpamReportRecord r = model.getReportAt(row);
            if (r != null) {
                this.pending = r.isPending();
                this.reportedLocked = r.isReportedUserLocked();
            }
            this.rowSelected = isSelected;
            setBackground(isSelected ? AppTheme.PRIMARY_SOFT_BG : AppTheme.SURFACE_CARD);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            String statusText = pending ? "Chờ xử lý" : "Đã xử lý";
            Color statusBg = pending ? AppTheme.WARNING_BG : AppTheme.SUCCESS_BG;
            Color statusFg = pending ? AppTheme.WARNING : AppTheme.SUCCESS;

            String lockText = reportedLocked ? "TK Đã khóa" : "TK Mở";
            Color lockBg = reportedLocked ? AppTheme.DANGER_BG : AppTheme.NEUTRAL_BG;
            Color lockFg = reportedLocked ? AppTheme.DANGER : AppTheme.TEXT_SECONDARY;

            g2.setFont(AppTheme.FONT_CAPTION_BOLD);
            FontMetrics fm = g2.getFontMetrics();
            int pillH = 24;
            int gap = 5;
            int w1 = fm.stringWidth(statusText) + 14;
            int w2 = fm.stringWidth(lockText) + 14;
            int totalW = w1 + gap + w2;
            int startX = Math.max(4, (getWidth() - totalW) / 2);
            int y = (getHeight() - pillH) / 2;
            int textY = y + ((pillH - fm.getHeight()) / 2) + fm.getAscent();

            g2.setColor(statusBg);
            g2.fillRoundRect(startX, y, w1, pillH, 12, 12);
            g2.setColor(statusFg);
            g2.drawString(statusText, startX + 7, textY);

            int x2 = startX + w1 + gap;
            g2.setColor(lockBg);
            g2.fillRoundRect(x2, y, w2, pillH, 12, 12);
            g2.setColor(lockFg);
            g2.drawString(lockText, x2 + 7, textY);

            if (!rowSelected) {
                g2.setColor(AppTheme.BORDER);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
            g2.dispose();
        }
    }

    private static final class SpamActionButtonsRenderer extends JPanel implements javax.swing.table.TableCellRenderer {

        private final SpamReportTableModel model;
        private boolean reportedLocked = false;
        private boolean rowSelected = false;

        private SpamActionButtonsRenderer(SpamReportTableModel model) {
            this.model = model;
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            SpamReportRecord r = model.getReportAt(row);
            this.reportedLocked = r != null && r.isReportedUserLocked();
            this.rowSelected = isSelected;
            setBackground(isSelected ? AppTheme.PRIMARY_SOFT_BG : AppTheme.SURFACE_CARD);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setFont(AppTheme.FONT_CAPTION_BOLD);
            FontMetrics fm = g2.getFontMetrics();

            String leftText = "Chi tiết";
            String rightText = reportedLocked ? "Mở khóa TK" : "Khóa TK";
            int btnH = 26;
            int gap = 6;
            int leftW = fm.stringWidth(leftText) + 16;
            int rightW = fm.stringWidth(rightText) + 16;
            int totalW = leftW + gap + rightW;
            int startX = Math.max(4, (getWidth() - totalW) / 2);
            int y = (getHeight() - btnH) / 2;
            int textY = y + ((btnH - fm.getHeight()) / 2) + fm.getAscent();

            g2.setColor(AppTheme.SURFACE_CARD);
            g2.fillRoundRect(startX, y, leftW, btnH, 6, 6);
            g2.setColor(AppTheme.BORDER);
            g2.drawRoundRect(startX, y, leftW - 1, btnH - 1, 6, 6);
            g2.setColor(AppTheme.PRIMARY);
            g2.drawString(leftText, startX + 8, textY);

            int rightX = startX + leftW + gap;
            Color rightBg = reportedLocked ? AppTheme.SUCCESS_BG : AppTheme.DANGER_BG;
            Color rightFg = reportedLocked ? AppTheme.SUCCESS : AppTheme.DANGER;
            g2.setColor(rightBg);
            g2.fillRoundRect(rightX, y, rightW, btnH, 6, 6);
            g2.setColor(rightFg);
            g2.drawString(rightText, rightX + 8, textY);

            if (!rowSelected) {
                g2.setColor(AppTheme.BORDER);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
            g2.dispose();
        }
    }

    private static final class SpamReportTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {
                "STT",
                "Thời gian báo cáo",
                "Người gửi báo cáo",
                "Tài khoản bị báo cáo",
                "Lý do báo cáo",
                "Trạng thái • TK",
                "Thao tác"
        };

        private final List<SpamReportRecord> reports;

        private SpamReportTableModel(List<SpamReportRecord> initialReports) {
            this.reports = new ArrayList<>(initialReports);
        }

        public void setReports(List<SpamReportRecord> updated) {
            this.reports.clear();
            if (updated != null) {
                this.reports.addAll(updated);
            }
            fireTableDataChanged();
        }

        public SpamReportRecord getReportAt(int rowIndex) {
            if (rowIndex < 0 || rowIndex >= reports.size()) {
                return null;
            }
            return reports.get(rowIndex);
        }

        @Override
        public int getRowCount() {
            return reports.size();
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
            SpamReportRecord r = reports.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> rowIndex + 1;
                case 1 -> r.getFormattedCreatedAt();
                case 2 -> r.getReporterDisplay();
                case 3 -> r.getReportedDisplay();
                case 4 -> r.getReason();
                case 5 -> r.getStatusDisplay();
                case 6 -> "Chi tiết | Khóa TK";
                default -> "";
            };
        }
    }
}
