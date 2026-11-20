package com.chatsystem.admin.ui;

import com.chatsystem.admin.components.StatCard;
import com.chatsystem.admin.data.MockUserRepository;
import com.chatsystem.admin.model.User;
import com.chatsystem.admin.table.UserTableModel;
import com.chatsystem.admin.theme.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
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
 * Main User Management screen panel.
 * Displays page intro, 4 summary stat cards, search/filter/sort toolbar with
 * distinct Account Status and Online Presence controls, styled Vietnamese mock user JTable
 * with dual status badges and row actions, and a pagination bar.
 */
public class UserManagementPanel extends JPanel {

    private static final String[] ACCOUNT_STATUS_OPTIONS = {
            "Tất cả TK",
            "Hoạt động",
            "Đã khóa"
    };

    private static final String[] PRESENCE_FILTER_OPTIONS = {
            "Tất cả phiên",
            "Trực tuyến",
            "Ngoại tuyến"
    };

    private static final String[] SORT_OPTIONS = {
            "Cũ nhất",
            "Mới nhất",
            "Tên A–Z",
            "Tên Z–A"
    };

    private final MockUserRepository userRepository;
    private final UserTableModel tableModel;
    private final JTable userTable;

    private StatCard totalCard;
    private StatCard onlineCard;
    private StatCard offlineCard;
    private StatCard lockedCard;

    private JTextField searchField;
    private JComboBox<String> accountStatusCombo;
    private JComboBox<String> presenceCombo;
    private JComboBox<String> sortCombo;
    private JLabel recordCountLabel;

    public UserManagementPanel(MockUserRepository userRepository) {
        super(new BorderLayout(0, AppTheme.SPACING_MD));
        this.userRepository = userRepository;
        this.tableModel = new UserTableModel(userRepository.getUsers());
        this.userTable = new JTable(tableModel);

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

        JLabel titleLabel = new JLabel("Quản lý người dùng");
        titleLabel.setFont(AppTheme.FONT_PAGE_TITLE);
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Quản lý tài khoản và thông tin người dùng trong hệ thống");
        subtitleLabel.setFont(AppTheme.FONT_BODY);
        subtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textGroup.add(titleLabel);
        textGroup.add(Box.createVerticalStrut(4));
        textGroup.add(subtitleLabel);

        JPanel buttonWrapper = new JPanel(new GridBagLayout());
        buttonWrapper.setOpaque(false);

        JButton addUserButton = createPrimaryActionButton("+ Thêm người dùng");
        addUserButton.setPreferredSize(new Dimension(176, AppTheme.CONTROL_HEIGHT));
        addUserButton.addActionListener(e -> openUserFormDialog(null));
        buttonWrapper.add(addUserButton);

        introPanel.add(textGroup, BorderLayout.WEST);
        introPanel.add(buttonWrapper, BorderLayout.EAST);
        return introPanel;
    }

    private JPanel createSummaryCardsRow() {
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, AppTheme.SPACING_MD, 0));
        cardsGrid.setOpaque(false);

        totalCard = new StatCard(
                "Tổng người dùng",
                userRepository.getTotalCount(),
                "Tổng số tài khoản",
                AppTheme.PRIMARY,
                AppTheme.PRIMARY_SOFT_BG
        );

        onlineCard = new StatCard(
                "Đang hoạt động",
                userRepository.getActiveOnlineCount(),
                "Đang trực tuyến",
                AppTheme.SUCCESS,
                AppTheme.SUCCESS_BG
        );

        offlineCard = new StatCard(
                "Ngoại tuyến",
                userRepository.getOfflineCount(),
                "Tạm ngắt kết nối",
                AppTheme.TEXT_SECONDARY,
                AppTheme.NEUTRAL_BG
        );

        lockedCard = new StatCard(
                "Tài khoản bị khóa",
                userRepository.getLockedCount(),
                "Đang bị hạn chế",
                AppTheme.DANGER,
                AppTheme.DANGER_BG
        );

        cardsGrid.add(totalCard);
        cardsGrid.add(onlineCard);
        cardsGrid.add(offlineCard);
        cardsGrid.add(lockedCard);

        return cardsGrid;
    }

    private JPanel createMainTableCard() {
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(AppTheme.SURFACE_CARD);
        tableCard.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1, true));

        tableCard.add(createFilterToolbar(), BorderLayout.NORTH);
        tableCard.add(createTableScrollPane(), BorderLayout.CENTER);
        tableCard.add(createPaginationFooter(), BorderLayout.SOUTH);

        return tableCard;
    }

    private JPanel createFilterToolbar() {
        JPanel toolbar = new JPanel(new GridBagLayout());
        toolbar.setBackground(AppTheme.SURFACE_CARD);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER),
                BorderFactory.createEmptyBorder(
                        AppTheme.SPACING_MD - 2,
                        AppTheme.SPACING_MD,
                        AppTheme.SPACING_MD - 2,
                        AppTheme.SPACING_MD
                )
        ));

        // Search input field (live filtering by fullName or username)
        searchField = new JTextField();
        searchField.setFont(AppTheme.FONT_BODY);
        searchField.setForeground(AppTheme.TEXT_PRIMARY);
        searchField.setPreferredSize(new Dimension(210, AppTheme.CONTROL_HEIGHT));
        searchField.setMinimumSize(new Dimension(140, AppTheme.CONTROL_HEIGHT));
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo tên hoặc tên đăng nhập...");
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

        // Account status filter dropdown (Hoạt động vs Đã khóa)
        accountStatusCombo = new JComboBox<>(ACCOUNT_STATUS_OPTIONS);
        accountStatusCombo.setFont(AppTheme.FONT_BODY);
        accountStatusCombo.setPreferredSize(new Dimension(118, AppTheme.CONTROL_HEIGHT));
        accountStatusCombo.setMinimumSize(new Dimension(110, AppTheme.CONTROL_HEIGHT));
        accountStatusCombo.setToolTipText("Lọc theo trạng thái khóa/mở của tài khoản");
        accountStatusCombo.addActionListener(e -> applyFiltersAndSort());

        // Online presence filter dropdown (Trực tuyến vs Ngoại tuyến)
        presenceCombo = new JComboBox<>(PRESENCE_FILTER_OPTIONS);
        presenceCombo.setFont(AppTheme.FONT_BODY);
        presenceCombo.setPreferredSize(new Dimension(126, AppTheme.CONTROL_HEIGHT));
        presenceCombo.setMinimumSize(new Dimension(118, AppTheme.CONTROL_HEIGHT));
        presenceCombo.setToolTipText("Lọc theo trạng thái phiên kết nối trực tuyến/ngoại tuyến");
        presenceCombo.addActionListener(e -> applyFiltersAndSort());

        // Sort dropdown
        sortCombo = new JComboBox<>(SORT_OPTIONS);
        sortCombo.setFont(AppTheme.FONT_BODY);
        sortCombo.setPreferredSize(new Dimension(116, AppTheme.CONTROL_HEIGHT));
        sortCombo.setMinimumSize(new Dimension(110, AppTheme.CONTROL_HEIGHT));
        sortCombo.setToolTipText("Sắp xếp danh sách theo họ tên hoặc ngày tạo");
        sortCombo.addActionListener(e -> applyFiltersAndSort());

        // Refresh / reset filters button
        JButton refreshButton = AppTheme.createSecondaryButton("Làm mới", 88);
        refreshButton.addActionListener(e -> resetFilters());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;

        // 0: Search field takes remaining width
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 8);
        toolbar.add(searchField, gbc);

        // 1-2: Account status label + combo
        JLabel accountLabel = new JLabel("Tài khoản:");
        accountLabel.setFont(AppTheme.FONT_CAPTION_BOLD);
        accountLabel.setForeground(AppTheme.TEXT_SECONDARY);
        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 2, 0, 4);
        toolbar.add(accountLabel, gbc);

        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 8);
        toolbar.add(accountStatusCombo, gbc);

        // 3-4: Presence label + combo
        JLabel presenceLabel = new JLabel("Phiên:");
        presenceLabel.setFont(AppTheme.FONT_CAPTION_BOLD);
        presenceLabel.setForeground(AppTheme.TEXT_SECONDARY);
        gbc.gridx = 3;
        gbc.insets = new Insets(0, 2, 0, 4);
        toolbar.add(presenceLabel, gbc);

        gbc.gridx = 4;
        gbc.insets = new Insets(0, 0, 0, 8);
        toolbar.add(presenceCombo, gbc);

        // 5-6: Sort label + combo
        JLabel sortLabel = new JLabel("Sắp xếp:");
        sortLabel.setFont(AppTheme.FONT_CAPTION_BOLD);
        sortLabel.setForeground(AppTheme.TEXT_SECONDARY);
        gbc.gridx = 5;
        gbc.insets = new Insets(0, 2, 0, 4);
        toolbar.add(sortLabel, gbc);

        gbc.gridx = 6;
        gbc.insets = new Insets(0, 0, 0, 8);
        toolbar.add(sortCombo, gbc);

        // 7: Refresh button
        gbc.gridx = 7;
        gbc.insets = new Insets(0, 0, 0, 0);
        toolbar.add(refreshButton, gbc);

        return toolbar;
    }

    private void resetFilters() {
        searchField.setText("");
        accountStatusCombo.setSelectedIndex(0);
        presenceCombo.setSelectedIndex(0);
        sortCombo.setSelectedIndex(0);
        applyFiltersAndSort();
    }

    private void applyFiltersAndSort() {
        String keyword = searchField != null ? searchField.getText().trim().toLowerCase(Locale.ROOT) : "";
        String accountFilter = accountStatusCombo != null
                ? (String) accountStatusCombo.getSelectedItem()
                : ACCOUNT_STATUS_OPTIONS[0];
        String presenceFilter = presenceCombo != null
                ? (String) presenceCombo.getSelectedItem()
                : PRESENCE_FILTER_OPTIONS[0];
        String sortOption = sortCombo != null
                ? (String) sortCombo.getSelectedItem()
                : SORT_OPTIONS[0];

        List<User> filtered = new ArrayList<>();
        for (User u : userRepository.getUsers()) {
            if (!keyword.isEmpty()) {
                boolean matchesName = u.getFullName().toLowerCase(Locale.ROOT).contains(keyword);
                boolean matchesUser = u.getUsername().toLowerCase(Locale.ROOT).contains(keyword);
                if (!matchesName && !matchesUser) {
                    continue;
                }
            }
            if ("Hoạt động".equals(accountFilter) && u.isLocked()) {
                continue;
            }
            if ("Đã khóa".equals(accountFilter) && !u.isLocked()) {
                continue;
            }
            if ("Trực tuyến".equals(presenceFilter) && !u.isOnline()) {
                continue;
            }
            if ("Ngoại tuyến".equals(presenceFilter) && u.isOnline()) {
                continue;
            }
            filtered.add(u);
        }

        Collator viCollator = Collator.getInstance(Locale.of("vi", "VN"));
        Comparator<User> comparator = switch (sortOption != null ? sortOption : SORT_OPTIONS[0]) {
            case "Tên A–Z" -> (a, b) -> {
                int cmp = viCollator.compare(a.getFullName(), b.getFullName());
                return cmp != 0 ? cmp : Long.compare(a.getId(), b.getId());
            };
            case "Tên Z–A" -> (a, b) -> {
                int cmp = viCollator.compare(b.getFullName(), a.getFullName());
                return cmp != 0 ? cmp : Long.compare(a.getId(), b.getId());
            };
            case "Mới nhất" -> Comparator.comparing(User::getCreatedAt).reversed()
                    .thenComparing(Comparator.comparingLong(User::getId).reversed());
            case "Cũ nhất" -> Comparator.comparing(User::getCreatedAt)
                    .thenComparingLong(User::getId);
            default -> Comparator.comparingLong(User::getId);
        };
        filtered.sort(comparator);

        tableModel.setUsers(filtered);
        if (tableModel.getRowCount() > 0) {
            userTable.setRowSelectionInterval(0, 0);
        }

        refreshSummaryCardsAndFooter(filtered.size());
    }

    private void refreshSummaryCardsAndFooter(int displayedCount) {
        if (totalCard != null) {
            totalCard.setCount(userRepository.getTotalCount());
            onlineCard.setCount(userRepository.getActiveOnlineCount());
            offlineCard.setCount(userRepository.getOfflineCount());
            lockedCard.setCount(userRepository.getLockedCount());
        }
        if (recordCountLabel != null) {
            int total = userRepository.getTotalCount();
            if (displayedCount == 0) {
                recordCountLabel.setText("Không tìm thấy người dùng phù hợp (Tổng hệ thống: " + total + ")");
            } else {
                recordCountLabel.setText(
                        "Hiển thị 1–" + displayedCount + " trên tổng số " + total + " người dùng  •  Trang 1 / 2"
                );
            }
        }
    }

    private JScrollPane createTableScrollPane() {
        configureTableAppearance();

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(AppTheme.SURFACE_CARD);
        return scrollPane;
    }

    private void configureTableAppearance() {
        AppTheme.configureSharedTable(userTable);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
            ) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column
                );
                label.setFont(AppTheme.FONT_CAPTION_BOLD);
                label.setForeground(AppTheme.TEXT_PRIMARY);
                label.setBackground(AppTheme.SURFACE_CARD);
                if (column == UserTableModel.COL_INDEX
                        || column == UserTableModel.COL_CREATED_AT
                        || column == UserTableModel.COL_STATUS
                        || column == UserTableModel.COL_ACTIONS) {
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                } else {
                    label.setHorizontalAlignment(SwingConstants.LEFT);
                    label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
                }
                return label;
            }
        };
        for (int i = 0; i < userTable.getColumnCount(); i++) {
            userTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        DefaultTableCellRenderer centerCellRenderer = createPaddedRenderer(SwingConstants.CENTER, AppTheme.FONT_BODY, AppTheme.TEXT_SECONDARY);
        DefaultTableCellRenderer usernameRenderer = createPaddedRenderer(SwingConstants.LEFT, AppTheme.FONT_BODY, AppTheme.TEXT_PRIMARY);
        DefaultTableCellRenderer boldNameRenderer = createPaddedRenderer(SwingConstants.LEFT, AppTheme.FONT_BODY_BOLD, AppTheme.TEXT_PRIMARY);
        DefaultTableCellRenderer emailRenderer = createPaddedRenderer(SwingConstants.LEFT, AppTheme.FONT_BODY, AppTheme.TEXT_SECONDARY);

        TableColumnModel columnModel = userTable.getColumnModel();
        columnModel.getColumn(UserTableModel.COL_INDEX).setCellRenderer(centerCellRenderer);
        columnModel.getColumn(UserTableModel.COL_USERNAME).setCellRenderer(usernameRenderer);
        columnModel.getColumn(UserTableModel.COL_FULL_NAME).setCellRenderer(boldNameRenderer);
        columnModel.getColumn(UserTableModel.COL_EMAIL).setCellRenderer(emailRenderer);
        columnModel.getColumn(UserTableModel.COL_CREATED_AT).setCellRenderer(centerCellRenderer);
        columnModel.getColumn(UserTableModel.COL_STATUS).setCellRenderer(new DualStatusBadgeCellRenderer());
        columnModel.getColumn(UserTableModel.COL_ACTIONS).setCellRenderer(new ActionControlCellRenderer());

        columnModel.getColumn(UserTableModel.COL_INDEX).setMinWidth(38);
        columnModel.getColumn(UserTableModel.COL_INDEX).setPreferredWidth(42);
        columnModel.getColumn(UserTableModel.COL_INDEX).setMaxWidth(48);

        columnModel.getColumn(UserTableModel.COL_USERNAME).setMinWidth(100);
        columnModel.getColumn(UserTableModel.COL_USERNAME).setPreferredWidth(120);

        columnModel.getColumn(UserTableModel.COL_FULL_NAME).setMinWidth(132);
        columnModel.getColumn(UserTableModel.COL_FULL_NAME).setPreferredWidth(158);

        columnModel.getColumn(UserTableModel.COL_EMAIL).setMinWidth(114);
        columnModel.getColumn(UserTableModel.COL_EMAIL).setPreferredWidth(185);

        columnModel.getColumn(UserTableModel.COL_CREATED_AT).setMinWidth(86);
        columnModel.getColumn(UserTableModel.COL_CREATED_AT).setPreferredWidth(94);
        columnModel.getColumn(UserTableModel.COL_CREATED_AT).setMaxWidth(106);

        columnModel.getColumn(UserTableModel.COL_STATUS).setMinWidth(156);
        columnModel.getColumn(UserTableModel.COL_STATUS).setPreferredWidth(164);
        columnModel.getColumn(UserTableModel.COL_STATUS).setMaxWidth(180);

        columnModel.getColumn(UserTableModel.COL_ACTIONS).setMinWidth(148);
        columnModel.getColumn(UserTableModel.COL_ACTIONS).setPreferredWidth(152);
        columnModel.getColumn(UserTableModel.COL_ACTIONS).setMaxWidth(164);

        if (tableModel.getRowCount() > 0) {
            userTable.setRowSelectionInterval(0, 0);
        }

        userTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = userTable.columnAtPoint(e.getPoint());
                if (col == UserTableModel.COL_ACTIONS) {
                    userTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    userTable.setCursor(Cursor.getDefaultCursor());
                }
            }
        });

        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point point = e.getPoint();
                int row = userTable.rowAtPoint(point);
                int col = userTable.columnAtPoint(point);
                if (row >= 0 && col == UserTableModel.COL_ACTIONS) {
                    userTable.setRowSelectionInterval(row, row);
                    Rectangle cellRect = userTable.getCellRect(row, col, false);
                    int relativeX = point.x - cellRect.x;
                    User selectedUser = tableModel.getUserAt(row);
                    if (selectedUser != null) {
                        if (relativeX < cellRect.width * 0.42) {
                            openUserFormDialog(selectedUser);
                        } else {
                            showRowActionMenu(userTable, cellRect.x + 8, cellRect.y + cellRect.height, selectedUser);
                        }
                    }
                }
            }
        });
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

    private void showRowActionMenu(Component invoker, int x, int y, User user) {
        JPopupMenu actionMenu = new JPopupMenu();
        actionMenu.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));

        JMenuItem editItem = createActionMenuItem("Sửa thông tin");
        editItem.addActionListener(e -> openUserFormDialog(user));

        JMenuItem deleteItem = createActionMenuItem("Xóa tài khoản");
        deleteItem.setForeground(AppTheme.DANGER);
        deleteItem.addActionListener(e -> openDeleteConfirmDialog(user));

        String lockLabel = user.isLocked() ? "Mở khóa tài khoản" : "Khóa tài khoản";
        JMenuItem lockToggleItem = createActionMenuItem(lockLabel);
        lockToggleItem.addActionListener(e -> openLockToggleConfirmDialog(user));

        JMenuItem changePasswordItem = createActionMenuItem("Đổi mật khẩu");
        changePasswordItem.addActionListener(e -> openPasswordUpdateDialog(user));

        JMenuItem loginHistoryItem = createActionMenuItem("Lịch sử đăng nhập");
        loginHistoryItem.addActionListener(e -> openUserLoginHistoryDialog(user));

        JMenuItem friendListItem = createActionMenuItem("Danh sách bạn bè");
        friendListItem.addActionListener(e -> openUserFriendsDialog(user));

        actionMenu.add(editItem);
        actionMenu.add(deleteItem);
        actionMenu.addSeparator();
        actionMenu.add(lockToggleItem);
        actionMenu.add(changePasswordItem);
        actionMenu.addSeparator();
        actionMenu.add(loginHistoryItem);
        actionMenu.add(friendListItem);

        actionMenu.show(invoker, x, y);
    }

    private JMenuItem createActionMenuItem(String label) {
        JMenuItem item = new JMenuItem(label);
        item.setFont(AppTheme.FONT_BODY);
        item.setForeground(AppTheme.TEXT_PRIMARY);
        item.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 16));
        return item;
    }

    private JPanel createPaginationFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AppTheme.SURFACE_CARD);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER),
                BorderFactory.createEmptyBorder(
                        AppTheme.SPACING_SM + 2,
                        AppTheme.SPACING_MD,
                        AppTheme.SPACING_SM + 2,
                        AppTheme.SPACING_MD
                )
        ));

        int total = userRepository.getTotalCount();
        recordCountLabel = new JLabel(
                "Hiển thị 1–" + total + " trên tổng số " + total + " người dùng  •  Trang 1 / 2"
        );
        recordCountLabel.setFont(AppTheme.FONT_CAPTION);
        recordCountLabel.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightControls.setOpaque(false);

        JButton prevBtn = createPageButton("« Trước", false, 72);
        prevBtn.setEnabled(false);
        JButton page1Btn = createPageButton("1", true, 34);
        JButton page2Btn = createPageButton("2", false, 34);
        JButton page3Btn = createPageButton("3", false, 34);
        JButton nextBtn = createPageButton("Sau »", false, 68);

        rightControls.add(prevBtn);
        rightControls.add(page1Btn);
        rightControls.add(page2Btn);
        rightControls.add(page3Btn);
        rightControls.add(nextBtn);

        footer.add(recordCountLabel, BorderLayout.WEST);
        footer.add(rightControls, BorderLayout.EAST);
        return footer;
    }

    private JButton createPageButton(String label, boolean active, int width) {
        return new JButton(label) {
            {
                setFont(active ? AppTheme.FONT_CAPTION_BOLD : AppTheme.FONT_CAPTION);
                setForeground(active ? AppTheme.SURFACE_CARD : AppTheme.TEXT_PRIMARY);
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setPreferredSize(new Dimension(width, 32));
                setMargin(new Insets(0, 4, 0, 4));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (active) {
                    g2.setColor(AppTheme.PRIMARY);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else {
                    g2.setColor(isEnabled() ? AppTheme.SURFACE_CARD : AppTheme.PAGE_BG);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                    g2.setColor(AppTheme.BORDER);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
    }

    private JButton createPrimaryActionButton(String text) {
        return AppTheme.createPrimaryButton(text, 176);
    }

    private void openUserFormDialog(User existingUser) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        UserFormDialog dialog = new UserFormDialog(
                owner,
                existingUser,
                (username, fullName, address, dateOfBirth, gender, email) -> {
                    if (existingUser == null) {
                        userRepository.addUser(username, fullName, address, dateOfBirth, gender, email);
                    } else {
                        userRepository.updateUser(
                                existingUser.getId(),
                                username,
                                fullName,
                                address,
                                dateOfBirth,
                                gender,
                                email
                        );
                    }
                    applyFiltersAndSort();
                }
        );
        dialog.setVisible(true);
    }

    private void openDeleteConfirmDialog(User user) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (ConfirmActionDialog.showDeleteUserConfirm(owner, user)) {
            userRepository.deleteUser(user.getId());
            applyFiltersAndSort();
        }
    }

    private void openLockToggleConfirmDialog(User user) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (ConfirmActionDialog.showLockToggleConfirm(owner, user)) {
            userRepository.toggleLockUser(user.getId());
            applyFiltersAndSort();
        }
    }

    private void openPasswordUpdateDialog(User user) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        PasswordUpdateDialog dialog = new PasswordUpdateDialog(owner, user);
        dialog.setVisible(true);
    }

    private void openUserLoginHistoryDialog(User user) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        UserLoginHistoryDialog dialog = new UserLoginHistoryDialog(
                owner,
                user,
                userRepository.getLoginHistoryForUser(user.getId())
        );
        dialog.setVisible(true);
    }

    private void openUserFriendsDialog(User user) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        UserFriendsDialog dialog = new UserFriendsDialog(
                owner,
                user,
                userRepository.getDirectFriendsForUser(user.getId())
        );
        dialog.setVisible(true);
    }

    /**
     * Renders both Account Status ("Hoạt động" vs "Đã khóa") and Online Presence
     * ("Online" vs "Offline") as two distinct side-by-side badge pills.
     */
    private static final class DualStatusBadgeCellRenderer extends JPanel implements TableCellRenderer {

        private boolean locked = false;
        private boolean online = false;

        private DualStatusBadgeCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            setBackground(isSelected ? AppTheme.PRIMARY_SOFT_BG : AppTheme.SURFACE_CARD);
            if (value instanceof User u) {
                this.locked = u.isLocked();
                this.online = u.isOnline();
                setToolTipText(
                        "Trạng thái tài khoản: " + u.getAccountStatusLabel()
                                + " | Phiên kết nối: " + u.getPresenceStatusLabel()
                );
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(AppTheme.FONT_CAPTION_BOLD);
            FontMetrics fm = g2.getFontMetrics();

            String acctText = locked ? "Đã khóa" : "Hoạt động";
            Color acctBg = locked ? AppTheme.DANGER_BG : AppTheme.PRIMARY_SOFT_BG;
            Color acctFg = locked ? AppTheme.DANGER : AppTheme.PRIMARY;

            String presText = online ? "Online" : "Offline";
            Color presBg = online ? AppTheme.SUCCESS_BG : AppTheme.NEUTRAL_BG;
            Color presFg = online ? AppTheme.SUCCESS : AppTheme.TEXT_SECONDARY;

            int badgeH = 24;
            int acctW = fm.stringWidth(acctText) + 16;
            int dotSize = 6;
            int dotGap = 5;
            int presW = dotSize + dotGap + fm.stringWidth(presText) + 16;
            int gap = 6;
            int totalW = acctW + gap + presW;

            int startX = (getWidth() - totalW) / 2;
            int y = (getHeight() - badgeH) / 2;
            int textY = y + ((badgeH - fm.getHeight()) / 2) + fm.getAscent();

            // Left pill: Account Status (Hoạt động / Đã khóa)
            g2.setColor(acctBg);
            g2.fillRoundRect(startX, y, acctW, badgeH, 12, 12);
            g2.setColor(acctFg);
            g2.drawString(acctText, startX + 8, textY);

            // Right pill: Online Presence (Online / Offline with status dot)
            int presX = startX + acctW + gap;
            g2.setColor(presBg);
            g2.fillRoundRect(presX, y, presW, badgeH, 12, 12);

            int dotX = presX + 8;
            int dotY = y + (badgeH - dotSize) / 2;
            g2.setColor(presFg);
            g2.fillOval(dotX, dotY, dotSize, dotSize);
            g2.drawString(presText, dotX + dotSize + dotGap, textY);

            g2.dispose();
        }
    }

    /**
     * Renders visible row action controls ("Sửa" and "Thao tác ▾") inside the "Thao tác" column.
     */
    private static final class ActionControlCellRenderer extends JPanel implements TableCellRenderer {

        private ActionControlCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            setBackground(isSelected ? AppTheme.PRIMARY_SOFT_BG : AppTheme.SURFACE_CARD);
            setToolTipText("Nhấn 'Sửa' hoặc 'Thao tác ▾' (Sửa, Xóa, Khóa/Mở khóa, Đổi mật khẩu, Lịch sử đăng nhập, Danh sách bạn bè)");
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(AppTheme.FONT_CAPTION_BOLD);
            FontMetrics fm = g2.getFontMetrics();

            String editText = "Sửa";
            String menuText = "Thao tác";

            int editW = fm.stringWidth(editText) + 16;
            int chevronSpace = 12;
            int menuW = fm.stringWidth(menuText) + 18 + chevronSpace;
            int gap = 6;
            int totalW = editW + gap + menuW;
            int pillH = 28;

            int startX = (getWidth() - totalW) / 2;
            int pillY = (getHeight() - pillH) / 2;
            int textBaseline = pillY + ((pillH - fm.getHeight()) / 2) + fm.getAscent();

            // "Sửa" pill
            g2.setColor(AppTheme.PRIMARY_SOFT_BG);
            g2.fillRoundRect(startX, pillY, editW, pillH, 8, 8);
            g2.setColor(AppTheme.PRIMARY);
            g2.drawRoundRect(startX, pillY, editW - 1, pillH - 1, 8, 8);
            g2.drawString(editText, startX + (editW - fm.stringWidth(editText)) / 2, textBaseline);

            // "Thao tác" + painted chevron pill
            int menuX = startX + editW + gap;
            g2.setColor(AppTheme.SURFACE_CARD);
            g2.fillRoundRect(menuX, pillY, menuW, pillH, 8, 8);
            g2.setColor(AppTheme.BORDER);
            g2.drawRoundRect(menuX, pillY, menuW - 1, pillH - 1, 8, 8);
            g2.setColor(AppTheme.TEXT_PRIMARY);
            int menuTextX = menuX + 9;
            g2.drawString(menuText, menuTextX, textBaseline);

            int cx = menuTextX + fm.stringWidth(menuText) + 6;
            int cy = pillY + (pillH / 2) - 1;
            g2.drawLine(cx, cy, cx + 3, cy + 3);
            g2.drawLine(cx + 3, cy + 3, cx + 6, cy);

            g2.dispose();
        }
    }
}
