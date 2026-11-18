package com.chatsystem.admin.ui;

import com.chatsystem.admin.components.StatCard;
import com.chatsystem.admin.data.MockUserRepository;
import com.chatsystem.admin.model.FriendStatRecord;
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
import java.awt.event.MouseEvent;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * User Friend Statistics screen panel (Requirement 7 / Step G - C).
 * Displays full name, username, account creation time, direct-friend count (1-hop),
 * and distinct-friend count including 1-hop and 2-hop friends (with explicit tooltips
 * explaining that the second count excludes self, duplicates, and soft-deleted paths),
 * name filter, sorting by name/creation time, and direct-friend count filter with {@code =}, {@code <}, {@code >}.
 */
public class FriendStatisticsPanel extends JPanel {

    private static final String CARD_TABLE = "TABLE";
    private static final String CARD_EMPTY = "EMPTY";

    public static final String FOF_TOOLTIP_TEXT =
            "Số lượng người dùng DISTINCT trong phạm vi 1 hoặc 2 bậc kết bạn (ACCEPTED): "
                    + "Đã loại trừ chính mình (excludes self), loại trừ trùng lặp (excludes duplicates) "
                    + "và không đi qua tài khoản đã xóa mềm.";

    private static final String[] OPERATOR_OPTIONS = {"=", "<", ">"};

    private static final String[] SORT_OPTIONS = {
            "Ngày tạo: Cũ nhất",
            "Ngày tạo: Mới nhất",
            "Họ tên: A–Z",
            "Họ tên: Z–A"
    };

    private static final int COL_DIRECT_FRIENDS = 4;
    private static final int COL_FOF_FRIENDS = 5;

    private final MockUserRepository userRepository;
    private final FriendStatsTableModel tableModel;
    private final JTable statsTable;
    private final CardLayout viewportCardLayout;
    private final JPanel viewportDeck;

    private StatCard totalUsersCard;
    private StatCard usersWithFriendsCard;
    private StatCard maxDirectFriendsCard;
    private StatCard maxFofFriendsCard;

    private JTextField nameFilterField;
    private JComboBox<String> operatorCombo;
    private JTextField directCountInputField;
    private JComboBox<String> sortCombo;
    private JPanel validationBanner;
    private JLabel validationMessageLabel;
    private JLabel selectionStatusLabel;
    private JLabel recordCountLabel;
    private JLabel emptyStateDetailLabel;

    public FriendStatisticsPanel(MockUserRepository userRepository) {
        super(new BorderLayout(0, AppTheme.SPACING_MD - 2));
        this.userRepository = userRepository;
        this.tableModel = new FriendStatsTableModel(userRepository.getFriendStatistics());
        this.statsTable = new JTable(tableModel) {
            @Override
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    @Override
                    public String getToolTipText(MouseEvent e) {
                        int col = columnModel.getColumnIndexAtX(e.getPoint().x);
                        if (col == COL_FOF_FRIENDS) {
                            return FOF_TOOLTIP_TEXT;
                        }
                        if (col == COL_DIRECT_FRIENDS) {
                            return "Số lượng bạn bè trực tiếp (1 bậc / 1-hop) đã chấp nhận kết bạn (ACCEPTED).";
                        }
                        return super.getToolTipText(e);
                    }
                };
            }
        };
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
        topSection.add(createPageIntroAndTooltipBanner(), BorderLayout.NORTH);
        topSection.add(createSummaryCardsRow(), BorderLayout.CENTER);

        add(topSection, BorderLayout.NORTH);
        add(createMainTableCard(), BorderLayout.CENTER);

        this.userRepository.addDataChangeListener(this::applyFiltersAndSort);
        applyFiltersAndSort();
    }

    private JPanel createPageIntroAndTooltipBanner() {
        JPanel wrapper = new JPanel(new BorderLayout(0, AppTheme.SPACING_SM));
        wrapper.setOpaque(false);

        JPanel introRow = new JPanel(new BorderLayout(AppTheme.SPACING_MD, 0));
        introRow.setOpaque(false);

        JPanel textGroup = new JPanel();
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));
        textGroup.setOpaque(false);

        JLabel titleLabel = new JLabel("Thống kê bạn bè & Mạng lưới kết nối");
        titleLabel.setFont(AppTheme.FONT_PAGE_TITLE);
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(
                "Thống kê số lượng bạn bè trực tiếp (1-hop) và tổng số bạn bè + bạn của bạn (1–2 bậc • QUERY 8)"
        );
        subtitleLabel.setFont(AppTheme.FONT_BODY);
        subtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textGroup.add(titleLabel);
        textGroup.add(Box.createVerticalStrut(2));
        textGroup.add(subtitleLabel);

        JLabel tooltipBadge = new JLabel("(i) Chú giải cột Bạn của bạn (Di chuột để xem Tooltip)");
        tooltipBadge.setFont(AppTheme.FONT_CAPTION_BOLD);
        tooltipBadge.setForeground(AppTheme.PRIMARY);
        tooltipBadge.setOpaque(true);
        tooltipBadge.setBackground(AppTheme.PRIMARY_SOFT_BG);
        tooltipBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xBF, 0xDB, 0xFE), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        tooltipBadge.setToolTipText(FOF_TOOLTIP_TEXT);

        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(tooltipBadge);

        introRow.add(textGroup, BorderLayout.WEST);
        introRow.add(badgeWrapper, BorderLayout.EAST);

        JPanel ruleBanner = new JPanel(new BorderLayout());
        ruleBanner.setBackground(AppTheme.PRIMARY_SOFT_BG);
        ruleBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppTheme.PRIMARY),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));
        ruleBanner.setToolTipText(FOF_TOOLTIP_TEXT);

        JLabel ruleLabel = new JLabel(
                "Quy tắc 'Bạn bè + Bạn của bạn (1–2 bậc)': Đếm người dùng DISTINCT trong 1 hoặc 2 bậc kết bạn (ACCEPTED), "
                        + "đã loại trừ chính mình (self), loại trừ trùng lặp (duplicates) và TK đã xóa mềm."
        );
        ruleLabel.setFont(AppTheme.FONT_CAPTION);
        ruleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        ruleLabel.setToolTipText(FOF_TOOLTIP_TEXT);
        ruleBanner.add(ruleLabel, BorderLayout.CENTER);

        wrapper.add(introRow, BorderLayout.NORTH);
        wrapper.add(ruleBanner, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createSummaryCardsRow() {
        List<FriendStatRecord> all = userRepository.getFriendStatistics();
        int total = all.size();
        int withFriends = (int) all.stream().filter(r -> r.getDirectFriendsCount() > 0).count();
        int maxDirect = all.stream().mapToInt(FriendStatRecord::getDirectFriendsCount).max().orElse(0);
        int maxFof = all.stream().mapToInt(FriendStatRecord::getDistinctFriendsWithFofCount).max().orElse(0);

        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, AppTheme.SPACING_MD, 0));
        cardsGrid.setOpaque(false);

        totalUsersCard = new StatCard(
                "Tài khoản đang thống kê",
                total,
                "Người dùng chưa xóa mềm",
                AppTheme.PRIMARY,
                AppTheme.PRIMARY_SOFT_BG
        );
        usersWithFriendsCard = new StatCard(
                "Có bạn bè trực tiếp",
                withFriends,
                "Số bạn trực tiếp > 0",
                AppTheme.SUCCESS,
                AppTheme.SUCCESS_BG
        );
        maxDirectFriendsCard = new StatCard(
                "Bạn trực tiếp cao nhất",
                maxDirect,
                "Bậc 1 (1-hop) tối đa",
                AppTheme.TEXT_SECONDARY,
                AppTheme.NEUTRAL_BG
        );
        maxFofFriendsCard = new StatCard(
                "Bạn 1–2 bậc cao nhất",
                maxFof,
                "DISTINCT 1–2 bậc tối đa",
                AppTheme.WARNING,
                AppTheme.WARNING_BG
        );

        cardsGrid.add(totalUsersCard);
        cardsGrid.add(usersWithFriendsCard);
        cardsGrid.add(maxDirectFriendsCard);
        cardsGrid.add(maxFofFriendsCard);
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

        operatorCombo = new JComboBox<>(OPERATOR_OPTIONS);
        operatorCombo.setFont(AppTheme.FONT_BODY_BOLD);
        operatorCombo.setPreferredSize(new Dimension(64, AppTheme.CONTROL_HEIGHT));
        operatorCombo.setMinimumSize(new Dimension(64, AppTheme.CONTROL_HEIGHT));
        operatorCombo.setToolTipText("Chọn toán tử so sánh số lượng bạn bè trực tiếp (=, <, >)");
        operatorCombo.addActionListener(e -> applyFiltersAndSort());

        directCountInputField = new JTextField();
        directCountInputField.setFont(AppTheme.FONT_BODY);
        directCountInputField.setPreferredSize(new Dimension(88, AppTheme.CONTROL_HEIGHT));
        directCountInputField.setMinimumSize(new Dimension(88, AppTheme.CONTROL_HEIGHT));
        directCountInputField.putClientProperty("JTextField.placeholderText", "Số bạn...");
        directCountInputField.setToolTipText("Nhập số lượng bạn bè trực tiếp để lọc (để trống để xem tất cả)");
        directCountInputField.getDocument().addDocumentListener(new DocumentListener() {
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

        sortCombo = new JComboBox<>(SORT_OPTIONS);
        sortCombo.setFont(AppTheme.FONT_BODY);
        sortCombo.setPreferredSize(new Dimension(174, AppTheme.CONTROL_HEIGHT));
        sortCombo.setMinimumSize(new Dimension(174, AppTheme.CONTROL_HEIGHT));
        sortCombo.addActionListener(e -> applyFiltersAndSort());

        JButton clearSelectionBtn = createSecondaryButton("Bỏ chọn", 84);
        clearSelectionBtn.addActionListener(e -> statsTable.clearSelection());

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
        toolbar.add(createToolbarLabel("Bạn trực tiếp:"), gbc);

        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 4);
        toolbar.add(operatorCombo, gbc);

        gbc.gridx = 3;
        gbc.insets = new Insets(0, 0, 0, 8);
        toolbar.add(directCountInputField, gbc);

        gbc.gridx = 4;
        gbc.insets = new Insets(0, 2, 0, 4);
        toolbar.add(createToolbarLabel("Sắp xếp:"), gbc);

        gbc.gridx = 5;
        gbc.insets = new Insets(0, 0, 0, 6);
        toolbar.add(sortCombo, gbc);

        gbc.gridx = 6;
        gbc.insets = new Insets(0, 0, 0, 6);
        toolbar.add(clearSelectionBtn, gbc);

        gbc.gridx = 7;
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
        statsTable.setFont(AppTheme.FONT_BODY);
        statsTable.setForeground(AppTheme.TEXT_PRIMARY);
        statsTable.setBackground(AppTheme.SURFACE_CARD);
        statsTable.setRowHeight(AppTheme.TABLE_ROW_HEIGHT);
        statsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        statsTable.setSelectionBackground(AppTheme.PRIMARY_SOFT_BG);
        statsTable.setSelectionForeground(AppTheme.TEXT_PRIMARY);
        statsTable.setShowHorizontalLines(true);
        statsTable.setShowVerticalLines(false);
        statsTable.setGridColor(AppTheme.BORDER);
        statsTable.setIntercellSpacing(new Dimension(0, 1));
        statsTable.setFillsViewportHeight(true);

        JTableHeader header = statsTable.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 40));
        header.setFont(AppTheme.FONT_CAPTION_BOLD);
        header.setBackground(AppTheme.PAGE_BG);
        header.setForeground(AppTheme.TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER));

        DefaultTableCellRenderer centerRenderer = createPaddedRenderer(SwingConstants.CENTER, AppTheme.FONT_BODY, AppTheme.TEXT_SECONDARY, null);
        DefaultTableCellRenderer boldNameRenderer = createPaddedRenderer(SwingConstants.LEFT, AppTheme.FONT_BODY_BOLD, AppTheme.TEXT_PRIMARY, null);
        DefaultTableCellRenderer usernameRenderer = createPaddedRenderer(SwingConstants.LEFT, AppTheme.FONT_BODY, AppTheme.PRIMARY, null);
        DefaultTableCellRenderer summaryRenderer = createPaddedRenderer(SwingConstants.LEFT, AppTheme.FONT_CAPTION, AppTheme.TEXT_SECONDARY, FOF_TOOLTIP_TEXT);

        TableColumnModel cm = statsTable.getColumnModel();
        cm.getColumn(0).setCellRenderer(centerRenderer);
        cm.getColumn(0).setMaxWidth(44);

        cm.getColumn(1).setCellRenderer(boldNameRenderer);
        cm.getColumn(1).setPreferredWidth(185);

        cm.getColumn(2).setCellRenderer(usernameRenderer);
        cm.getColumn(2).setPreferredWidth(135);

        cm.getColumn(3).setCellRenderer(centerRenderer);
        cm.getColumn(3).setPreferredWidth(145);

        cm.getColumn(COL_DIRECT_FRIENDS).setCellRenderer(
                new CountBadgeRenderer(AppTheme.PRIMARY, AppTheme.PRIMARY_SOFT_BG,
                        "Số lượng bạn bè trực tiếp (1 bậc / 1-hop) đã chấp nhận kết bạn.")
        );
        cm.getColumn(COL_DIRECT_FRIENDS).setPreferredWidth(145);

        cm.getColumn(COL_FOF_FRIENDS).setCellRenderer(
                new CountBadgeRenderer(AppTheme.SUCCESS, AppTheme.SUCCESS_BG, FOF_TOOLTIP_TEXT)
        );
        cm.getColumn(COL_FOF_FRIENDS).setPreferredWidth(185);

        cm.getColumn(6).setCellRenderer(summaryRenderer);
        cm.getColumn(6).setPreferredWidth(215);

        statsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectionStatus();
            }
        });

        JScrollPane scrollPane = new JScrollPane(statsTable);
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

        JLabel title = new JLabel("Không tìm thấy người dùng khớp điều kiện thống kê bạn bè");
        title.setFont(AppTheme.FONT_SECTION_TITLE);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyStateDetailLabel = new JLabel("Không có người dùng nào khớp với bộ lọc tên hoặc số lượng bạn bè trực tiếp.");
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
        operatorCombo.setSelectedIndex(0);
        directCountInputField.setText("");
        sortCombo.setSelectedIndex(0);
        validationBanner.setVisible(false);
        applyFiltersAndSort();
    }

    private void applyFiltersAndSort() {
        String keyword = nameFilterField != null ? nameFilterField.getText().trim().toLowerCase(Locale.ROOT) : "";
        String op = operatorCombo != null ? (String) operatorCombo.getSelectedItem() : "=";
        String numText = directCountInputField != null ? directCountInputField.getText().trim() : "";

        Integer targetCount = null;
        if (!numText.isEmpty()) {
            try {
                targetCount = Integer.parseInt(numText);
                if (targetCount < 0) {
                    showValidationError("Lỗi bộ lọc số lượng bạn trực tiếp: Giá trị không được là số âm (" + numText + ").");
                    return;
                }
            } catch (NumberFormatException ex) {
                showValidationError("Lỗi bộ lọc số lượng bạn trực tiếp: \"" + numText
                        + "\" không phải là số nguyên hợp lệ (ví dụ: 0, 1, 3).");
                return;
            }
        }

        if (validationBanner != null) {
            validationBanner.setVisible(false);
        }

        List<FriendStatRecord> all = userRepository.getFriendStatistics();
        List<FriendStatRecord> filtered = new ArrayList<>();
        for (FriendStatRecord r : all) {
            if (!keyword.isEmpty()) {
                boolean matchName = r.getFullName().toLowerCase(Locale.ROOT).contains(keyword);
                boolean matchUser = r.getUsername().toLowerCase(Locale.ROOT).contains(keyword);
                if (!matchName && !matchUser) {
                    continue;
                }
            }
            if (targetCount != null) {
                int direct = r.getDirectFriendsCount();
                boolean matchesOp = switch (op != null ? op : "=") {
                    case "<" -> direct < targetCount;
                    case ">" -> direct > targetCount;
                    default -> direct == targetCount;
                };
                if (!matchesOp) {
                    continue;
                }
            }
            filtered.add(r);
        }

        String sortOption = sortCombo != null ? (String) sortCombo.getSelectedItem() : SORT_OPTIONS[0];
        Collator viCollator = Collator.getInstance(Locale.of("vi", "VN"));
        Comparator<FriendStatRecord> cmp = switch (sortOption != null ? sortOption : SORT_OPTIONS[0]) {
            case "Họ tên: A–Z" -> (a, b) -> {
                int c = viCollator.compare(a.getFullName(), b.getFullName());
                return c != 0 ? c : Long.compare(a.getUserId(), b.getUserId());
            };
            case "Họ tên: Z–A" -> (a, b) -> {
                int c = viCollator.compare(b.getFullName(), a.getFullName());
                return c != 0 ? c : Long.compare(a.getUserId(), b.getUserId());
            };
            case "Ngày tạo: Mới nhất" -> Comparator.comparing(FriendStatRecord::getCreatedAt).reversed()
                    .thenComparing(Comparator.comparingLong(FriendStatRecord::getUserId).reversed());
            default -> Comparator.comparing(FriendStatRecord::getCreatedAt)
                    .thenComparingLong(FriendStatRecord::getUserId);
        };
        filtered.sort(cmp);

        tableModel.setRecords(filtered);

        if (filtered.isEmpty()) {
            viewportCardLayout.show(viewportDeck, CARD_EMPTY);
            if (emptyStateDetailLabel != null) {
                emptyStateDetailLabel.setText(
                        "Không có người dùng nào thỏa mãn điều kiện lọc bạn bè trực tiếp hiện tại."
                );
            }
        } else {
            viewportCardLayout.show(viewportDeck, CARD_TABLE);
            statsTable.setRowSelectionInterval(0, 0);
        }

        if (recordCountLabel != null) {
            recordCountLabel.setText(
                    "Hiển thị " + filtered.size() + " / " + all.size() + " người dùng (Khớp QUERY 8)"
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
        int row = statsTable.getSelectedRow();
        if (row < 0 || row >= tableModel.getRowCount()) {
            selectionStatusLabel.setText(
                    "Trạng thái chọn: Chưa chọn dòng nào (No-selection) — Chọn một người dùng để xem tập kết nối 1–2 bậc."
            );
            selectionStatusLabel.setForeground(AppTheme.TEXT_SECONDARY);
        } else {
            FriendStatRecord r = tableModel.getRecordAt(row);
            selectionStatusLabel.setText(
                    "Đang chọn: " + r.getFullName() + " (@" + r.getUsername()
                            + ")  •  Bạn trực tiếp: " + r.getDirectFriendsCount()
                            + "  •  Bạn 1–2 bậc (DISTINCT, loại trừ chính mình): " + r.getDistinctFriendsWithFofCount()
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

    private DefaultTableCellRenderer createPaddedRenderer(
            int alignment,
            java.awt.Font font,
            Color textColor,
            String tooltip
    ) {
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
                label.setToolTipText(tooltip);
                if (alignment == SwingConstants.CENTER) {
                    label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                } else {
                    label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
                }
                return label;
            }
        };
    }

    private static final class CountBadgeRenderer extends JPanel implements javax.swing.table.TableCellRenderer {

        private final Color accentFg;
        private final Color accentBg;
        private String valueText = "0";
        private boolean zeroValue = true;
        private boolean rowSelected = false;

        private CountBadgeRenderer(Color accentFg, Color accentBg, String tooltipText) {
            this.accentFg = accentFg;
            this.accentBg = accentBg;
            setOpaque(true);
            setToolTipText(tooltipText);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            this.valueText = value != null ? value.toString() : "0";
            this.zeroValue = "0".equals(this.valueText) || this.valueText.startsWith("0 ");
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

    private static final class FriendStatsTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {
                "STT",
                "Họ và tên",
                "Tên đăng nhập",
                "Thời gian tạo TK",
                "Bạn bè trực tiếp",
                "Bạn bè + Bạn của bạn (i)",
                "Tập kết nối 1–2 bậc (Loại trừ self)"
        };

        private final List<FriendStatRecord> records;

        private FriendStatsTableModel(List<FriendStatRecord> initialRecords) {
            this.records = new ArrayList<>(initialRecords);
        }

        public void setRecords(List<FriendStatRecord> updated) {
            this.records.clear();
            if (updated != null) {
                this.records.addAll(updated);
            }
            fireTableDataChanged();
        }

        public FriendStatRecord getRecordAt(int rowIndex) {
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
            FriendStatRecord r = records.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> rowIndex + 1;
                case 1 -> r.getFullName();
                case 2 -> "@" + r.getUsername();
                case 3 -> r.getFormattedCreatedAt();
                case 4 -> String.valueOf(r.getDirectFriendsCount());
                case 5 -> String.valueOf(r.getDistinctFriendsWithFofCount());
                case 6 -> r.getReachableUsernamesSummary();
                default -> "";
            };
        }
    }
}
