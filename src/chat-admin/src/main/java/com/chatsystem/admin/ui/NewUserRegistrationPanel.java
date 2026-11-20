package com.chatsystem.admin.ui;

import com.chatsystem.admin.components.StatCard;
import com.chatsystem.admin.data.MockUserRepository;
import com.chatsystem.admin.model.RegistrationRecord;
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
 * Newly Registered Users screen panel (Requirement 5 / Step G - A).
 * Displays start/end date controls with validation, name filter, sorting by name/creation time,
 * and a table containing full name, username, and account creation time (retaining historical
 * soft-deleted accounts per Rule 8 and {@code QUERY 6} in {@code script/03_admin_queries.sql}).
 */
public class NewUserRegistrationPanel extends JPanel {

    private static final String CARD_TABLE = "TABLE";
    private static final String CARD_EMPTY = "EMPTY";

    private static final String[] SORT_OPTIONS = {
            "Ngày tạo: Cũ nhất",
            "Ngày tạo: Mới nhất",
            "Họ tên: A–Z",
            "Họ tên: Z–A"
    };

    private final MockUserRepository userRepository;
    private final RegistrationTableModel tableModel;
    private final JTable regTable;
    private final CardLayout viewportCardLayout;
    private final JPanel viewportDeck;

    private StatCard totalRegistrationsCard;
    private StatCard matchedRangeCard;
    private StatCard activeAccountsCard;
    private StatCard historicalDeletedCard;

    private JTextField nameFilterField;
    private JTextField fromDateField;
    private JTextField toDateField;
    private JComboBox<String> sortCombo;
    private JPanel validationBanner;
    private JLabel validationMessageLabel;
    private JLabel selectionStatusLabel;
    private JLabel recordCountLabel;
    private JLabel emptyStateDetailLabel;

    public NewUserRegistrationPanel(MockUserRepository userRepository) {
        super(new BorderLayout(0, AppTheme.SPACING_MD));
        this.userRepository = userRepository;
        this.tableModel = new RegistrationTableModel(userRepository.getRegistrationRecords());
        this.regTable = new JTable(tableModel);
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

        JLabel titleLabel = new JLabel("Người dùng đăng ký mới");
        titleLabel.setFont(AppTheme.FONT_PAGE_TITLE);
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(
                "Tra cứu danh sách tài khoản đăng ký mới theo khoảng thời gian, lọc theo tên và sắp xếp (bao gồm cả tài khoản lịch sử đã xóa mềm)"
        );
        subtitleLabel.setFont(AppTheme.FONT_BODY);
        subtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textGroup.add(titleLabel);
        textGroup.add(Box.createVerticalStrut(4));
        textGroup.add(subtitleLabel);

        JPanel rightPresetGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SPACING_SM, 4));
        rightPresetGroup.setOpaque(false);

        JButton febPresetBtn = createPrimaryOutlineButton("Mẫu Tháng 02/2026 (QUERY 6)", 202);
        febPresetBtn.setToolTipText("Áp dụng nhanh khoảng ngày 2026-02-01 đến 2026-02-28 khớp với Test Case 6 trong SQL");
        febPresetBtn.addActionListener(e -> {
            fromDateField.setText("2026-02-01");
            toDateField.setText("2026-02-28");
            applyFiltersAndSort();
        });

        JButton allYearBtn = createSecondaryButton("Cả năm 2026", 112);
        allYearBtn.addActionListener(e -> {
            fromDateField.setText("2026-01-01");
            toDateField.setText("2026-12-31");
            applyFiltersAndSort();
        });

        rightPresetGroup.add(febPresetBtn);
        rightPresetGroup.add(allYearBtn);

        introPanel.add(textGroup, BorderLayout.WEST);
        introPanel.add(rightPresetGroup, BorderLayout.EAST);
        return introPanel;
    }

    private JPanel createSummaryCardsRow() {
        List<RegistrationRecord> all = userRepository.getRegistrationRecords();
        int total = all.size();
        int active = (int) all.stream().filter(r -> !r.isSoftDeleted() && !r.isLocked()).count();
        int softDeleted = (int) all.stream().filter(RegistrationRecord::isSoftDeleted).count();

        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, AppTheme.SPACING_MD, 0));
        cardsGrid.setOpaque(false);

        totalRegistrationsCard = new StatCard(
                "Tổng lượt đăng ký",
                total,
                "Tài khoản USER trong lịch sử",
                AppTheme.PRIMARY,
                AppTheme.PRIMARY_SOFT_BG
        );
        matchedRangeCard = new StatCard(
                "Trong khoảng thời gian",
                total,
                "Khớp bộ lọc ngày & tên",
                AppTheme.SUCCESS,
                AppTheme.SUCCESS_BG
        );
        activeAccountsCard = new StatCard(
                "Tài khoản đang hoạt động",
                active,
                "Trong số kết quả khớp",
                AppTheme.TEXT_SECONDARY,
                AppTheme.NEUTRAL_BG
        );
        historicalDeletedCard = new StatCard(
                "TK xóa mềm (lưu lịch sử)",
                softDeleted,
                "Bảo toàn thống kê đăng ký",
                AppTheme.WARNING,
                AppTheme.WARNING_BG
        );

        cardsGrid.add(totalRegistrationsCard);
        cardsGrid.add(matchedRangeCard);
        cardsGrid.add(activeAccountsCard);
        cardsGrid.add(historicalDeletedCard);
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

        nameFilterField = new JTextField();
        nameFilterField.setFont(AppTheme.FONT_BODY);
        nameFilterField.setForeground(AppTheme.TEXT_PRIMARY);
        nameFilterField.setPreferredSize(new Dimension(190, AppTheme.CONTROL_HEIGHT));
        nameFilterField.setMinimumSize(new Dimension(120, AppTheme.CONTROL_HEIGHT));
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

        fromDateField = new JTextField("2026-01-01");
        fromDateField.setFont(AppTheme.FONT_BODY);
        fromDateField.setPreferredSize(new Dimension(110, AppTheme.CONTROL_HEIGHT));
        fromDateField.setMinimumSize(new Dimension(110, AppTheme.CONTROL_HEIGHT));
        fromDateField.putClientProperty("JTextField.placeholderText", "2026-01-01");
        fromDateField.setToolTipText("Từ ngày đăng ký (YYYY-MM-DD)");
        fromDateField.addActionListener(e -> applyFiltersAndSort());

        toDateField = new JTextField("2026-12-31");
        toDateField.setFont(AppTheme.FONT_BODY);
        toDateField.setPreferredSize(new Dimension(110, AppTheme.CONTROL_HEIGHT));
        toDateField.setMinimumSize(new Dimension(110, AppTheme.CONTROL_HEIGHT));
        toDateField.putClientProperty("JTextField.placeholderText", "2026-12-31");
        toDateField.setToolTipText("Đến ngày đăng ký (YYYY-MM-DD)");
        toDateField.addActionListener(e -> applyFiltersAndSort());

        JButton applyDateBtn = createSecondaryButton("Lọc ngày", 88);
        applyDateBtn.addActionListener(e -> applyFiltersAndSort());

        sortCombo = new JComboBox<>(SORT_OPTIONS);
        sortCombo.setFont(AppTheme.FONT_BODY);
        sortCombo.setPreferredSize(new Dimension(174, AppTheme.CONTROL_HEIGHT));
        sortCombo.setMinimumSize(new Dimension(174, AppTheme.CONTROL_HEIGHT));
        sortCombo.addActionListener(e -> applyFiltersAndSort());

        JButton clearSelectionBtn = createSecondaryButton("Bỏ chọn", 84);
        clearSelectionBtn.addActionListener(e -> regTable.clearSelection());

        JButton resetBtn = createSecondaryButton("Làm mới", 88);
        resetBtn.addActionListener(e -> resetFilters());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;

        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 8);
        toolbar.add(nameFilterField, gbc);

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
        AppTheme.configureSharedTable(regTable);

        DefaultTableCellRenderer centerRenderer = createPaddedRenderer(SwingConstants.CENTER, AppTheme.FONT_BODY, AppTheme.TEXT_SECONDARY);
        DefaultTableCellRenderer boldNameRenderer = createPaddedRenderer(SwingConstants.LEFT, AppTheme.FONT_BODY_BOLD, AppTheme.TEXT_PRIMARY);
        DefaultTableCellRenderer usernameRenderer = createPaddedRenderer(SwingConstants.LEFT, AppTheme.FONT_BODY, AppTheme.PRIMARY);
        DefaultTableCellRenderer timeRenderer = createPaddedRenderer(SwingConstants.CENTER, AppTheme.FONT_BODY_BOLD, AppTheme.TEXT_PRIMARY);
        DefaultTableCellRenderer emailRenderer = createPaddedRenderer(SwingConstants.LEFT, AppTheme.FONT_BODY, AppTheme.TEXT_SECONDARY);

        TableColumnModel cm = regTable.getColumnModel();
        cm.getColumn(0).setCellRenderer(centerRenderer);
        cm.getColumn(0).setMaxWidth(45);

        cm.getColumn(1).setCellRenderer(boldNameRenderer);
        cm.getColumn(1).setPreferredWidth(210);

        cm.getColumn(2).setCellRenderer(usernameRenderer);
        cm.getColumn(2).setPreferredWidth(150);

        cm.getColumn(3).setCellRenderer(timeRenderer);
        cm.getColumn(3).setPreferredWidth(175);

        cm.getColumn(4).setCellRenderer(emailRenderer);
        cm.getColumn(4).setPreferredWidth(230);

        cm.getColumn(5).setCellRenderer(new LifecyclePillRenderer(tableModel));
        cm.getColumn(5).setPreferredWidth(190);

        regTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectionStatus();
            }
        });

        JScrollPane scrollPane = new JScrollPane(regTable);
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

        JLabel title = new JLabel("Không tìm thấy tài khoản đăng ký mới trong khoảng thời gian này");
        title.setFont(AppTheme.FONT_SECTION_TITLE);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyStateDetailLabel = new JLabel("Không có tài khoản nào khớp với từ khóa hoặc khoảng ngày đã chọn.");
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
        fromDateField.setText("2026-01-01");
        toDateField.setText("2026-12-31");
        sortCombo.setSelectedIndex(0);
        validationBanner.setVisible(false);
        applyFiltersAndSort();
    }

    private void applyFiltersAndSort() {
        String keyword = nameFilterField != null ? nameFilterField.getText().trim().toLowerCase(Locale.ROOT) : "";
        String fromText = fromDateField != null ? fromDateField.getText().trim() : "";
        String toText = toDateField != null ? toDateField.getText().trim() : "";

        LocalDate fromDate = null;
        LocalDate toDate = null;

        if (!fromText.isEmpty()) {
            try {
                fromDate = LocalDate.parse(fromText);
            } catch (DateTimeParseException ex) {
                showValidationError("Lỗi khoảng thời gian: 'Từ ngày' (\"" + fromText
                        + "\") không đúng định dạng YYYY-MM-DD (ví dụ: 2026-02-01).");
                return;
            }
        }
        if (!toText.isEmpty()) {
            try {
                toDate = LocalDate.parse(toText);
            } catch (DateTimeParseException ex) {
                showValidationError("Lỗi khoảng thời gian: 'Đến ngày' (\"" + toText
                        + "\") không đúng định dạng YYYY-MM-DD (ví dụ: 2026-02-28).");
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

        List<RegistrationRecord> all = userRepository.getRegistrationRecords();
        List<RegistrationRecord> filtered = new ArrayList<>();
        for (RegistrationRecord r : all) {
            if (!keyword.isEmpty()) {
                boolean matchName = r.getFullName().toLowerCase(Locale.ROOT).contains(keyword);
                boolean matchUser = r.getUsername().toLowerCase(Locale.ROOT).contains(keyword);
                if (!matchName && !matchUser) {
                    continue;
                }
            }
            LocalDate regDate = r.getCreatedAt().toLocalDate();
            if (fromDate != null && regDate.isBefore(fromDate)) {
                continue;
            }
            if (toDate != null && regDate.isAfter(toDate)) {
                continue;
            }
            filtered.add(r);
        }

        String sortOption = sortCombo != null ? (String) sortCombo.getSelectedItem() : SORT_OPTIONS[0];
        Collator viCollator = Collator.getInstance(Locale.of("vi", "VN"));
        Comparator<RegistrationRecord> cmp = switch (sortOption != null ? sortOption : SORT_OPTIONS[0]) {
            case "Họ tên: A–Z" -> (a, b) -> {
                int c = viCollator.compare(a.getFullName(), b.getFullName());
                return c != 0 ? c : Long.compare(a.getUserId(), b.getUserId());
            };
            case "Họ tên: Z–A" -> (a, b) -> {
                int c = viCollator.compare(b.getFullName(), a.getFullName());
                return c != 0 ? c : Long.compare(a.getUserId(), b.getUserId());
            };
            case "Ngày tạo: Mới nhất" -> Comparator.comparing(RegistrationRecord::getCreatedAt).reversed()
                    .thenComparing(Comparator.comparingLong(RegistrationRecord::getUserId).reversed());
            default -> Comparator.comparing(RegistrationRecord::getCreatedAt)
                    .thenComparingLong(RegistrationRecord::getUserId);
        };
        filtered.sort(cmp);

        tableModel.setRecords(filtered);

        if (filtered.isEmpty()) {
            viewportCardLayout.show(viewportDeck, CARD_EMPTY);
            if (emptyStateDetailLabel != null) {
                emptyStateDetailLabel.setText(
                        "Không có tài khoản nào đăng ký trong khoảng thời gian / điều kiện lọc đã chọn."
                );
            }
        } else {
            viewportCardLayout.show(viewportDeck, CARD_TABLE);
            regTable.setRowSelectionInterval(0, 0);
        }

        if (totalRegistrationsCard != null) {
            totalRegistrationsCard.setCount(all.size());
            matchedRangeCard.setCount(filtered.size());
            activeAccountsCard.setCount((int) filtered.stream().filter(r -> !r.isSoftDeleted() && !r.isLocked()).count());
            historicalDeletedCard.setCount((int) filtered.stream().filter(RegistrationRecord::isSoftDeleted).count());
        }
        if (recordCountLabel != null) {
            recordCountLabel.setText(
                    "Hiển thị " + filtered.size() + " / " + all.size() + " tài khoản đăng ký (Múi giờ Asia/Ho_Chi_Minh)"
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
        int row = regTable.getSelectedRow();
        if (row < 0 || row >= tableModel.getRowCount()) {
            selectionStatusLabel.setText(
                    "Trạng thái chọn: Chưa chọn tài khoản nào (No-selection) — Nhấp vào một dòng để xem thông tin đăng ký."
            );
            selectionStatusLabel.setForeground(AppTheme.TEXT_SECONDARY);
        } else {
            RegistrationRecord r = tableModel.getRecordAt(row);
            selectionStatusLabel.setText(
                    "Đang chọn: " + r.getFullName() + " (@" + r.getUsername()
                            + ")  •  Tạo lúc: " + r.getFormattedCreatedAt()
                            + "  •  " + r.getAccountLifecycleLabel()
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
        return AppTheme.createPrimaryOutlineButton(text, width);
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

    private static final class LifecyclePillRenderer extends JPanel implements javax.swing.table.TableCellRenderer {

        private final RegistrationTableModel model;
        private RegistrationRecord record;
        private boolean rowSelected;

        private LifecyclePillRenderer(RegistrationTableModel model) {
            this.model = model;
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            this.record = model.getRecordAt(row);
            this.rowSelected = isSelected;
            setBackground(isSelected ? AppTheme.PRIMARY_SOFT_BG : AppTheme.SURFACE_CARD);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (record == null) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            String text = record.getAccountLifecycleLabel();
            Color bg;
            Color fg;
            if (record.isSoftDeleted()) {
                bg = AppTheme.WARNING_BG;
                fg = AppTheme.WARNING;
            } else if (record.isLocked()) {
                bg = AppTheme.DANGER_BG;
                fg = AppTheme.DANGER;
            } else {
                bg = AppTheme.SUCCESS_BG;
                fg = AppTheme.SUCCESS;
            }

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

    private static final class RegistrationTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {
                "STT",
                "Họ và tên",
                "Tên đăng nhập",
                "Thời gian tạo tài khoản",
                "Email",
                "Trạng thái lưu trữ"
        };

        private final List<RegistrationRecord> records;

        private RegistrationTableModel(List<RegistrationRecord> initialRecords) {
            this.records = new ArrayList<>(initialRecords);
        }

        public void setRecords(List<RegistrationRecord> updated) {
            this.records.clear();
            if (updated != null) {
                this.records.addAll(updated);
            }
            fireTableDataChanged();
        }

        public RegistrationRecord getRecordAt(int rowIndex) {
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
            RegistrationRecord r = records.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> rowIndex + 1;
                case 1 -> r.getFullName();
                case 2 -> "@" + r.getUsername();
                case 3 -> r.getFormattedCreatedAt();
                case 4 -> r.getEmail();
                case 5 -> r.getAccountLifecycleLabel();
                default -> "";
            };
        }
    }
}
