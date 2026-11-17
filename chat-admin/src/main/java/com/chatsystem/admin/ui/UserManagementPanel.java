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

/**
 * Main User Management screen panel (Phase 3).
 * Displays page intro, 4 summary stat cards, search/filter/sort toolbar,
 * styled 24-row Vietnamese mock user JTable with status badges and row actions,
 * and a visual pagination bar.
 */
public class UserManagementPanel extends JPanel {

    private static final String[] STATUS_FILTER_OPTIONS = {
            "Tất cả trạng thái",
            "Đang hoạt động",
            "Ngoại tuyến",
            "Đã khóa"
    };

    private static final String[] SORT_OPTIONS = {
            "Tên A–Z",
            "Tên Z–A",
            "Mới nhất",
            "Cũ nhất"
    };

    private final MockUserRepository userRepository;
    private final UserTableModel tableModel;
    private final JTable userTable;

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

        StatCard totalCard = new StatCard(
                "Tổng người dùng",
                userRepository.getTotalCount(),
                "Tổng số tài khoản",
                AppTheme.PRIMARY,
                AppTheme.PRIMARY_SOFT_BG
        );

        StatCard onlineCard = new StatCard(
                "Đang hoạt động",
                userRepository.getActiveOnlineCount(),
                "Đang trực tuyến",
                AppTheme.SUCCESS,
                AppTheme.SUCCESS_BG
        );

        StatCard offlineCard = new StatCard(
                "Ngoại tuyến",
                userRepository.getOfflineCount(),
                "Tạm ngắt kết nối",
                AppTheme.TEXT_SECONDARY,
                AppTheme.NEUTRAL_BG
        );

        StatCard lockedCard = new StatCard(
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

        // Search input field
        JTextField searchField = new JTextField();
        searchField.setFont(AppTheme.FONT_BODY);
        searchField.setForeground(AppTheme.TEXT_PRIMARY);
        searchField.setPreferredSize(new Dimension(260, AppTheme.CONTROL_HEIGHT));
        searchField.setMinimumSize(new Dimension(160, AppTheme.CONTROL_HEIGHT));
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo tên hoặc tên đăng nhập...");

        // Status filter dropdown
        JComboBox<String> statusCombo = new JComboBox<>(STATUS_FILTER_OPTIONS);
        statusCombo.setFont(AppTheme.FONT_BODY);
        statusCombo.setPreferredSize(new Dimension(164, AppTheme.CONTROL_HEIGHT));
        statusCombo.setMinimumSize(new Dimension(158, AppTheme.CONTROL_HEIGHT));
        statusCombo.setToolTipText("Lọc theo trạng thái tài khoản");

        // Sort dropdown
        JComboBox<String> sortCombo = new JComboBox<>(SORT_OPTIONS);
        sortCombo.setFont(AppTheme.FONT_BODY);
        sortCombo.setPreferredSize(new Dimension(126, AppTheme.CONTROL_HEIGHT));
        sortCombo.setMinimumSize(new Dimension(118, AppTheme.CONTROL_HEIGHT));
        sortCombo.setToolTipText("Sắp xếp danh sách người dùng");

        // Refresh button
        JButton refreshButton = new JButton("Làm mới");
        refreshButton.setFont(AppTheme.FONT_BODY_BOLD);
        refreshButton.setForeground(AppTheme.TEXT_PRIMARY);
        refreshButton.setBackground(AppTheme.SURFACE_CARD);
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshButton.setMargin(new Insets(0, 10, 0, 10));
        refreshButton.setPreferredSize(new Dimension(96, AppTheme.CONTROL_HEIGHT));
        refreshButton.setMinimumSize(new Dimension(90, AppTheme.CONTROL_HEIGHT));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, AppTheme.SPACING_SM + 2);

        // Search takes available horizontal space
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        toolbar.add(searchField, gbc);

        // Status filter label + combo
        JLabel statusLabel = new JLabel("Trạng thái:");
        statusLabel.setFont(AppTheme.FONT_CAPTION_BOLD);
        statusLabel.setForeground(AppTheme.TEXT_SECONDARY);
        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, AppTheme.SPACING_SM, 0, 6);
        toolbar.add(statusLabel, gbc);

        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, AppTheme.SPACING_SM + 2);
        toolbar.add(statusCombo, gbc);

        // Sort label + combo
        JLabel sortLabel = new JLabel("Sắp xếp:");
        sortLabel.setFont(AppTheme.FONT_CAPTION_BOLD);
        sortLabel.setForeground(AppTheme.TEXT_SECONDARY);
        gbc.gridx = 3;
        gbc.insets = new Insets(0, 4, 0, 6);
        toolbar.add(sortLabel, gbc);

        gbc.gridx = 4;
        gbc.insets = new Insets(0, 0, 0, AppTheme.SPACING_SM + 2);
        toolbar.add(sortCombo, gbc);

        // Refresh button at right edge
        gbc.gridx = 5;
        gbc.insets = new Insets(0, 0, 0, 0);
        toolbar.add(refreshButton, gbc);

        return toolbar;
    }

    private JScrollPane createTableScrollPane() {
        configureTableAppearance();

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(AppTheme.SURFACE_CARD);
        return scrollPane;
    }

    private void configureTableAppearance() {
        userTable.setFont(AppTheme.FONT_BODY);
        userTable.setForeground(AppTheme.TEXT_PRIMARY);
        userTable.setBackground(AppTheme.SURFACE_CARD);
        userTable.setRowHeight(AppTheme.TABLE_ROW_HEIGHT);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setSelectionBackground(AppTheme.PRIMARY_SOFT_BG);
        userTable.setSelectionForeground(AppTheme.TEXT_PRIMARY);
        userTable.setShowHorizontalLines(true);
        userTable.setShowVerticalLines(false);
        userTable.setGridColor(AppTheme.BORDER);
        userTable.setIntercellSpacing(new Dimension(0, 1));
        userTable.setFillsViewportHeight(true);

        JTableHeader header = userTable.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 40));
        header.setFont(AppTheme.FONT_CAPTION_BOLD);
        header.setBackground(AppTheme.PAGE_BG);
        header.setForeground(AppTheme.TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER));

        // Custom header cell alignment
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
            ) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column
                );
                label.setFont(AppTheme.FONT_CAPTION_BOLD);
                label.setForeground(AppTheme.TEXT_SECONDARY);
                label.setBackground(AppTheme.PAGE_BG);
                if (column == UserTableModel.COL_INDEX
                        || column == UserTableModel.COL_CREATED_AT
                        || column == UserTableModel.COL_STATUS
                        || column == UserTableModel.COL_ACTIONS) {
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
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

        // Standard text cell renderers
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
        columnModel.getColumn(UserTableModel.COL_STATUS).setCellRenderer(new StatusBadgeCellRenderer());
        columnModel.getColumn(UserTableModel.COL_ACTIONS).setCellRenderer(new ActionControlCellRenderer());

        // Column widths optimized for both 1280x800 and 1100x700
        columnModel.getColumn(UserTableModel.COL_INDEX).setMinWidth(42);
        columnModel.getColumn(UserTableModel.COL_INDEX).setPreferredWidth(46);
        columnModel.getColumn(UserTableModel.COL_INDEX).setMaxWidth(54);

        columnModel.getColumn(UserTableModel.COL_USERNAME).setMinWidth(110);
        columnModel.getColumn(UserTableModel.COL_USERNAME).setPreferredWidth(128);

        columnModel.getColumn(UserTableModel.COL_FULL_NAME).setMinWidth(144);
        columnModel.getColumn(UserTableModel.COL_FULL_NAME).setPreferredWidth(168);

        columnModel.getColumn(UserTableModel.COL_EMAIL).setMinWidth(130);
        columnModel.getColumn(UserTableModel.COL_EMAIL).setPreferredWidth(205);

        columnModel.getColumn(UserTableModel.COL_CREATED_AT).setMinWidth(92);
        columnModel.getColumn(UserTableModel.COL_CREATED_AT).setPreferredWidth(100);
        columnModel.getColumn(UserTableModel.COL_CREATED_AT).setMaxWidth(114);

        columnModel.getColumn(UserTableModel.COL_STATUS).setMinWidth(126);
        columnModel.getColumn(UserTableModel.COL_STATUS).setPreferredWidth(134);
        columnModel.getColumn(UserTableModel.COL_STATUS).setMaxWidth(148);

        columnModel.getColumn(UserTableModel.COL_ACTIONS).setMinWidth(146);
        columnModel.getColumn(UserTableModel.COL_ACTIONS).setPreferredWidth(152);
        columnModel.getColumn(UserTableModel.COL_ACTIONS).setMaxWidth(166);

        // Select row 0 initially for visual polish
        if (tableModel.getRowCount() > 0) {
            userTable.setRowSelectionInterval(0, 0);
        }

        // Hand cursor & click handling on "Thao tác" column
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
                    // Left pill ("Sửa") opens UserFormDialog; right pill ("Thao tác ▾") opens action menu
                    if (relativeX < cellRect.width * 0.42) {
                        openUserFormDialog(selectedUser);
                    } else {
                        showRowActionMenu(userTable, cellRect.x + 8, cellRect.y + cellRect.height, selectedUser);
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

        JMenuItem editItem = createActionMenuItem("Sửa");
        editItem.addActionListener(e -> openUserFormDialog(user));

        JMenuItem deleteItem = createActionMenuItem("Xóa");
        deleteItem.setForeground(AppTheme.DANGER);

        JMenuItem lockToggleItem = createActionMenuItem("Khóa/Mở khóa");
        JMenuItem changePasswordItem = createActionMenuItem("Đổi mật khẩu");
        JMenuItem loginHistoryItem = createActionMenuItem("Lịch sử đăng nhập");
        JMenuItem friendListItem = createActionMenuItem("Danh sách bạn bè");

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
        JLabel recordCountLabel = new JLabel(
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
        return new JButton(text) {
            private boolean hovered = false;

            {
                setFont(AppTheme.FONT_BODY_BOLD);
                setForeground(AppTheme.SURFACE_CARD);
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? AppTheme.PRIMARY_HOVER : AppTheme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.CORNER_RADIUS, AppTheme.CORNER_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
    }

    private void openUserFormDialog(User user) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        UserFormDialog dialog = new UserFormDialog(owner, user);
        dialog.setVisible(true);
    }

    /**
     * Renders account status ("Đang hoạt động", "Ngoại tuyến", "Đã khóa") as a rounded badge pill.
     */
    private static final class StatusBadgeCellRenderer extends JPanel implements TableCellRenderer {

        private String statusText = "";
        private Color pillBg = AppTheme.NEUTRAL_BG;
        private Color pillFg = AppTheme.TEXT_SECONDARY;

        private StatusBadgeCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            statusText = value != null ? value.toString() : "";
            setBackground(isSelected ? AppTheme.PRIMARY_SOFT_BG : AppTheme.SURFACE_CARD);

            if (User.STATUS_LOCKED.equals(statusText)) {
                pillBg = AppTheme.DANGER_BG;
                pillFg = AppTheme.DANGER;
            } else if (User.STATUS_ONLINE.equals(statusText)) {
                pillBg = AppTheme.SUCCESS_BG;
                pillFg = AppTheme.SUCCESS;
            } else {
                pillBg = AppTheme.NEUTRAL_BG;
                pillFg = AppTheme.TEXT_SECONDARY;
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
            int dotDiameter = 6;
            int dotGap = 6;
            int textWidth = fm.stringWidth(statusText);
            int contentWidth = dotDiameter + dotGap + textWidth;
            int badgeWidth = contentWidth + 20;
            int badgeHeight = 26;

            int x = (getWidth() - badgeWidth) / 2;
            int y = (getHeight() - badgeHeight) / 2;

            g2.setColor(pillBg);
            g2.fillRoundRect(x, y, badgeWidth, badgeHeight, 13, 13);

            int startX = x + 10;
            int dotY = y + (badgeHeight - dotDiameter) / 2;
            g2.setColor(pillFg);
            g2.fillOval(startX, dotY, dotDiameter, dotDiameter);

            int textX = startX + dotDiameter + dotGap;
            int textY = y + ((badgeHeight - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(statusText, textX, textY);

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

            // Paint a small downward chevron next to "Thao tác"
            int cx = menuTextX + fm.stringWidth(menuText) + 6;
            int cy = pillY + (pillH / 2) - 1;
            g2.drawLine(cx, cy, cx + 3, cy + 3);
            g2.drawLine(cx + 3, cy + 3, cx + 6, cy);

            g2.dispose();
        }
    }
}
