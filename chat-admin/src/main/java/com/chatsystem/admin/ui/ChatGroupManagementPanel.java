package com.chatsystem.admin.ui;

import com.chatsystem.admin.components.StatCard;
import com.chatsystem.admin.data.MockUserRepository;
import com.chatsystem.admin.model.ChatGroupRecord;
import com.chatsystem.admin.model.GroupMemberRecord;
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
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Read-only Chat Group Management screen panel (Requirement 3 / Step F - B).
 * Displays group name, creation time, creator, current member count, and encryption indicator,
 * with filtering by group name, sorting by group name / creation time, and read-only dialogs
 * for inspecting the selected group's current members and group admins.
 */
public class ChatGroupManagementPanel extends JPanel {

    private static final String CARD_TABLE = "TABLE";
    private static final String CARD_EMPTY = "EMPTY";

    private static final String[] SORT_OPTIONS = {
            "Ngày tạo: Cũ nhất",
            "Ngày tạo: Mới nhất",
            "Tên nhóm: A–Z",
            "Tên nhóm: Z–A"
    };

    private static final int COL_ACTIONS = 6;

    private final MockUserRepository userRepository;
    private final ChatGroupTableModel tableModel;
    private final JTable groupTable;
    private final CardLayout viewportCardLayout;
    private final JPanel viewportDeck;

    private StatCard totalGroupsCard;
    private StatCard encryptedGroupsCard;
    private StatCard standardGroupsCard;
    private StatCard activeMembershipsCard;

    private JTextField nameFilterField;
    private JComboBox<String> sortCombo;
    private JLabel selectionStatusLabel;
    private JLabel recordCountLabel;
    private JLabel emptyStateDetailLabel;

    public ChatGroupManagementPanel(MockUserRepository userRepository) {
        super(new BorderLayout(0, AppTheme.SPACING_MD));
        this.userRepository = userRepository;
        this.tableModel = new ChatGroupTableModel(userRepository.getChatGroups());
        this.groupTable = new JTable(tableModel);
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

        JLabel titleLabel = new JLabel("Quản lý nhóm chat");
        titleLabel.setFont(AppTheme.FONT_PAGE_TITLE);
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(
                "Tra cứu danh sách nhóm chat, chỉ báo mã hóa, danh sách thành viên hiện tại và quản trị viên nhóm"
        );
        subtitleLabel.setFont(AppTheme.FONT_BODY);
        subtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textGroup.add(titleLabel);
        textGroup.add(Box.createVerticalStrut(4));
        textGroup.add(subtitleLabel);

        JLabel readOnlyBadge = new JLabel("CHẾ ĐỘ TRA CỨU CHỈ ĐỌC (READ-ONLY)");
        readOnlyBadge.setFont(AppTheme.FONT_CAPTION_BOLD);
        readOnlyBadge.setForeground(AppTheme.PRIMARY);
        readOnlyBadge.setOpaque(true);
        readOnlyBadge.setBackground(AppTheme.PRIMARY_SOFT_BG);
        readOnlyBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xBF, 0xDB, 0xFE), 1, true),
                BorderFactory.createEmptyBorder(7, 14, 7, 14)
        ));

        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(readOnlyBadge);

        introPanel.add(textGroup, BorderLayout.WEST);
        introPanel.add(badgeWrapper, BorderLayout.EAST);
        return introPanel;
    }

    private JPanel createSummaryCardsRow() {
        List<ChatGroupRecord> groups = userRepository.getChatGroups();
        int total = groups.size();
        int encrypted = (int) groups.stream().filter(ChatGroupRecord::isEncrypted).count();
        int standard = total - encrypted;
        int totalCurrentMembers = groups.stream().mapToInt(ChatGroupRecord::getCurrentMemberCount).sum();

        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, AppTheme.SPACING_MD, 0));
        cardsGrid.setOpaque(false);

        totalGroupsCard = new StatCard(
                "Tổng số nhóm chat",
                total,
                "Nhóm trong hệ thống",
                AppTheme.PRIMARY,
                AppTheme.PRIMARY_SOFT_BG
        );
        encryptedGroupsCard = new StatCard(
                "Nhóm bật mã hóa",
                encrypted,
                "is_encrypted = TRUE",
                AppTheme.SUCCESS,
                AppTheme.SUCCESS_BG
        );
        standardGroupsCard = new StatCard(
                "Nhóm tiêu chuẩn",
                standard,
                "is_encrypted = FALSE",
                AppTheme.TEXT_SECONDARY,
                AppTheme.NEUTRAL_BG
        );
        activeMembershipsCard = new StatCard(
                "Tổng thành viên hiện tại",
                totalCurrentMembers,
                "Thành viên left_at IS NULL",
                AppTheme.WARNING,
                AppTheme.WARNING_BG
        );

        cardsGrid.add(totalGroupsCard);
        cardsGrid.add(encryptedGroupsCard);
        cardsGrid.add(standardGroupsCard);
        cardsGrid.add(activeMembershipsCard);
        return cardsGrid;
    }

    private JPanel createMainTableCard() {
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(AppTheme.SURFACE_CARD);
        tableCard.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1, true));

        viewportDeck.setBackground(AppTheme.SURFACE_CARD);
        viewportDeck.add(createTableScrollPane(), CARD_TABLE);
        viewportDeck.add(createEmptyResultPanel(), CARD_EMPTY);

        tableCard.add(createFilterToolbar(), BorderLayout.NORTH);
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
        nameFilterField.setPreferredSize(new Dimension(220, AppTheme.CONTROL_HEIGHT));
        nameFilterField.setMinimumSize(new Dimension(140, AppTheme.CONTROL_HEIGHT));
        nameFilterField.putClientProperty("JTextField.placeholderText", "Lọc theo tên nhóm chat...");
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

        sortCombo = new JComboBox<>(SORT_OPTIONS);
        sortCombo.setFont(AppTheme.FONT_BODY);
        sortCombo.setPreferredSize(new Dimension(156, AppTheme.CONTROL_HEIGHT));
        sortCombo.setMinimumSize(new Dimension(156, AppTheme.CONTROL_HEIGHT));
        sortCombo.addActionListener(e -> applyFiltersAndSort());

        JButton viewMembersBtn = createPrimaryOutlineButton("Xem thành viên hiện tại", 176);
        viewMembersBtn.addActionListener(e -> openSelectedGroupDialog(false));

        JButton viewAdminsBtn = createPrimaryOutlineButton("Xem Admin nhóm", 142);
        viewAdminsBtn.addActionListener(e -> openSelectedGroupDialog(true));

        JButton clearSelectionBtn = createSecondaryButton("Bỏ chọn", 84);
        clearSelectionBtn.setToolTipText("Bỏ chọn dòng hiện tại để kiểm tra trạng thái chưa chọn (no-selection)");
        clearSelectionBtn.addActionListener(e -> groupTable.clearSelection());

        JButton resetBtn = createSecondaryButton("Làm mới", 88);
        resetBtn.addActionListener(e -> resetFilters());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;

        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 8);
        toolbar.add(nameFilterField, gbc);

        JLabel sortLabel = new JLabel("Sắp xếp:");
        sortLabel.setFont(AppTheme.FONT_CAPTION_BOLD);
        sortLabel.setForeground(AppTheme.TEXT_SECONDARY);
        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 2, 0, 4);
        toolbar.add(sortLabel, gbc);

        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 8);
        toolbar.add(sortCombo, gbc);

        gbc.gridx = 3;
        gbc.insets = new Insets(0, 0, 0, 6);
        toolbar.add(viewMembersBtn, gbc);

        gbc.gridx = 4;
        gbc.insets = new Insets(0, 0, 0, 6);
        toolbar.add(viewAdminsBtn, gbc);

        gbc.gridx = 5;
        gbc.insets = new Insets(0, 0, 0, 6);
        toolbar.add(clearSelectionBtn, gbc);

        gbc.gridx = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        toolbar.add(resetBtn, gbc);

        return toolbar;
    }

    private JScrollPane createTableScrollPane() {
        groupTable.setFont(AppTheme.FONT_BODY);
        groupTable.setForeground(AppTheme.TEXT_PRIMARY);
        groupTable.setBackground(AppTheme.SURFACE_CARD);
        groupTable.setRowHeight(AppTheme.TABLE_ROW_HEIGHT);
        groupTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupTable.setSelectionBackground(AppTheme.PRIMARY_SOFT_BG);
        groupTable.setSelectionForeground(AppTheme.TEXT_PRIMARY);
        groupTable.setShowHorizontalLines(true);
        groupTable.setShowVerticalLines(false);
        groupTable.setGridColor(AppTheme.BORDER);
        groupTable.setIntercellSpacing(new Dimension(0, 1));
        groupTable.setFillsViewportHeight(true);

        JTableHeader header = groupTable.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 40));
        header.setFont(AppTheme.FONT_CAPTION_BOLD);
        header.setBackground(AppTheme.PAGE_BG);
        header.setForeground(AppTheme.TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER));

        DefaultTableCellRenderer centerRenderer = createPaddedRenderer(SwingConstants.CENTER, AppTheme.FONT_BODY, AppTheme.TEXT_SECONDARY);
        DefaultTableCellRenderer boldNameRenderer = createPaddedRenderer(SwingConstants.LEFT, AppTheme.FONT_BODY_BOLD, AppTheme.TEXT_PRIMARY);
        DefaultTableCellRenderer creatorRenderer = createPaddedRenderer(SwingConstants.LEFT, AppTheme.FONT_BODY, AppTheme.TEXT_PRIMARY);
        DefaultTableCellRenderer countRenderer = createPaddedRenderer(SwingConstants.CENTER, AppTheme.FONT_BODY_BOLD, AppTheme.TEXT_PRIMARY);

        TableColumnModel cm = groupTable.getColumnModel();
        cm.getColumn(0).setCellRenderer(centerRenderer);
        cm.getColumn(0).setMaxWidth(42);
        cm.getColumn(0).setPreferredWidth(40);

        cm.getColumn(1).setCellRenderer(boldNameRenderer);
        cm.getColumn(1).setPreferredWidth(205);

        cm.getColumn(2).setCellRenderer(centerRenderer);
        cm.getColumn(2).setPreferredWidth(140);

        cm.getColumn(3).setCellRenderer(creatorRenderer);
        cm.getColumn(3).setPreferredWidth(205);

        cm.getColumn(4).setCellRenderer(countRenderer);
        cm.getColumn(4).setPreferredWidth(170);

        cm.getColumn(5).setCellRenderer(new EncryptionBadgeRenderer());
        cm.getColumn(5).setPreferredWidth(160);

        cm.getColumn(COL_ACTIONS).setCellRenderer(new GroupActionButtonsRenderer());
        cm.getColumn(COL_ACTIONS).setPreferredWidth(170);

        groupTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectionStatus();
            }
        });

        groupTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = groupTable.columnAtPoint(e.getPoint());
                if (col == COL_ACTIONS) {
                    groupTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    groupTable.setCursor(Cursor.getDefaultCursor());
                }
            }
        });

        groupTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point pt = e.getPoint();
                int row = groupTable.rowAtPoint(pt);
                int col = groupTable.columnAtPoint(pt);
                if (row >= 0 && col == COL_ACTIONS) {
                    groupTable.setRowSelectionInterval(row, row);
                    Rectangle rect = groupTable.getCellRect(row, col, false);
                    int relX = pt.x - rect.x;
                    boolean adminsOnly = relX >= rect.width / 2;
                    openGroupDialogForRow(row, adminsOnly);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(groupTable);
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

        JLabel title = new JLabel("Không tìm thấy nhóm chat phù hợp");
        title.setFont(AppTheme.FONT_SECTION_TITLE);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyStateDetailLabel = new JLabel("Không có nhóm chat nào khớp với từ khóa tìm kiếm.");
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
        sortCombo.setSelectedIndex(0);
        applyFiltersAndSort();
    }

    private void applyFiltersAndSort() {
        String keyword = nameFilterField != null ? nameFilterField.getText().trim().toLowerCase(Locale.ROOT) : "";
        String sortOption = sortCombo != null ? (String) sortCombo.getSelectedItem() : SORT_OPTIONS[0];

        List<ChatGroupRecord> all = userRepository.getChatGroups();
        List<ChatGroupRecord> filtered = new ArrayList<>();
        for (ChatGroupRecord g : all) {
            if (!keyword.isEmpty() && !g.getGroupName().toLowerCase(Locale.ROOT).contains(keyword)) {
                continue;
            }
            filtered.add(g);
        }

        Collator viCollator = Collator.getInstance(Locale.of("vi", "VN"));
        Comparator<ChatGroupRecord> cmp = switch (sortOption != null ? sortOption : SORT_OPTIONS[0]) {
            case "Tên nhóm: A–Z" -> (a, b) -> {
                int c = viCollator.compare(a.getGroupName(), b.getGroupName());
                return c != 0 ? c : Long.compare(a.getId(), b.getId());
            };
            case "Tên nhóm: Z–A" -> (a, b) -> {
                int c = viCollator.compare(b.getGroupName(), a.getGroupName());
                return c != 0 ? c : Long.compare(a.getId(), b.getId());
            };
            case "Ngày tạo: Mới nhất" -> Comparator.comparing(ChatGroupRecord::getCreatedAt).reversed()
                    .thenComparing(Comparator.comparingLong(ChatGroupRecord::getId).reversed());
            default -> Comparator.comparing(ChatGroupRecord::getCreatedAt)
                    .thenComparingLong(ChatGroupRecord::getId);
        };
        filtered.sort(cmp);

        tableModel.setGroups(filtered);

        if (filtered.isEmpty()) {
            viewportCardLayout.show(viewportDeck, CARD_EMPTY);
            if (emptyStateDetailLabel != null) {
                emptyStateDetailLabel.setText(
                        "Không tìm thấy nhóm chat nào có tên chứa \"" + nameFilterField.getText().trim() + "\"."
                );
            }
        } else {
            viewportCardLayout.show(viewportDeck, CARD_TABLE);
            groupTable.setRowSelectionInterval(0, 0);
        }

        if (recordCountLabel != null) {
            recordCountLabel.setText(
                    "Hiển thị " + filtered.size() + " / " + all.size() + " nhóm chat (Tra cứu chỉ đọc)"
            );
        }
        updateSelectionStatus();
    }

    private void updateSelectionStatus() {
        if (selectionStatusLabel == null) {
            return;
        }
        int row = groupTable.getSelectedRow();
        if (row < 0 || row >= tableModel.getRowCount()) {
            selectionStatusLabel.setText(
                    "Trạng thái chọn: Chưa chọn nhóm chat nào (No-selection) — Chọn một nhóm để xem Thành viên hoặc Admin nhóm."
            );
            selectionStatusLabel.setForeground(AppTheme.TEXT_SECONDARY);
        } else {
            ChatGroupRecord g = tableModel.getGroupAt(row);
            selectionStatusLabel.setText(
                    "Đang chọn nhóm: " + g.getGroupName()
                            + "  •  Người tạo: " + g.getCreatorDisplay()
                            + "  •  Thành viên hiện tại: " + g.getCurrentMemberCount()
                            + " (" + g.getCurrentAdminCount() + " Admin)"
            );
            selectionStatusLabel.setForeground(AppTheme.PRIMARY);
        }
    }

    private void openSelectedGroupDialog(boolean adminsOnly) {
        int row = groupTable.getSelectedRow();
        if (row < 0 || row >= tableModel.getRowCount()) {
            selectionStatusLabel.setText(
                    "Lưu ý: Vui lòng chọn một nhóm chat trong bảng trước khi mở hộp thoại "
                            + (adminsOnly ? "Admin nhóm." : "Thành viên hiện tại.")
            );
            selectionStatusLabel.setForeground(AppTheme.WARNING);
            return;
        }
        openGroupDialogForRow(row, adminsOnly);
    }

    private void openGroupDialogForRow(int row, boolean adminsOnly) {
        ChatGroupRecord group = tableModel.getGroupAt(row);
        if (group == null) {
            return;
        }
        List<GroupMemberRecord> members = adminsOnly
                ? userRepository.getCurrentAdminsForGroup(group.getId())
                : userRepository.getCurrentMembersForGroup(group.getId());
        Window owner = SwingUtilities.getWindowAncestor(this);
        GroupMembersDialog dialog = new GroupMembersDialog(owner, group, members, adminsOnly);
        dialog.setVisible(true);
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

    private static final class EncryptionBadgeRenderer extends JPanel implements javax.swing.table.TableCellRenderer {

        private boolean encrypted;
        private boolean rowSelected;

        private EncryptionBadgeRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            String str = value != null ? value.toString() : "";
            this.encrypted = str.contains("E2EE");
            this.rowSelected = isSelected;
            setBackground(isSelected ? AppTheme.PRIMARY_SOFT_BG : AppTheme.SURFACE_CARD);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            String text = encrypted ? "Đã bật mã hóa (E2EE)" : "Tiêu chuẩn (Plaintext)";
            Color bg = encrypted ? AppTheme.SUCCESS_BG : AppTheme.NEUTRAL_BG;
            Color fg = encrypted ? AppTheme.SUCCESS : AppTheme.TEXT_SECONDARY;

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

    private static final class GroupActionButtonsRenderer extends JPanel implements javax.swing.table.TableCellRenderer {

        private boolean rowSelected;

        private GroupActionButtonsRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
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

            String leftText = "Thành viên";
            String rightText = "Admin";
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
            g2.setColor(AppTheme.PRIMARY_SOFT_BG);
            g2.fillRoundRect(rightX, y, rightW, btnH, 6, 6);
            g2.setColor(new Color(0xBF, 0xDB, 0xFE));
            g2.drawRoundRect(rightX, y, rightW - 1, btnH - 1, 6, 6);
            g2.setColor(AppTheme.PRIMARY);
            g2.drawString(rightText, rightX + 8, textY);

            if (!rowSelected) {
                g2.setColor(AppTheme.BORDER);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
            g2.dispose();
        }
    }

    private static final class ChatGroupTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {
                "STT",
                "Tên nhóm chat",
                "Thời gian tạo",
                "Người tạo nhóm",
                "Thành viên hiện tại",
                "Chỉ báo mã hóa",
                "Tra cứu (Chỉ đọc)"
        };

        private final List<ChatGroupRecord> groups;

        private ChatGroupTableModel(List<ChatGroupRecord> initialGroups) {
            this.groups = new ArrayList<>(initialGroups);
        }

        public void setGroups(List<ChatGroupRecord> updated) {
            this.groups.clear();
            if (updated != null) {
                this.groups.addAll(updated);
            }
            fireTableDataChanged();
        }

        public ChatGroupRecord getGroupAt(int rowIndex) {
            if (rowIndex < 0 || rowIndex >= groups.size()) {
                return null;
            }
            return groups.get(rowIndex);
        }

        @Override
        public int getRowCount() {
            return groups.size();
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
            ChatGroupRecord g = groups.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> rowIndex + 1;
                case 1 -> g.getGroupName();
                case 2 -> g.getFormattedCreatedAt();
                case 3 -> g.getCreatorDisplay();
                case 4 -> g.getCurrentMemberCount() + " thành viên (" + g.getCurrentAdminCount() + " Admin)";
                case 5 -> g.getEncryptionLabel();
                case 6 -> "Thành viên | Admin";
                default -> "";
            };
        }
    }
}
