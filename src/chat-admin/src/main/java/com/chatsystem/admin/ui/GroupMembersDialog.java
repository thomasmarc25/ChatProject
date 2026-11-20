package com.chatsystem.admin.ui;

import com.chatsystem.admin.model.ChatGroupRecord;
import com.chatsystem.admin.model.GroupMemberRecord;
import com.chatsystem.admin.theme.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.util.List;

/**
 * Read-only modal dialog displaying either all current members ({@code left_at IS NULL})
 * or current group administrators ({@code role = 'ADMIN' AND left_at IS NULL}) of a selected chat group.
 * Explicitly separates the group role ({@code group_members.role}) from the system role ({@code users.role}).
 */
public class GroupMembersDialog extends JDialog {

    public GroupMembersDialog(
            Window owner,
            ChatGroupRecord group,
            List<GroupMemberRecord> members,
            boolean adminsOnly
    ) {
        super(
                owner,
                (adminsOnly ? "Danh sách quản trị viên nhóm — " : "Danh sách thành viên hiện tại — ")
                        + group.getGroupName(),
                ModalityType.APPLICATION_MODAL
        );

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.SURFACE_CARD);

        root.add(createHeader(group, members.size(), adminsOnly), BorderLayout.NORTH);
        root.add(createBody(members), BorderLayout.CENTER);
        root.add(createFooter(group, adminsOnly), BorderLayout.SOUTH);

        setContentPane(root);
        setSize(new Dimension(920, 520));
        setMinimumSize(new Dimension(820, 420));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private JPanel createHeader(ChatGroupRecord group, int count, boolean adminsOnly) {
        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setBackground(AppTheme.SURFACE_CARD);

        JPanel header = new JPanel(new BorderLayout(AppTheme.SPACING_MD, 0));
        header.setBackground(AppTheme.SURFACE_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER),
                BorderFactory.createEmptyBorder(
                        AppTheme.SPACING_MD + 2,
                        AppTheme.CONTENT_PADDING,
                        AppTheme.SPACING_MD,
                        AppTheme.CONTENT_PADDING
                )
        ));

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        String titleText = adminsOnly
                ? "Danh sách quản trị viên (Admin) của nhóm chat"
                : "Danh sách thành viên hiện tại của nhóm chat";
        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(AppTheme.FONT_SECTION_TITLE);
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLabel = new JLabel(
                "Nhóm: " + group.getGroupName()
                        + "  •  Người tạo: " + group.getCreatorDisplay()
                        + "  •  " + group.getEncryptionLabel()
        );
        subLabel.setFont(AppTheme.FONT_CAPTION);
        subLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textCol.add(titleLabel);
        textCol.add(Box.createVerticalStrut(4));
        textCol.add(subLabel);

        String badgeText = adminsOnly
                ? "Admin nhóm hiện tại: " + count
                : "Thành viên hiện tại: " + count;
        JLabel badgeLabel = new JLabel(badgeText);
        badgeLabel.setFont(AppTheme.FONT_CAPTION_BOLD);
        badgeLabel.setForeground(adminsOnly ? AppTheme.PRIMARY : AppTheme.SUCCESS);
        badgeLabel.setOpaque(true);
        badgeLabel.setBackground(adminsOnly ? AppTheme.PRIMARY_SOFT_BG : AppTheme.SUCCESS_BG);
        badgeLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(badgeLabel);

        header.add(textCol, BorderLayout.CENTER);
        header.add(badgeWrapper, BorderLayout.EAST);

        JPanel roleNoticeBar = new JPanel(new BorderLayout());
        roleNoticeBar.setBackground(AppTheme.PRIMARY_SOFT_BG);
        roleNoticeBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER),
                BorderFactory.createEmptyBorder(8, AppTheme.CONTENT_PADDING, 8, AppTheme.CONTENT_PADDING)
        ));
        JLabel roleNoticeLabel = new JLabel(
                "<html><b>Phân biệt vai trò:</b> Cột <b>Vai trò trong nhóm</b> (<code>group_members.role</code>: ADMIN / MEMBER) "
                        + "độc lập với cột <b>Vai trò hệ thống</b> (<code>users.role</code>: USER / ADMIN).</html>"
        );
        roleNoticeLabel.setFont(AppTheme.FONT_CAPTION);
        roleNoticeLabel.setForeground(AppTheme.TEXT_PRIMARY);
        roleNoticeBar.add(roleNoticeLabel, BorderLayout.CENTER);

        headerWrapper.add(header, BorderLayout.NORTH);
        headerWrapper.add(roleNoticeBar, BorderLayout.SOUTH);
        return headerWrapper;
    }

    private JPanel createBody(List<GroupMemberRecord> members) {
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(AppTheme.PAGE_BG);
        body.setBorder(BorderFactory.createEmptyBorder(
                AppTheme.SPACING_MD,
                AppTheme.CONTENT_PADDING,
                AppTheme.SPACING_MD,
                AppTheme.CONTENT_PADDING
        ));

        JTable table = new JTable(new GroupMembersTableModel(members));
        AppTheme.configureSharedTable(table);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(0).setMaxWidth(48);
        table.getColumnModel().getColumn(1).setPreferredWidth(115);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setCellRenderer(new RolePillRenderer(true));
        table.getColumnModel().getColumn(3).setPreferredWidth(165);
        table.getColumnModel().getColumn(4).setCellRenderer(new RolePillRenderer(false));
        table.getColumnModel().getColumn(4).setPreferredWidth(155);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setPreferredWidth(125);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setPreferredWidth(95);

        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1, true));
        scrollPane.getViewport().setBackground(AppTheme.SURFACE_CARD);

        body.add(scrollPane, BorderLayout.CENTER);
        return body;
    }

    private JPanel createFooter(ChatGroupRecord group, boolean adminsOnly) {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AppTheme.SURFACE_CARD);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER),
                BorderFactory.createEmptyBorder(
                        AppTheme.SPACING_SM + 4,
                        AppTheme.CONTENT_PADDING,
                        AppTheme.SPACING_SM + 4,
                        AppTheme.CONTENT_PADDING
                )
        ));

        String departedNote = group.getDepartedMemberCount() > 0
                ? "Chỉ tính thành viên hiện tại (left_at IS NULL). Đã loại trừ "
                + group.getDepartedMemberCount() + " thành viên đã rời nhóm."
                : (adminsOnly
                ? "Hiển thị các thành viên hiện tại có vai trò ADMIN trong nhóm."
                : "Hiển thị toàn bộ thành viên đang tham gia nhóm (left_at IS NULL).");
        JLabel noteLabel = new JLabel(departedNote);
        noteLabel.setFont(AppTheme.FONT_CAPTION);
        noteLabel.setForeground(AppTheme.TEXT_SECONDARY);

        JButton closeButton = AppTheme.createSecondaryButton("Đóng", 100);
        closeButton.addActionListener(e -> dispose());

        footer.add(noteLabel, BorderLayout.WEST);
        footer.add(closeButton, BorderLayout.EAST);
        return footer;
    }

    private static final class RolePillRenderer extends JPanel implements javax.swing.table.TableCellRenderer {

        private final boolean groupRoleColumn;
        private String text = "";
        private boolean highlighted = false;
        private boolean rowSelected = false;

        private RolePillRenderer(boolean groupRoleColumn) {
            this.groupRoleColumn = groupRoleColumn;
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            this.text = value != null ? value.toString() : "";
            this.highlighted = text.contains("(ADMIN)");
            this.rowSelected = isSelected;
            setBackground(isSelected ? AppTheme.PRIMARY_SOFT_BG : AppTheme.SURFACE_CARD);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg;
            Color fg;
            if (groupRoleColumn) {
                bg = highlighted ? AppTheme.PRIMARY_SOFT_BG : AppTheme.NEUTRAL_BG;
                fg = highlighted ? AppTheme.PRIMARY : AppTheme.TEXT_PRIMARY;
            } else {
                bg = highlighted ? AppTheme.WARNING_BG : AppTheme.NEUTRAL_BG;
                fg = highlighted ? AppTheme.WARNING : AppTheme.TEXT_SECONDARY;
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

    private static final class GroupMembersTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {
                "STT",
                "Tên đăng nhập",
                "Họ và tên",
                "Vai trò trong nhóm",
                "Vai trò hệ thống",
                "Thời gian tham gia",
                "TT Tài khoản"
        };

        private final List<GroupMemberRecord> members;

        private GroupMembersTableModel(List<GroupMemberRecord> members) {
            this.members = members;
        }

        @Override
        public int getRowCount() {
            return members.size();
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
            GroupMemberRecord m = members.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> rowIndex + 1;
                case 1 -> "@" + m.getUsername();
                case 2 -> m.getFullName();
                case 3 -> m.getGroupRoleDisplay();
                case 4 -> m.getSystemRoleDisplay();
                case 5 -> m.getFormattedJoinedAt();
                case 6 -> m.getAccountStatusDisplay();
                default -> "";
            };
        }
    }
}
