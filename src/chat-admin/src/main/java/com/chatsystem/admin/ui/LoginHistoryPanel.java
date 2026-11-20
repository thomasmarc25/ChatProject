package com.chatsystem.admin.ui;

import com.chatsystem.admin.components.StatCard;
import com.chatsystem.admin.data.MockUserRepository;
import com.chatsystem.admin.model.LoginRecord;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * System-wide Login History screen panel (Requirement 2 / Step F - A).
 * Displays chronological authentication records ({@code login_history}: login time, username, full name,
 * logout time, IP, and session notes) and explicitly distinguishes authentication events from
 * client application-open statistics ({@code app_open_events}).
 */
public class LoginHistoryPanel extends JPanel {

    private static final String CARD_TABLE = "TABLE";
    private static final String CARD_EMPTY = "EMPTY";

    private static final String[] SORT_OPTIONS = {
            "Mới nhất trước",
            "Cũ nhất trước"
    };

    private final MockUserRepository userRepository;
    private final SystemLoginTableModel tableModel;
    private final JTable loginTable;
    private final CardLayout viewportCardLayout;
    private final JPanel viewportDeck;

    private StatCard totalLoginsCard;
    private StatCard uniqueUsersCard;
    private StatCard activeSessionsCard;
    private StatCard displayedCountCard;

    private JTextField searchField;
    private JTextField fromDateField;
    private JTextField toDateField;
    private JComboBox<String> sortCombo;
    private JPanel validationBanner;
    private JLabel validationMessageLabel;
    private JLabel selectionStatusLabel;
    private JLabel recordCountLabel;
    private JLabel emptyStateDetailLabel;

    public LoginHistoryPanel(MockUserRepository userRepository) {
        super(new BorderLayout(0, AppTheme.SPACING_MD - 2));
        this.userRepository = userRepository;
        this.tableModel = new SystemLoginTableModel(userRepository.getAllLoginHistory());
        this.loginTable = new JTable(tableModel);
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
        topSection.add(createPageIntroAndDistinctionBanner(), BorderLayout.NORTH);
        topSection.add(createSummaryCardsRow(), BorderLayout.CENTER);

        add(topSection, BorderLayout.NORTH);
        add(createMainTableCard(), BorderLayout.CENTER);

        this.userRepository.addDataChangeListener(this::applyFiltersAndSort);
        applyFiltersAndSort();
    }

    private JPanel createPageIntroAndDistinctionBanner() {
        JPanel wrapper = new JPanel(new BorderLayout(0, AppTheme.SPACING_SM));
        wrapper.setOpaque(false);

        JPanel introRow = new JPanel(new BorderLayout(AppTheme.SPACING_MD, 0));
        introRow.setOpaque(false);

        JPanel textGroup = new JPanel();
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));
        textGroup.setOpaque(false);

        JLabel titleLabel = new JLabel("Lịch sử đăng nhập toàn hệ thống");
        titleLabel.setFont(AppTheme.FONT_PAGE_TITLE);
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(
                "Theo dõi các phiên đăng nhập xác thực (bảng login_history) theo thứ tự thời gian thực tế"
        );
        subtitleLabel.setFont(AppTheme.FONT_BODY);
        subtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textGroup.add(titleLabel);
        textGroup.add(Box.createVerticalStrut(2));
        textGroup.add(subtitleLabel);

        JLabel schemaBadge = new JLabel("BẢNG LOGIN_HISTORY • KHÁC VỚI APP_OPEN_EVENTS");
        schemaBadge.setFont(AppTheme.FONT_CAPTION_BOLD);
        schemaBadge.setForeground(AppTheme.PRIMARY);
        schemaBadge.setOpaque(true);
        schemaBadge.setBackground(AppTheme.PRIMARY_SOFT_BG);
        schemaBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.PRIMARY_BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        JPanel badgeBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        badgeBox.setOpaque(false);
        badgeBox.add(schemaBadge);

        introRow.add(textGroup, BorderLayout.WEST);
        introRow.add(badgeBox, BorderLayout.EAST);

        // Callout distinguishing login_history from app_open_events (two crisp lines that never clip)
        JPanel calloutPanel = new JPanel();
        calloutPanel.setLayout(new BoxLayout(calloutPanel, BoxLayout.Y_AXIS));
        calloutPanel.setBackground(AppTheme.PRIMARY_SOFT_BG);
        calloutPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));

        JLabel calloutLine1 = new JLabel(
                "Phân biệt với Thống kê mở ứng dụng (App Opens): Màn hình này ghi nhận từng lần đăng nhập xác thực (bảng login_history)."
        );
        calloutLine1.setFont(AppTheme.FONT_CAPTION_BOLD);
        calloutLine1.setForeground(AppTheme.PRIMARY);
        calloutLine1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel calloutLine2 = new JLabel(
                "Một lần mở app (launch_id trong app_open_events) có thể có nhiều lần đăng nhập/đăng xuất (VD: @nguyenvanan ngày 05/03/2026 đăng nhập 3 lần trong 1 lần mở app)."
        );
        calloutLine2.setFont(AppTheme.FONT_CAPTION);
        calloutLine2.setForeground(AppTheme.TEXT_PRIMARY);
        calloutLine2.setAlignmentX(Component.LEFT_ALIGNMENT);

        calloutPanel.add(calloutLine1);
        calloutPanel.add(Box.createVerticalStrut(2));
        calloutPanel.add(calloutLine2);

        wrapper.add(introRow, BorderLayout.NORTH);
        wrapper.add(calloutPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createSummaryCardsRow() {
        List<LoginRecord> all = userRepository.getAllLoginHistory();
        int totalLogins = all.size();
        int uniqueUsers = (int) all.stream().map(LoginRecord::getUserId).distinct().count();
        int openSessions = (int) all.stream().filter(r -> r.getLogoutAt() == null).count();

        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, AppTheme.SPACING_MD, 0));
        cardsGrid.setOpaque(false);

        totalLoginsCard = new StatCard(
                "Tổng lượt đăng nhập",
                totalLogins,
                "Bản ghi trong login_history",
                AppTheme.PRIMARY,
                AppTheme.PRIMARY_SOFT_BG
        );
        uniqueUsersCard = new StatCard(
                "Tài khoản đã đăng nhập",
                uniqueUsers,
                "Bao gồm cả TK lịch sử",
                AppTheme.SUCCESS,
                AppTheme.SUCCESS_BG
        );
        activeSessionsCard = new StatCard(
                "Phiên xác thực đang mở",
                openSessions,
                "Chưa ghi nhận logout_at",
                AppTheme.WARNING,
                AppTheme.WARNING_BG
        );
        displayedCountCard = new StatCard(
                "Kết quả đang hiển thị",
                totalLogins,
                "Theo bộ lọc hiện tại",
                AppTheme.TEXT_SECONDARY,
                AppTheme.NEUTRAL_BG
        );

        cardsGrid.add(totalLoginsCard);
        cardsGrid.add(uniqueUsersCard);
        cardsGrid.add(activeSessionsCard);
        cardsGrid.add(displayedCountCard);
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

        searchField = new JTextField();
        searchField.setFont(AppTheme.FONT_BODY);
        searchField.setForeground(AppTheme.TEXT_PRIMARY);
        searchField.setPreferredSize(new Dimension(180, AppTheme.CONTROL_HEIGHT));
        searchField.setMinimumSize(new Dimension(120, AppTheme.CONTROL_HEIGHT));
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo username hoặc họ tên...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
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
        fromDateField.setPreferredSize(new Dimension(112, AppTheme.CONTROL_HEIGHT));
        fromDateField.setMinimumSize(new Dimension(112, AppTheme.CONTROL_HEIGHT));
        fromDateField.putClientProperty("JTextField.placeholderText", "2026-01-01");
        fromDateField.setToolTipText("Từ ngày (định dạng YYYY-MM-DD, ví dụ: 2026-03-01)");
        fromDateField.addActionListener(e -> applyFiltersAndSort());

        toDateField = new JTextField();
        toDateField.setFont(AppTheme.FONT_BODY);
        toDateField.setPreferredSize(new Dimension(112, AppTheme.CONTROL_HEIGHT));
        toDateField.setMinimumSize(new Dimension(112, AppTheme.CONTROL_HEIGHT));
        toDateField.putClientProperty("JTextField.placeholderText", "2026-12-31");
        toDateField.setToolTipText("Đến ngày (định dạng YYYY-MM-DD, ví dụ: 2026-03-31)");
        toDateField.addActionListener(e -> applyFiltersAndSort());

        JButton filterDateBtn = createSecondaryButton("Lọc ngày", 88);
        filterDateBtn.addActionListener(e -> applyFiltersAndSort());

        sortCombo = new JComboBox<>(SORT_OPTIONS);
        sortCombo.setFont(AppTheme.FONT_BODY);
        sortCombo.setPreferredSize(new Dimension(150, AppTheme.CONTROL_HEIGHT));
        sortCombo.setMinimumSize(new Dimension(150, AppTheme.CONTROL_HEIGHT));
        sortCombo.addActionListener(e -> applyFiltersAndSort());

        JButton clearSelectionBtn = createSecondaryButton("Bỏ chọn", 84);
        clearSelectionBtn.setToolTipText("Bỏ chọn dòng hiện tại để kiểm tra trạng thái chưa chọn (no-selection)");
        clearSelectionBtn.addActionListener(e -> loginTable.clearSelection());

        JButton resetBtn = createSecondaryButton("Làm mới", 88);
        resetBtn.addActionListener(e -> resetFilters());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;

        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 8);
        toolbar.add(searchField, gbc);

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
        toolbar.add(filterDateBtn, gbc);

        gbc.gridx = 6;
        gbc.insets = new Insets(0, 2, 0, 4);
        toolbar.add(createToolbarLabel("Thứ tự:"), gbc);

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
        AppTheme.configureSharedTable(loginTable);

        DefaultTableCellRenderer centerRenderer = createPaddedRenderer(SwingConstants.CENTER, AppTheme.FONT_BODY, AppTheme.TEXT_SECONDARY);
        DefaultTableCellRenderer timeRenderer = createPaddedRenderer(SwingConstants.CENTER, AppTheme.FONT_BODY_BOLD, AppTheme.TEXT_PRIMARY);
        DefaultTableCellRenderer userRenderer = createPaddedRenderer(SwingConstants.LEFT, AppTheme.FONT_BODY, AppTheme.PRIMARY);
        DefaultTableCellRenderer nameRenderer = createPaddedRenderer(SwingConstants.LEFT, AppTheme.FONT_BODY_BOLD, AppTheme.TEXT_PRIMARY);
        DefaultTableCellRenderer noteRenderer = createPaddedRenderer(SwingConstants.LEFT, AppTheme.FONT_CAPTION, AppTheme.TEXT_SECONDARY);

        TableColumnModel cm = loginTable.getColumnModel();
        cm.getColumn(0).setCellRenderer(centerRenderer);
        cm.getColumn(0).setMaxWidth(42);
        cm.getColumn(0).setMinWidth(38);

        cm.getColumn(1).setCellRenderer(timeRenderer);
        cm.getColumn(1).setMinWidth(150);
        cm.getColumn(1).setPreferredWidth(156);

        cm.getColumn(2).setCellRenderer(userRenderer);
        cm.getColumn(2).setMinWidth(122);
        cm.getColumn(2).setPreferredWidth(132);

        cm.getColumn(3).setCellRenderer(nameRenderer);
        cm.getColumn(3).setMinWidth(148);
        cm.getColumn(3).setPreferredWidth(175);

        cm.getColumn(4).setCellRenderer(centerRenderer);
        cm.getColumn(4).setMinWidth(150);
        cm.getColumn(4).setPreferredWidth(156);

        cm.getColumn(5).setCellRenderer(centerRenderer);
        cm.getColumn(5).setMinWidth(92);
        cm.getColumn(5).setPreferredWidth(98);

        cm.getColumn(6).setCellRenderer(new SessionStatusCellRenderer());
        cm.getColumn(6).setMinWidth(114);
        cm.getColumn(6).setPreferredWidth(120);

        cm.getColumn(7).setCellRenderer(noteRenderer);
        cm.getColumn(7).setPreferredWidth(190);

        loginTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectionStatus();
            }
        });

        JScrollPane scrollPane = new JScrollPane(loginTable);
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

        JLabel title = new JLabel("Không tìm thấy bản ghi đăng nhập phù hợp");
        title.setFont(AppTheme.FONT_SECTION_TITLE);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyStateDetailLabel = new JLabel("Không có phiên đăng nhập nào khớp với bộ lọc hiện tại.");
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
        searchField.setText("");
        fromDateField.setText("");
        toDateField.setText("");
        sortCombo.setSelectedIndex(0);
        validationBanner.setVisible(false);
        applyFiltersAndSort();
    }

    private void applyFiltersAndSort() {
        String keyword = searchField != null ? searchField.getText().trim().toLowerCase(Locale.ROOT) : "";
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
                        + "\") không đúng định dạng YYYY-MM-DD (ví dụ: 2026-03-31).");
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

        List<LoginRecord> all = userRepository.getAllLoginHistory();
        List<LoginRecord> filtered = new ArrayList<>();
        for (LoginRecord r : all) {
            if (!keyword.isEmpty()) {
                boolean matchUser = r.getUsername().toLowerCase(Locale.ROOT).contains(keyword);
                boolean matchName = r.getFullName().toLowerCase(Locale.ROOT).contains(keyword);
                if (!matchUser && !matchName) {
                    continue;
                }
            }
            LocalDate loginDate = r.getLoginAt().toLocalDate();
            if (fromDate != null && loginDate.isBefore(fromDate)) {
                continue;
            }
            if (toDate != null && loginDate.isAfter(toDate)) {
                continue;
            }
            filtered.add(r);
        }

        String sortOption = sortCombo != null ? (String) sortCombo.getSelectedItem() : SORT_OPTIONS[0];
        Comparator<LoginRecord> cmp = Comparator.comparing(LoginRecord::getLoginAt)
                .thenComparingLong(LoginRecord::getId);
        if ("Mới nhất trước".equals(sortOption)) {
            cmp = cmp.reversed();
        }
        filtered.sort(cmp);

        tableModel.setRecords(filtered);

        if (filtered.isEmpty()) {
            viewportCardLayout.show(viewportDeck, CARD_EMPTY);
            if (emptyStateDetailLabel != null) {
                emptyStateDetailLabel.setText(
                        "Không tìm thấy bản ghi đăng nhập nào khớp với điều kiện lọc hiện tại."
                );
            }
        } else {
            viewportCardLayout.show(viewportDeck, CARD_TABLE);
            loginTable.setRowSelectionInterval(0, 0);
        }

        if (totalLoginsCard != null) {
            totalLoginsCard.setCount(all.size());
            uniqueUsersCard.setCount((int) all.stream().map(LoginRecord::getUserId).distinct().count());
            activeSessionsCard.setCount((int) all.stream().filter(r -> r.getLogoutAt() == null).count());
            displayedCountCard.setCount(filtered.size());
        }
        if (recordCountLabel != null) {
            recordCountLabel.setText(
                    "Hiển thị " + filtered.size() + " / " + all.size() + " phiên (Asia/Ho_Chi_Minh)"
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
        int row = loginTable.getSelectedRow();
        if (row < 0 || row >= tableModel.getRowCount()) {
            selectionStatusLabel.setText(
                    "Trạng thái chọn: Chưa chọn dòng nào (No-selection) — Nhấp vào một bản ghi để xem chi tiết phiên xác thực."
            );
            selectionStatusLabel.setForeground(AppTheme.TEXT_SECONDARY);
        } else {
            LoginRecord r = tableModel.getRecordAt(row);
            selectionStatusLabel.setText(
                    "Đang chọn phiên #" + r.getId() + ": " + r.getFullName() + " (@" + r.getUsername()
                            + ")  •  Đăng nhập: " + r.getFormattedLoginAt()
                            + "  •  IP: " + r.getIpAddress()
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
                    label.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                } else {
                    label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 6));
                }
                return label;
            }
        };
    }

    private static final class SessionStatusCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {

        private boolean openSession;
        private boolean rowSelected;

        private SessionStatusCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            this.openSession = "Đang mở".equals(value);
            this.rowSelected = isSelected;
            setBackground(isSelected ? AppTheme.PRIMARY_SOFT_BG : AppTheme.SURFACE_CARD);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            String text = openSession ? "Phiên đang mở" : "Đã kết thúc";
            Color bg = openSession ? AppTheme.SUCCESS_BG : AppTheme.NEUTRAL_BG;
            Color fg = openSession ? AppTheme.SUCCESS : AppTheme.TEXT_SECONDARY;

            g2.setFont(AppTheme.FONT_CAPTION_BOLD);
            FontMetrics fm = g2.getFontMetrics();
            int pillW = fm.stringWidth(text) + 18;
            int pillH = 24;
            int x = Math.max(4, (getWidth() - pillW) / 2);
            int y = (getHeight() - pillH) / 2;

            g2.setColor(bg);
            g2.fillRoundRect(x, y, pillW, pillH, 12, 12);
            g2.setColor(fg);
            int textY = y + ((pillH - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(text, x + 9, textY);

            if (!rowSelected) {
                g2.setColor(AppTheme.BORDER);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
            g2.dispose();
        }
    }

    private static final class SystemLoginTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {
                "STT",
                "Thời gian đăng nhập",
                "Tên đăng nhập",
                "Họ và tên",
                "Thời gian đăng xuất",
                "Địa chỉ IP",
                "Trạng thái phiên",
                "Ghi chú nghiệp vụ"
        };

        private final List<LoginRecord> records;

        private SystemLoginTableModel(List<LoginRecord> initialRecords) {
            this.records = new ArrayList<>(initialRecords);
        }

        public void setRecords(List<LoginRecord> updated) {
            this.records.clear();
            if (updated != null) {
                this.records.addAll(updated);
            }
            fireTableDataChanged();
        }

        public LoginRecord getRecordAt(int rowIndex) {
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
            LoginRecord r = records.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> rowIndex + 1;
                case 1 -> r.getFormattedLoginAt();
                case 2 -> "@" + r.getUsername();
                case 3 -> r.getFullName();
                case 4 -> r.getFormattedLogoutAt();
                case 5 -> r.getIpAddress();
                case 6 -> r.getLogoutAt() == null ? "Đang mở" : "Đã kết thúc";
                case 7 -> r.getSessionNote();
                default -> "";
            };
        }
    }
}
